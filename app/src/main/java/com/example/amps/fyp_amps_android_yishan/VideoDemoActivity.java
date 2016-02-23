package com.example.amps.fyp_amps_android_yishan;

/***
 * Copyright (c) 2008-2012 CommonsWare, LLC
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 * by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 * <p/>
 * From _The Busy Coder's Guide to Advanced Android Development_
 * http://commonsware.com/AdvAndroid
 */


import java.io.File;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.amps.fyp_amps_android_yishan.preview.VideoEnabledWebChromeClient;
import com.example.amps.fyp_amps_android_yishan.preview.VideoEnabledWebView;

public class VideoDemoActivity extends Activity implements Settings{
    private VideoView video;
    private MediaController ctlr;
    private static final String TAG = "VideoPlayerActivity";
//    private VideoEnabledWebView webView;
//    private VideoEnabledWebChromeClient webChromeClient;
    String token_id;
    String user_id;
    String asset_id;
    String project_id;
    String video_url;
    String revNum;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.activity_video_demo);
        getActionBar().hide();
//
//        File clip = new File(Environment.getExternalStorageDirectory(),
//                "test.mp4");

//        if (clip.exists()) {

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            SharedPreferences settings = getSharedPreferences(SETTINGS, 0);
            token_id = settings.getString("tokenid", null);
            user_id = settings.getString("userid", null);
            asset_id = extras.getString("asset_id");
            project_id = extras.getString("project_id");
            revNum = extras.getString("revNum");
        }

        String VideoURL = SZAAPIURL + "downloadAsset?tokenid=" + token_id +
                "&userid=" + user_id +
                "&projectid=" + project_id +
                "&assetid_lst=" + asset_id +
                "&revnum=" + revNum;

        video = (VideoView) findViewById(R.id.video);
        DisplayMetrics metrics = new DisplayMetrics(); getWindowManager().getDefaultDisplay().getMetrics(metrics);
        android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) video.getLayoutParams();
        params.width =  metrics.widthPixels;
        params.height = metrics.heightPixels;
        params.leftMargin = 0;
//            video.setVideoPath(clip.getAbsolutePath());
        video.setLayoutParams(params);
        video.setVideoURI(Uri.parse(VideoURL));
        ctlr = new MediaController(this, false);
        ctlr.setMediaPlayer(video);
        video.setMediaController(ctlr);
        video.requestFocus();
        video.start();
//        }
    }
}