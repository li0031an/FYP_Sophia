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
import android.graphics.PointF;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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


import com.example.amps.fyp_amps_android_yishan.DownloadAssetListener;
import com.example.amps.fyp_amps_android_yishan.R;
import com.example.amps.fyp_amps_android_yishan.Settings;
import com.example.amps.fyp_amps_android_yishan.touch.TouchImageView;

public class ImageReviewFullScreenActivity extends Activity implements Settings {
    private static final String TAG = "ImageReview";
    byte[] imageDecodedString;
    String assetExt;
//    SharedPreferences settings;
//    String projectId;
//    String assetidLst;
//    String revid;
    ImageView imageView;
    Uri uri;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        setContentView(R.layout.activity_image_review_full_screen);
        Bundle extras = getIntent().getExtras();
        ImageView imageView = (ImageView) findViewById(R.id.imageReviewFullScreen);
        if (extras != null) {
            String stringUri = extras.getString("imageUri");
            uri = Uri.parse(stringUri);
            Log.d(TAG, "image uri is gotten from extras: " + stringUri);
//            imageDecodedString = extras.getByteArray("imageDecodedString");
//            assetExt = extras.getString("imageExt");
        }
        if (null != uri) {
//            try{
//                URL url_new = new Uri(url);
//                imageView.setImageURI(url_new);
//            } catch (MalformedURLException e) {
//                Log.e(TAG, "url cannot be formed: original string: " + url);
//            }
//            imageView.setImageURI(url_new);
//                TouchImageView img = (TouchImageView) findViewById(R.id.imageReviewFullScreen);
//                img.setMaxZoom(4);
//            Drawable drawable = LoadImageFromWebOperations(url);
//            Log.d(TAG, "drawable: " + drawable);
//            imageView.setImageDrawable(drawable);
            imageView.setImageURI(uri);
            TouchImageView img = (TouchImageView) findViewById(R.id.imageReviewFullScreen);
            img.setMaxZoom(4);
        }



//        if (null != imageDecodedString) {
//            if (null != assetExt) {
//                switch (assetExt) {
//                    case "png":
//                    case "PNG":
//                }
//            }
//            Bitmap decodedByte = BitmapFactory.decodeByteArray(imageDecodedString, 0, imageDecodedString.length);
//            System.out.println(decodedByte.getConfig());
//            image.setImageBitmap(decodedByte);
//            String tokenid = settings.getString("tokenid", null);
//            String userid = settings.getString("userid", null);
//            String req = SZAAPIURL + "downloadAsset?tokenid=" + tokenid +
//                    "&userid=" + userid +
//                    "&projectid=" + projectId +
//                    "&assetid_lst=" + assetidLst;
//            if (null != revid) {
//                req += "&revid=" + revid;
//            }
//                req += "dostream=1";
//            URL url;
//            try{
//                url = new URL(req);
//            } catch (MalformedURLException e) {
//                Log.e(TAG, "url cannot be formed: original string: " + req);
//            }
//            if (null != url) {
//                image.setImageURI(url);
//                TouchImageView img = (TouchImageView) findViewById(R.id.imageReviewFullScreen);
//                img.setMaxZoom(4);
//            } else {
//                finish();
////            }
//        } else {
//            Log.e(TAG, "imageDecodedString is not available");
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return false;
    }

//    @Override
//    public void onDownloadAssetReady(Uri uri) {
//        ImageView image = (ImageView) findViewById(R.id.imageReviewFullScreen);
//        image.setImageURI(uri);
//        TouchImageView img = (TouchImageView) findViewById(R.id.imageReviewFullScreen);
//        img.setMaxZoom(4);
//    }

    public static Drawable LoadImageFromWebOperations(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            return d;
        } catch (Exception e) {
                Log.e(TAG, "url cannot be formed: original string: " + url);
        }
        return null;
    }


}

