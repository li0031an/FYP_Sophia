package com.example.amps.fyp_amps_android_yishan.preview;

import java.io.ByteArrayOutputStream;

//import com.example.amps.WorkingAssetsListActivity.GetProjectInfo;
//import com.example.amps.touch.TouchImageView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;


import com.example.amps.fyp_amps_android_yishan.R;
import com.example.amps.fyp_amps_android_yishan.Settings;
import com.example.amps.fyp_amps_android_yishan.touch.TouchImageView;

public class ImageReviewFullScreenActivity extends Activity implements Settings {
    byte[] imageDecodedString;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        setContentView(R.layout.activity_image_review_full_screen);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            imageDecodedString = extras.getByteArray("imageDecodedString");
        }
        ImageView image = (ImageView) findViewById(R.id.imageReviewFullScreen);

        Bitmap decodedByte = BitmapFactory.decodeByteArray(imageDecodedString, 0, imageDecodedString.length);
        System.out.println(decodedByte.getConfig());
        image.setImageBitmap(decodedByte);
        TouchImageView img = (TouchImageView) findViewById(R.id.imageReviewFullScreen);
        img.setMaxZoom(4);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return false;
    }
}

