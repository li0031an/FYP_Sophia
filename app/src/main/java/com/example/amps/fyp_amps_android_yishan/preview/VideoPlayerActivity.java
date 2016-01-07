package com.example.amps.fyp_amps_android_yishan.preview;

import java.io.IOException;

import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.amps.fyp_amps_android_yishan.R;
import com.example.amps.fyp_amps_android_yishan.Settings;

public class VideoPlayerActivity extends Activity implements Settings {
    /*// Declare variables
    ProgressDialog pDialog;
    VideoView videoview;
    String userid;
    String token_id;
    String asset_id;
    String project_id;
    String video_url;
    String revNum;
    // Insert your Video URL


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the layout from video_main.xml
        setContentView(R.layout.video_player);
        // Find your VideoView in your video_main.xml layout
        videoview = (VideoView) findViewById(R.id.video_player);
        // Execute StreamVideo AsyncTask
        //String VideoURL = "http://www.androidbegin.com/tutorial/AndroidCommercial.3gp";
        String VideoURL = SZAAPIURL + "downloadAsset?tokenid=" + token_id +
                "&userid=" + userid +
                "&projectid=" + project_id +
                "&assetid_lst=" + asset_id +
                "&revnum=" + revNum;
        // Create a progressbar
        pDialog = new ProgressDialog(VideoPlayerActivity.this);
        // Set progressbar title
        pDialog.setTitle("Android Video Streaming Tutorial");
        // Set progressbar message
        pDialog.setMessage("Buffering...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        // Show progressbar
        pDialog.show();

        try {
            // Start the MediaController
            MediaController mediacontroller = new MediaController(VideoPlayerActivity.this);
            mediacontroller.setAnchorView(videoview);
            // Get the URL from String VideoURL
            Uri video = Uri.parse(VideoURL);
            videoview.setMediaController(mediacontroller);
            videoview.setVideoURI(video);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }

        videoview.requestFocus();
        videoview.setOnPreparedListener(new OnPreparedListener() {
            // Close the progress bar and play the video
            public void onPrepared(MediaPlayer mp) {
                pDialog.dismiss();
                videoview.start();
            }
        });

    }*/
    private VideoEnabledWebView webView;
    private VideoEnabledWebChromeClient webChromeClient;
    String token_id;
    String user_id;
    String asset_id;
    String project_id;
    String video_url;
    String revNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout
        setContentView(R.layout.video_player);

        // Save the web view
        webView = (VideoEnabledWebView) findViewById(R.id.webView);

        // Initialize the VideoEnabledWebChromeClient and set event handlers
        final View nonVideoLayout = findViewById(R.id.nonVideoLayout); // Your own view, read class comments
        final ViewGroup videoLayout = (ViewGroup) findViewById(R.id.videoLayout); // Your own view, read class comments
        View loadingView = getLayoutInflater().inflate(R.layout.video_player, null); // Your own view, read class comments
        final Activity activity = this;
        webChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout, videoLayout, loadingView, webView) // See all available constructors...
        {
            // Subscribe to standard events, such as onProgressChanged()...
            @Override
            public void onProgressChanged(WebView view, int progress) {
                activity.setProgress(progress * 1000);
            }
        };
        webChromeClient.setOnToggledFullscreen(new VideoEnabledWebChromeClient.ToggledFullscreenCallback() {
            @Override
            public void toggledFullscreen(boolean fullscreen) {
                // Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
                if (fullscreen) {
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 14) {
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                    }
                } else {
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 14) {
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }
                }

            }
        });
        webView.setWebChromeClient(webChromeClient);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            SharedPreferences settings = getSharedPreferences(SETTINGS, 0);
            token_id = settings.getString("tokenid", null);
            user_id = settings.getString("userid", null);
            asset_id = extras.getString("asset_id");
            project_id = extras.getString("project_id");
            revNum = extras.getString("revNum");
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(false);
        webView.getSettings().setUseWideViewPort(false);
        String VideoURL = SZAAPIURL + "downloadAsset?tokenid=" + token_id +
                "&userid=" + user_id +
                "&projectid=" + project_id +
                "&assetid_lst=" + asset_id +
                "&revnum=" + revNum;
        // Navigate everywhere you want, this classes have only been tested on YouTube's mobile site
        String play = "<!DOCTYPE html><html><body><video width=\"320\" height=\"240\" controls autoplay autobuffer src=" + VideoURL + ">Your browser does not support the video tag.</video></body></html>";
        webView.loadData(play, "text/html", "utf-8");
        webView.setBackgroundColor(0x00000000);
    }


    @Override
    public void onBackPressed() {
        // Notify the VideoEnabledWebChromeClient, and handle it ourselves if it doesn't handle it
        if (!webChromeClient.onBackPressed()) {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                // Close app (presumably)
                super.onBackPressed();
            }
        }
    }

    protected void onPause() {
        super.onPause();
        ((AudioManager) getSystemService(
                Context.AUDIO_SERVICE)).requestAudioFocus(
                new OnAudioFocusChangeListener() {
                    @Override
                    public void onAudioFocusChange(int focusChange) {
                    }
                }, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
    }
}

