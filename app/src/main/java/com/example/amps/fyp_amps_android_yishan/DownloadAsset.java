package com.example.amps.fyp_amps_android_yishan;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class DownloadAsset extends AsyncTask<Object, String, Object> implements Settings {

    private static String TAG = "DownloadAsset";
    Activity activity;
    ProgressDialog dialog;
    SharedPreferences settings;
    String projectId;
    String assetidLst;
    String assetFullName;
    String revid;
    int size_type = 0;
    int progressInt = 0;
    DecimalFormat twoDP = new DecimalFormat("#.##");
    ProgressDialog downloadDialog;

    public DownloadAsset(Activity activity, SharedPreferences settings, String assetidLst
            , String projectId, String assetFullName, String revid) {
        this.activity = activity;
        this.settings = settings;
        this.assetidLst = assetidLst;
        this.projectId = projectId;
        this.assetFullName = assetFullName;
        this.revid = revid;
    }

    @Override
    protected void onPreExecute() {

        downloadDialog = new ProgressDialog(activity);
        downloadDialog.setMessage("Downloading " + assetFullName + ".. Please wait..");
        downloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //downloadDialog.setMax(100);
        downloadDialog.setProgressNumberFormat("0 MB/s");
        downloadDialog.setIndeterminate(false);
        downloadDialog.setCancelable(false);
        downloadDialog.show();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected String doInBackground(Object... arg0) {
        //return downloadAsset();
        int count;
        String tokenid = settings.getString("tokenid", null);
        String userid = settings.getString("userid", null);
        String req = SZAAPIURL + "downloadAsset?tokenid=" + tokenid +
                "&userid=" + userid +
                "&projectid=" + projectId +
                "&assetid_lst=" + assetidLst;
//                "&revid=" + revid;
        String downloadedFileName = assetFullName;
        //TrafficStats traffic = new TrafficStats();
        //double totalNetworkBytes = traffic.getTotalTxBytes();

        File downloadFolder = new File(Environment.getExternalStorageDirectory() + "/AMPS");
        boolean folderExist = true;
        if (!downloadFolder.exists()) {
            folderExist = downloadFolder.mkdir();
        }
        if (folderExist) {
            File downloadLocation = new File(downloadFolder, downloadedFileName);
            Log.d(TAG, "downloadLocation: " + downloadLocation.toString());
            InputStream inputStream = null;
            OutputStream fileOutput = null;
            try {
                URL url = new URL(req);
                Log.d(TAG, "URL: " + req);
                long startTime = System.currentTimeMillis();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.connect();
                //get downloaded data
                //FileOutputStream fileOutput = new FileOutputStream (downloadLocation);
                fileOutput = new FileOutputStream(downloadLocation);

                //get data from internet
                inputStream = connection.getInputStream();

                //total size of the file
                int totalSize = connection.getContentLength();
                long total = 0;
                String unit = "";
                double newSpeed = 0.00;
                //create buffer...
                byte[] data = new byte[1024];
                long elapsedTime = System.currentTimeMillis() - startTime;
                //long startTime = System.nanoTime();	//Initialise the time for download speed
                //final double downloadSpeedPerSec = 1000000000.00;
                //final float bytesPerMib = 1024 * 1024;
                Log.d(TAG, "inputStream: " + inputStream.toString());
                while ((count = inputStream.read(data)) != -1) {
                    total += count;
                    //calculate speed
                    double speed = total * 1000.0f / elapsedTime;

                    if (speed < 1024) {
                        newSpeed = Double.parseDouble(twoDP.format(speed));
                        unit = "Bytes/ sec";
                    } else if (speed < 1024 * 1024) {
                        newSpeed = Double.parseDouble(twoDP.format(speed / 1024));
                        unit = "kB/s";
                    } else {
                        newSpeed = Double.parseDouble(twoDP.format((speed / 1024 * 1024) / 1000000));
                        unit = "MB/s";
                    }

                    downloadDialog.setProgressNumberFormat(newSpeed + unit);
                    //increase from 0-100%
                    publishProgress("" + (int) ((total * 100) / totalSize));
                    fileOutput.write(data, 0, count);
                }

                //close connection
                fileOutput.flush();
                fileOutput.close();
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "ERROR");
            } finally {
                if (null != inputStream) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "inputStream cannot be closed.");
                        e.printStackTrace();
                    }
                }
                if (null != fileOutput) {
                    try {
                        fileOutput.close();
                    } catch (IOException e) {
                        Log.e(TAG, "fileOutput cannot be closed.");
                        e.printStackTrace();
                    }
                }
            }
        } else {
            Log.e(TAG, "folder AMPS cannot be created");
        }

        return null;
    }

    //Updating Progress Bar
    //Focus:
    protected void onProgressUpdate(String... progress) {
        //Log.d("",progress[0]);
        //super.onProgressUpdate(progress);
        progressInt = Integer.parseInt(progress[0]);
        downloadDialog.setProgress(Integer.parseInt(progress[0]));
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPostExecute(Object result) {
        downloadDialog.dismiss();
        File downloadFolder = new File(Environment.getExternalStorageDirectory() + "/AMPS");
        Log.d(TAG, "downloaded to " + downloadFolder);
        if (progressInt == 100) {
            AlertDialog downloadComplete = new AlertDialog.Builder(activity).create();
            downloadComplete.setTitle("Download Status");
            downloadComplete.setMessage(assetFullName + " is downloaded to folder Pictures/AMPS successfully.");
            downloadComplete.setButton("OK", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub

                }
            });
            downloadComplete.show();
        } else {
            AlertDialog downloadError = new AlertDialog.Builder(activity).create();
            downloadError.setTitle("Download Status");
            downloadError.setMessage("Failed to download " + assetFullName + ".. Please try again later..");
            downloadError.setButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //  TODO Auto-generated method stub

                }
            });
            downloadError.show();
        }
    }

    /**
     * public String downloadAsset() {
     * String req = SZAAPIURL + "downloadAsset?tokenid=" + tokenid +
     * "&userid=" + userid +
     * "&projectid=" + project_id +
     * "&assetid_lst=" + asset_id +
     * "&revid=" + a.getRevId() ;
     * try {
     * DownloadManager downloadManager;
     * downloadManager = (DownloadManager)WorkingAssetsPreviewFragment.this.getActivity().getSystemService("download");
     * Uri uri = Uri.parse(req);
     * DownloadManager.Request request = new DownloadManager.Request(uri);
     * request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, a.getName() + "." + a.getExt());
     * downloadManager.enqueue(request);
     * } catch (Exception e) {
     * e.printStackTrace();
     * }
     * return null;
     * }
     **/
    public void showToast(String info) {
        Toast toast = Toast.makeText(
                activity,
                info,
                Toast.LENGTH_SHORT);
        toast.show();
    }

}
