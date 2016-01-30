package com.example.amps.fyp_amps_android_yishan.preview;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
//import android.net.uri;

//import com.example.amps.WorkingAssetsListActivity.GetProjectInfo;
//import com.example.amps.touch.TouchImageView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;


import com.example.amps.fyp_amps_android_yishan.DownloadAsset;
import com.example.amps.fyp_amps_android_yishan.DownloadAssetListener;
import com.example.amps.fyp_amps_android_yishan.R;
import com.example.amps.fyp_amps_android_yishan.Settings;
import com.example.amps.fyp_amps_android_yishan.touch.TouchImageView;

public class ImageReviewFullScreenActivity extends Activity implements Settings, DownloadAssetListener {
    private static final String TAG = "ImageReview";
    private static final int PREVIEW_SCREEN_MULTIPLIER = 1;
    ImageView imageView;
    String imageExt;
    String imageId;
    String imageProjectId;
    String imageFullName;
    String imageLatestRevid;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        setContentView(R.layout.activity_image_review_full_screen);
        Bundle extras = getIntent().getExtras();
        imageView = (ImageView) findViewById(R.id.imageReviewFullScreen);
        if (extras != null) {
            imageExt = extras.getString("imageExt");
            imageId = extras.getString("imageId");
            imageProjectId = extras.getString("imageProjectId");
            imageFullName = extras.getString("imageFullName");
            imageLatestRevid = extras.getString("imageLatestRevid");

//            uri = Uri.parse(stringUri);
//            Log.d(TAG, "image uri is gotten from extras: " + stringUri);
        }
//        if (null != uri) {
//            imageView.setImageURI(uri);
//            TouchImageView img = (TouchImageView) findViewById(R.id.imageReviewFullScreen);
//            img.setMaxZoom(4);
//        }
        SharedPreferences settings = getSharedPreferences(SETTINGS, 0);
        if (null != imageExt && null != imageId && null != imageLatestRevid
                && null != imageProjectId && null != imageFullName) {
            DownloadAsset taskDownload = new DownloadAsset(ImageReviewFullScreenActivity.this, settings
                    , imageId, imageProjectId, imageFullName, imageExt, imageLatestRevid,
                    ImageReviewFullScreenActivity.this, true);
            taskDownload.execute();
        } else {
            Log.e(TAG, "one of the input is null");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return false;
    }

    @Override
    public void onDownloadAssetReady(Bitmap bitmap) {
//        Bitmap bitmap = decodeImage(inputStream, width, height);
//        Log.d(TAG, "bitmap size: " + bitmap.getByteCount());
        imageView.setImageBitmap(bitmap);
    }

    @Override
    public int getRequiredImageWidth() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        int requiredWidth = PREVIEW_SCREEN_MULTIPLIER * screenWidth;
        return requiredWidth;
    }

    @Override
    public int getRequiredImageHeight() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenHeight = size.y;
        int requiredHeight = PREVIEW_SCREEN_MULTIPLIER * screenHeight;
        return requiredHeight;
    }

}

