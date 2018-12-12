package com.project.androidtestwidgetonly;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;
import android.widget.ViewAnimator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * Implementation of App Widget functionality.
 */
public class Schedule extends AppWidgetProvider
{
    static final String site ="http://www.mrk-bsuir.by/ru";
    static String pdfUrl = "";
    static final String UPDATE_ACTION = "update_action";
    static final String FORCE_UPDATE_ACTION = "force_update_action";
    final static String PREFIX = "ppreffix";

    static void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId, final boolean force)
    {
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.schedule);

        final SharedPreferences sp = context.getSharedPreferences(ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE);

        Intent intent1 = new Intent(context, Schedule.class);
        intent1.setAction(FORCE_UPDATE_ACTION);
        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, 0, intent1, 0);
        views.setOnClickPendingIntent(R.id.imageView, pendingIntent1);
        views.setOnClickPendingIntent(R.id.relative, pendingIntent1);

        if(force)
            Log.d(PREFIX, "Force update");
        Handler handler = new Handler(Looper.getMainLooper());
        if(force)
            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    Toast.makeText(context,"Updating...", Toast.LENGTH_SHORT).show();
                }
            });
        class Asynk extends AsyncTask<Void, Void, Void>
        {
            @Override
            protected Void doInBackground(Void... voids)
            {
                if(sp.getBoolean("widgetNotReady" + appWidgetId, true))
                {
                    return null;
                }
                Log.d(PREFIX, "doInBackground");
                try
                {
                    String pdfUrlNew = "";
                    URL siteUrl = new URL(site);
                    try(BufferedReader bf1 = new BufferedReader(new InputStreamReader(siteUrl.openStream())))
                    {
                        StringBuilder sb1 = new StringBuilder();
                        String line1 = null;
                        while((line1 = bf1.readLine()) != null)
                        {
                            sb1.append(line1);
                        }
                        int pos1 = sb1.indexOf("Объявления");
                        int end1 = sb1.indexOf(".pdf", pos1);
                        pdfUrlNew = sb1.substring(pos1+76, end1+4);
                    }

                    views.setTextViewText(R.id.textView,DateFormat.format("dd/MM/yyyy HH:mm:ss", System.currentTimeMillis()).toString());
                    views.setViewVisibility(R.id.textView, sp.getBoolean("switch" + appWidgetId, true) ? View.VISIBLE : View.INVISIBLE);

                    if(pdfUrl == null || pdfUrlNew != pdfUrl)
                    {
                        pdfUrl = pdfUrlNew;
                        URL file = new URL(pdfUrl);

                        //String fileNamePath = Environment.getExternalStorageDirectory().toString() + "/load/" + "rasp.pdf";
                        //File dire = new File(Environment.getExternalStorageDirectory().toString() + "/load");
                        //if(!dire.exists())
                        //    dire.mkdir();
                        File nonexFile = File.createTempFile("pref", "suff");
                        try(InputStream is = file.openStream())
                        {
                            int len;
                            byte[] buf = new byte[2048 * 2];
                            try(FileOutputStream fos = new FileOutputStream(nonexFile))
                            {
                                while((len = is.read(buf)) != -1)
                                {
                                    fos.write(buf, 0 , len);
                                }
                            }
                        }


                        try(PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(nonexFile, ParcelFileDescriptor.MODE_READ_ONLY)))
                        {

                            int pageI = sp.getInt("page" + appWidgetId, 0);
                            Log.d(PREFIX, Integer.toString(pageI));
                            PdfRenderer.Page page = renderer.openPage(pageI);
                            Bitmap bitmap = Bitmap.createBitmap(page.getWidth()*2, page.getHeight()*2, Bitmap.Config.ARGB_8888);
                            Log.d(PREFIX, Integer.toString(page.getHeight()));
                            Log.d(PREFIX, Integer.toString(page.getWidth()));
                            page.render(bitmap, new Rect(0, 0, page.getWidth() * 2, page.getHeight() * 2), null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                            Log.d(PREFIX, Integer.toString(bitmap.getHeight()));
                            Log.d(PREFIX, Integer.toString(bitmap.getWidth()));
                            int x1 = sp.getInt("x1" + appWidgetId, 0);
                            int x2 = sp.getInt("x2" + appWidgetId, 0);
                            int y1 = sp.getInt("y1" + appWidgetId, 0);
                            int y2 = sp.getInt("y2" + appWidgetId, 0);
                            Log.d(PREFIX, Integer.toString(x1));
                            Log.d(PREFIX, Integer.toString(y1));
                            Log.d(PREFIX, Integer.toString(x2));
                            Log.d(PREFIX, Integer.toString(y2));
                            Bitmap bmp = Bitmap.createBitmap(bitmap, x1, y1, x2 - x1, y2 - y1);
                            views.setBitmap(R.id.imageView, "setImageBitmap", bmp);
                            page.close();
                        }

                    }
                    Log.d(PREFIX, "Update");
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                    Handler handler = new Handler(Looper.getMainLooper());
                    if(force)
                        handler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Toast.makeText(context,"DONE", Toast.LENGTH_LONG).show();
                            }
                        });
                }
                catch (Exception e)
                {
                    Log.d(PREFIX, "error", e);
                    Handler handler = new Handler(Looper.getMainLooper());
                    if(force)
                        handler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Toast.makeText(context,"Something went wrong...", Toast.LENGTH_LONG).show();
                            }
                        });
                    e.printStackTrace();
                }
                return null;
            }
        }

        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(mWifi.isConnected() || force)
        {
            new Asynk().execute();
        }
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, false);
        }
    }

    @Override
    public void onEnabled(Context context)
    {
        super.onEnabled(context);
        Log.d(PREFIX, "onEnabled");
        Intent intent = new Intent(context, Schedule.class);
        intent.setAction(UPDATE_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 60000, pendingIntent);
    }

    @Override
    public void onDisabled(Context context)
    {
        super.onDisabled(context);
        Log.d(PREFIX, "onDisabled");
        Intent intent = new Intent(context, Schedule.class);
        intent.setAction(UPDATE_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context, intent);
        Log.d(PREFIX, "onReceive");
        if(intent.getAction().equalsIgnoreCase(UPDATE_ACTION))
        {
            ComponentName name = new ComponentName(context.getPackageName(), getClass().getName());
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int[] ids = manager.getAppWidgetIds(name);
            for(int i: ids)
            {
                updateAppWidget(context, manager, i, false);
            }
        }
        else if(intent.getAction().equalsIgnoreCase(FORCE_UPDATE_ACTION))
        {

            Log.d(PREFIX, "onReceiveForce");
            ComponentName name = new ComponentName(context.getPackageName(), getClass().getName());
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int[] ids = manager.getAppWidgetIds(name);
            for(int i: ids)
            {
                updateAppWidget(context, manager, i, true);
            }
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        SharedPreferences.Editor editor = context.getSharedPreferences(ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE).edit();
        for(int id: appWidgetIds)
        {
            editor.remove("x1" + id);
            editor.remove("x2" + id);
            editor.remove("y1" + id);
            editor.remove("y2" + id);
            editor.remove("edit" + id);
            editor.remove("switch" + id);
            editor.remove("labelX" + id);
            editor.remove("labelY" + id);
        }
        editor.apply();
    }
}

