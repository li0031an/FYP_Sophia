package com.example.amps.fyp_amps_android_yishan;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

public class DownloadAsset extends AsyncTask<Object, String, Object> implements Settings {

    private static String TAG = "DownloadAsset";
    Activity activity;
    ProgressDialog dialog;
    SharedPreferences settings;
    String projectId;
    String assetidLst;
    String assetFullName;
    String revid;
    String assetExt;
    String virtualPath;
    String physicalPath;
    File downloadFolder;
    int progressInt = 0;
    DecimalFormat twoDP = new DecimalFormat("#.##");
    ProgressDialog downloadDialog;

    public DownloadAsset(Activity activity, SharedPreferences settings, String assetidLst
            , String projectId, String assetFullName, String assetExt, String revid) {
        this.activity = activity;
        this.settings = settings;
        this.assetidLst = assetidLst;
        this.projectId = projectId;
        this.assetFullName = assetFullName;
        this.assetExt = assetExt;
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
        String tokenid = settings.getString("tokenid", null);
        String userid = settings.getString("userid", null);
        String req = SZAAPIURL + "downloadAsset?tokenid=" + tokenid +
                "&userid=" + userid +
                "&projectid=" + projectId +
                "&assetid_lst=" + assetidLst;
        if (null != revid) {
            req += "&revid=" + revid;
        }
        Log.d(TAG, "revid: " + revid);
        //TrafficStats traffic = new TrafficStats();
        //double totalNetworkBytes = traffic.getTotalTxBytes();

        downloadFolder = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "AMPS");
        virtualPath = "Picture/AMPS";
        boolean folderExist = true;
        Log.d(TAG, "downloadFolder.exists(): " + String.valueOf(downloadFolder.exists()));
        Log.d(TAG, "downloadFolder.isDirectory(): " + String.valueOf(downloadFolder.isDirectory()));
        if ((!downloadFolder.exists()) || (!downloadFolder.isDirectory())) {
            folderExist = downloadFolder.mkdirs();
            Log.d(TAG, "downloadFolder is created: " + downloadFolder.toString());
            Log.d(TAG, "folderExist: " + String.valueOf(folderExist));
        }
        if (folderExist) {
            File downloadLocation = new File(downloadFolder, assetFullName);
            Log.d(TAG, "downloadLocation: " + downloadLocation.toString());
            InputStream inputStream = null;
            OutputStream fileOutput = null;
            try {
                URL url = new URL(req);
                Log.d(TAG, "URL: " + req);
                long startTime = System.currentTimeMillis();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestMethod("GET");
//                connection.setFixedLengthStreamingMode(bytes.length);
                connection.setDoOutput(true);
                connection.connect();
                //get downloaded data
                //FileOutputStream fileOutput = new FileOutputStream (downloadLocation);
                fileOutput = new FileOutputStream(downloadLocation, false);

                //get data from internet
                inputStream = new BufferedInputStream(connection.getInputStream());

                //total size of the file
                int totalSize = connection.getContentLength();
                //variable to store total downloaded bytes
                int downloadedSize = 0;
                String unit = "";
                double newSpeed = 0.00;
                //create buffer...
                byte[] buffer = new byte[1024];
                int bufferLength = 0;//used to store a temporary size of the buffer

                long elapsedTime = System.currentTimeMillis() - startTime;
                //long startTime = System.nanoTime();	//Initialise the time for download speed
                //final double downloadSpeedPerSec = 1000000000.00;
                //final float bytesPerMib = 1024 * 1024;
                Log.d(TAG, "inputStream: " + inputStream.toString());
//                while ((count = inputStream.read(data)) != -1) {

                //now, read through the input buffer and write the contents to the file
                while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                    //add the data in the buffer to the file in the file output stream (the file on the sd card
                    fileOutput.write(buffer, 0, bufferLength);
                    //add up the size so we know how much is downloaded
                    downloadedSize += bufferLength;
                    //this is where you would do something to report the prgress, like this maybe
//                    updateProgress(downloadedSize, totalSize);

                    double speed = downloadedSize * 1000.0f / elapsedTime;

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
                    publishProgress("" + (int) ((downloadedSize * 100) / totalSize));
//                    fileOutput.write(buffer, 0, count);

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
        Log.d(TAG, "downloaded to " + downloadFolder.toString());
        if (progressInt == 100) {
            AlertDialog downloadComplete = new AlertDialog.Builder(activity).create();
            downloadComplete.setTitle("Download Status");
            downloadComplete.setMessage(assetFullName + " is downloaded to folder AMPS successfully.");
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
