package com.project.androidtestwidgetonly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class SetDatePositionActivity extends AppCompatActivity {

    int widgetId;
    ImageView image;
    boolean firstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_date_position);
        Intent intent = getIntent();
        widgetId = intent.getIntExtra("widgetID", -1);
        image = (ImageView) findViewById(R.id.imageView3);
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus && firstTime)
        {
            try
            {
                firstTime = false;
                Log.d(Schedule.PREFIX, Integer.toString(((ImageView)findViewById(R.id.imageView3)).getWidth()) + "FOCUS CHANGED+_+_+_+_+_+_+_+_+_+_+_");
                SharedPreferences sp = getSharedPreferences(ConfigActivity.WIDGET_PREF, MODE_PRIVATE);
                String path = sp.getString("FileNameImage", "ss");
                sp.edit().remove("FileNameImage").apply();
                Bitmap bmpOriginal = BitmapFactory.decodeFile(path);
                int x = sp.getInt("x1"+widgetId, 0);
                int y = sp.getInt("y1"+widgetId, 0);
                int width = sp.getInt("x2"+widgetId, 0) - x;
                int height = sp.getInt("y2"+widgetId, 0) - y;
                Bitmap bmp = Bitmap.createBitmap(bmpOriginal, x, y, width, height);
                int realWidth = image.getWidth();
                int realHeight = image.getHeight();
                int imageWidth = bmp.getWidth();
                int imageHeight = bmp.getHeight();
                float realK = (float)realHeight / realWidth;
                float imageK = (float)imageHeight / imageWidth;
                Log.d(ConfigActivity.WIDGET_PREF + "2", "realWidth: " + realWidth);
                Log.d(ConfigActivity.WIDGET_PREF + "2", "realHeight: " + realHeight);
                Log.d(ConfigActivity.WIDGET_PREF + "2", "imageWidth: " + imageWidth);
                Log.d(ConfigActivity.WIDGET_PREF + "2", "imageHeight: " + imageHeight);
                Log.d(ConfigActivity.WIDGET_PREF + "2", "realK: " + realK);
                Log.d(ConfigActivity.WIDGET_PREF + "2", "imageK: " + imageK);
                if(realK > imageK)
                {
                    Log.d(ConfigActivity.WIDGET_PREF + "2", "realK > imageK: " + imageK * realWidth);
//                    image.setMaxHeight((int)imageK * realWidth);
                    image.getLayoutParams().height = (int)(imageK * realWidth);
                    image.getLayoutParams().width = realWidth;
                }
                else
                {
                    Log.d(ConfigActivity.WIDGET_PREF + "2", "!realK > imageK: " + imageK * realHeight);
//                    image.setMaxWidth((int)imageK * realHeight);
                    image.getLayoutParams().width = (int)( realHeight / imageK);
//                    image.getLayoutParams().height = realHeight;
//                    ((ViewGroup.MarginLayoutParams)image.getLayoutParams()).rightMargin = ((ViewGroup.MarginLayoutParams)image.getLayoutParams()).leftMargin + realWidth - ((int)imageK * realHeight);
                }
                image.setImageBitmap(bmp);
            }
            catch (Exception e)
            {
                Log.d(Schedule.PREFIX, "errer", e);
            }
        }
    }
}
