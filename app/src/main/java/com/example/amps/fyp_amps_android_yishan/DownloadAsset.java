package com.example.amps.fyp_amps_android_yishan;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
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

public class DownloadAsset extends AsyncTask<Object, String, Bitmap> implements Settings {

    private static String TAG = "DownloadAsset";
    DownloadAssetListener downloadAssetListener;
    Activity activity;
    ProgressDialog dialog;
    SharedPreferences settings;
    String projectId;
    String assetidLst;
    String assetFullName;
    String revid;
    String assetExt;
    String shortDownloadDirectory;
    File downloadFolder;
    File downloadedFile;
    int progressInt = 0;
    boolean isDoStream;
    boolean folderExist = false;
    boolean isDownloadSucessful = true;
    FileType filetype;
    DecimalFormat twoDP = new DecimalFormat("#.##");
    ProgressDialog downloadDialog;
    String url;
    DecimalFormat formatter = new DecimalFormat("#.##");

    public DownloadAsset(Activity activity, SharedPreferences settings, String assetidLst
            , String projectId, String assetFullName, String assetExt, String revid,
                         DownloadAssetListener downloadAssetListener, boolean isDoStream) {
        this.activity = activity;
        this.settings = settings;
        this.assetidLst = assetidLst;
        this.projectId = projectId;
        this.assetFullName = assetFullName;
        this.assetExt = assetExt;
        this.revid = revid;
        this.downloadAssetListener = downloadAssetListener;
        this.isDoStream = isDoStream;
        setDownloadedFileFormat(assetExt);
    }

    public enum FileType {
        IMAGE,
        VIDEO,
        AUDIO,
        DOCUMENT,
        OTHER
    }

    public void setDownloadedFileFormat(String assetExt) {
        if (((assetExt.equals("jpg") || (assetExt.equals("png") || (assetExt.equals("jpeg")) || (assetExt.equals("gif")))))) {
            filetype = FileType.IMAGE;
        } else if ((assetExt.equals("avi") || (assetExt.equals("flv") || (assetExt.equals("mp4")) || (assetExt.equals("webm")) || (assetExt.equals("wmv"))))) {
            filetype = FileType.VIDEO;
        } else if ((assetExt.equals("pdf") || (assetExt.equals("txt") || (assetExt.equals("doc")) || (assetExt.equals("xml")) || (assetExt.equals("pptx"))))) {
            filetype = FileType.DOCUMENT;
        } else if ((assetExt.equals("mp3"))) {
            filetype = FileType.AUDIO;
        } else {
            filetype = FileType.OTHER;
        }
        Log.d(TAG, "filetype: " + filetype);
    }

    public boolean setDownloadFolder() {
        shortDownloadDirectory = "";
        File downloadDirectory;
        switch (filetype) {
            case IMAGE:
                shortDownloadDirectory = Environment.DIRECTORY_PICTURES;
                break;
            case VIDEO:
                shortDownloadDirectory = Environment.DIRECTORY_MOVIES;
                break;
//            case DOCUMENT: //only applicable to API 19 and above
//                shortDownloadDirectory = Environment.DIRECTORY_DOCUMENTS;
//                break;
            default:
                shortDownloadDirectory = Environment.DIRECTORY_DOWNLOADS;
        }
        boolean downloadFolderExist = true;

        //prepare download directory - should be existing, directory and writable
        downloadDirectory = new File(Environment.getExternalStorageDirectory(), shortDownloadDirectory);
        if (!downloadDirectory.exists() || (!downloadDirectory.isDirectory())) {
            downloadFolderExist = downloadDirectory.mkdir();
        }

        //prepare download folder - should be existing, directory and writable
        if (downloadFolderExist) {
            if (downloadDirectory.canWrite() || downloadDirectory.setWritable(true, true)) {
                downloadFolder = new File(downloadDirectory, "/AMPS");
                Log.d(TAG, "shortDownloadDirectory: " + shortDownloadDirectory);
                Log.d(TAG, "downloadFolder: " + downloadFolder.toString());
                Log.d(TAG, "downloadFolder.exists(): " + String.valueOf(downloadFolder.exists()));
                Log.d(TAG, "downloadFolder.isDirectory(): " + String.valueOf(downloadFolder.isDirectory()));
                if ((!downloadFolder.exists()) || (!downloadFolder.isDirectory())) {
                    downloadFolderExist = downloadFolder.mkdirs();
                    Log.d(TAG, "downloadFolder is created: " + downloadFolder.toString());
                    Log.d(TAG, "folderExist: " + String.valueOf(folderExist));
                }
                if (downloadFolderExist) {
                    if (downloadFolder.canWrite() || downloadFolder.setWritable(true, true)) {
                        downloadFolderExist = true;
                    } else {
                        downloadFolderExist = false;
                        showToast("Sorry, cannot download, because your download directory: " + shortDownloadDirectory + " is not writable.");
                    }
                } else {
                    downloadFolderExist = false;
                    showToast("Sorry, cannot download, because your download directory: " + shortDownloadDirectory + " cannot be created.");
                }
            } else {
                downloadFolderExist = false;
                showToast("Sorry, cannot download, because your download directory: " + shortDownloadDirectory + " is not writable.");
            }
        } else {
            downloadFolderExist = false;
            showToast("Sorry, cannot download, because your download directory: " + shortDownloadDirectory + " cannot be created.");
        }
        return downloadFolderExist;
    }

    @Override
    protected void onPreExecute() {
        downloadDialog = new ProgressDialog(activity);
        if (!isDoStream) {
            downloadDialog.setMessage("Downloading " + assetFullName + ".. Please wait..");
        } else {
            downloadDialog.setMessage("Loading " + assetFullName + ".. Please wait..");
        }
        downloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //downloadDialog.setMax(100);
        downloadDialog.setProgressNumberFormat("0 MB/s");
        downloadDialog.setIndeterminate(false);
        downloadDialog.setCancelable(false);
        downloadDialog.show();
        folderExist = setDownloadFolder();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Bitmap doInBackground(Object... arg0) {
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
        if (isDoStream) {
            req += "&dostream=1";
            url = req;
        }

        if (folderExist) {
            downloadedFile = new File(downloadFolder, assetFullName);
            Log.d(TAG, "downloadedFile: " + downloadedFile.toString());
            InputStream inputStream = null;
            OutputStream fileOutput = null;
            try {
                URL url = new URL(req);
                Log.d(TAG, "URL: " + req);
                long startTime = System.nanoTime();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestMethod("GET");
//                connection.setFixedLengthStreamingMode(bytes.length);
                connection.setDoOutput(true);
                connection.connect();

                //get data from internet
                inputStream = new BufferedInputStream(connection.getInputStream());

                if (isDoStream) {
                    isDownloadSucessful = true;
                    int requiredImageWidth = downloadAssetListener.getRequiredImageWidth();
                    int requiredImageHeight = downloadAssetListener.getRequiredImageHeight();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(inputStream, null, options);
                    options.inSampleSize = calculateInSampleSize(options, requiredImageWidth, requiredImageHeight);
                    options.inJustDecodeBounds = false;
                    inputStream.close();
                    //re-get the stream
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    connection.setRequestMethod("GET");
                    //connection.setFixedLengthStreamingMode(bytes.length);
                    connection.setDoOutput(true);
                    connection.connect();
                    //get data from internet
                    inputStream = new BufferedInputStream(connection.getInputStream());
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                    inputStream.close();
                    return bitmap;
                }
                //get downloaded data
                //FileOutputStream fileOutput = new FileOutputStream (downloadLocation);
                fileOutput = new FileOutputStream(downloadedFile, false);
                //total size of the file
                int totalSize = connection.getContentLength();
                //variable to store total downloaded bytes
                int downloadedSize = 0;
                String unit = "";
                double newSpeed = 0.00;
                //create buffer...
                byte[] buffer = new byte[1024];
                int bufferLength = 0;//used to store a temporary size of the buffer

                long elapsedTime = System.nanoTime() - startTime;
                //final double downloadSpeedPerSec = 1000000000.00;
                Log.d(TAG, "inputStream: " + inputStream.toString());

                //now, read through the input buffer and write the contents to the file
                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    //add the data in the buffer to the file in the file output stream (the file on the sd card
                    fileOutput.write(buffer, 0, bufferLength);
                    //add up the size so we know how much is downloaded
                    downloadedSize += bufferLength;
                    //this is where you would do something to report the prgress, like this maybe
//                    updateProgress(downloadedSize, totalSize);

                    double speed = bufferLength * 1000000000.000f / elapsedTime;

                    if (speed > 1024.00 * 1024.00) {
                        newSpeed = Double.parseDouble(formatter.format(speed / (1024.00 * 1024.00)));
                        unit = " MB/s";
                    } else if ((speed > 1024.00) && (speed <= 1024.00 * 1024.00)) {
                        newSpeed = Double.parseDouble(formatter.format(speed / 1024.00));
                        unit = " kB/s";
                    } else {
                        newSpeed = Double.parseDouble(formatter.format(speed));
                        unit = " bytes/s";
                    }

                    downloadDialog.setProgressNumberFormat(newSpeed + unit);
                    //increase from 0-100%
                    int progress = (downloadedSize * 100 / totalSize);
                    if (progress >= 0) {
                        publishProgress("" + (downloadedSize * 100 / totalSize));
                    } else {
                        publishProgress("" + (100 + progress));
                    }
                }

                if (downloadedSize == totalSize) isDownloadSucessful = true;
                else isDownloadSucessful = false;

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
    protected void onProgressUpdate(String... progress) {
        //super.onProgressUpdate(progress);
        if (null != downloadDialog) {
//            progressInt = Integer.parseInt(progress[0]);
            downloadDialog.setProgress(Integer.parseInt(progress[0]));
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPostExecute(Bitmap result) {
        if (null != downloadDialog) {
            downloadDialog.dismiss();
        }
        Log.d(TAG, "downloaded to " + downloadFolder.toString());

        if (folderExist) { //to avoid double error msg
            if (isDownloadSucessful) {
                if (!isDoStream) {
                    AlertDialog downloadComplete = new AlertDialog.Builder(activity).create();
                    downloadComplete.setTitle("Download Status");
                    downloadComplete.setMessage(assetFullName + " is downloaded to directory " + shortDownloadDirectory + "/AMPS successfully.");
                    downloadComplete.setButton("OK", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub

                        }
                    });
                    downloadComplete.show();

                    //make sure the download file appear in the file system immediately
                    MediaScannerConnection.scanFile(activity,
                            new String[]{downloadedFile.toString()}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("ExternalStorage", "Scanned " + path + ":");
                                    Log.i("ExternalStorage", "-> uri=" + uri);
                                }
                            });
                } else {
                    downloadAssetListener.onDownloadAssetReady(result);
                }
            } else {
                if (!isDoStream) {
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
                } else {
                    AlertDialog downloadError = new AlertDialog.Builder(activity).create();
                    downloadError.setTitle("Preview Status");
                    downloadError.setMessage("Failed to preview " + assetFullName + ".. Please try again later..");
                    downloadError.setButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //  TODO Auto-generated method stub

                        }
                    });
                    downloadError.show();
                }
            }
        } else {
            //do nothing
        }
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public void showToast(String info) {
        Toast toast = Toast.makeText(
                activity,
                info,
                Toast.LENGTH_SHORT);
        toast.show();
    }

}
