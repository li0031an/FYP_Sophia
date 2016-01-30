package com.example.amps.fyp_amps_android_yishan;

import android.graphics.Bitmap;

public interface DownloadAssetListener {
    void onDownloadAssetReady(Bitmap bitmap);
    int getRequiredImageWidth();
    int getRequiredImageHeight();
}
