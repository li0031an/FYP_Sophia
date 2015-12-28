package com.example.amps.fyp_amps_android_yishan;

import android.util.Log;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

//@TargetApi(19)
public class AssetUploadActivity extends Activity implements Settings {
    private static String TAG = "AssetUploadActivity";
    private static final int PICK_IMAGE = 1;
    private static final int MAX_CONTENT_SIZE = 1024; //1024byte = 1KB
    private String project_id, folder_id;
    private String asset_id;
    private String new_asset_id;
    private String assetFullName;
    private String latest_revid;
    ProgressDialog dialog;
    ProgressDialog anotherDialog;
    private Uri imageUri;
    private String selectedImagePath;
    private int numberOfChunks;
    boolean isNewRevision;
    ImageView imageUploaded;
    EditText editTextDescription;
    DecimalFormat formatter = new DecimalFormat("#.##");
    int fileSize = 0;
    double totalNetworkBytes = 0.00;
    int currentChunkNo = 1;
    ArrayList<Bitmap> chunkedImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_upload);
        setTitle("Upload Asset");
        imageUploaded = (ImageView) findViewById(R.id.fileUploaded);
        editTextDescription = (EditText) findViewById(R.id.revision_desc);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            asset_id = extras.getString("asset_id");
            project_id = extras.getString("project_id");
            folder_id = extras.getString("folder_id");
            isNewRevision = extras.getBoolean("isNewRevision");
            latest_revid = extras.getString("latest_revid");
            Log.d(TAG, "folder_id: GET: "+folder_id);
            Log.d(TAG, "isNewRevision: " + String.valueOf(isNewRevision));
            Log.d(TAG, "latest_revid: " + latest_revid);
            assetFullName = extras.getString("assetFullName");
        }
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_base, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE) {
                Uri selectedImageUri = data.getData();
                imageUri = selectedImageUri;
                selectedImagePath = getPath(selectedImageUri);
                if (readImageFileIntoChunk() > 1) {
                    Bitmap bit = decodeSampleBitmapFromLargeImage(selectedImagePath);
                    imageUploaded.setImageBitmap(bit);
                } else {
                    imageUploaded.setImageURI(imageUri);
                }
                //Try to create another image file as png inside a cache
                int rotateImage = getCameraPhotoOrientation(
                        AssetUploadActivity.this, selectedImageUri,
                        selectedImagePath);

                imageUploaded.setAdjustViewBounds(true);

                Matrix matrix = new Matrix();
                imageUploaded.setScaleType(ImageView.ScaleType.MATRIX); // required
//                matrix.postRotate((float) rotateImage, imageUploaded
//                        .getDrawable().getBounds().width() / 2, imageUploaded
//                        .getDrawable().getBounds().height() / 2);
                matrix.postRotate((float) rotateImage, imageUploaded
                        .getDrawable().getBounds().width(), imageUploaded
                        .getDrawable().getBounds().height());
                imageUploaded.setImageMatrix(matrix);

            }
        }
    }

    public static Bitmap decodeSampleBitmapFromLargeImage(String imagePath) {
        int ample_size = 16;
        // change ample_size to 32 or any power of 2 to increase or decrease bitmap size of image

        Bitmap bitmap = null;
        BitmapFactory.Options bitoption = new BitmapFactory.Options();
        bitoption.inSampleSize = ample_size;

        Bitmap bitmapPhoto = BitmapFactory.decodeFile(imagePath, bitoption);

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            // Auto-generated catch block
            e.printStackTrace();
        }
        int orientation = exif
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
        Matrix matrix = new Matrix();

        if ((orientation == 3)) {
            matrix.postRotate(180);
            bitmap = Bitmap.createBitmap(bitmapPhoto, 0, 0,
                    bitmapPhoto.getWidth(), bitmapPhoto.getHeight(), matrix,
                    true);

        } else if (orientation == 6) {
            matrix.postRotate(90);
            bitmap = Bitmap.createBitmap(bitmapPhoto, 0, 0,
                    bitmapPhoto.getWidth(), bitmapPhoto.getHeight(), matrix,
                    true);

        } else if (orientation == 8) {
            matrix.postRotate(270);
            bitmap = Bitmap.createBitmap(bitmapPhoto, 0, 0,
                    bitmapPhoto.getWidth(), bitmapPhoto.getHeight(), matrix,
                    true);

        } else {
            matrix.postRotate(0);
            bitmap = Bitmap.createBitmap(bitmapPhoto, 0, 0,
                    bitmapPhoto.getWidth(), bitmapPhoto.getHeight(), matrix,
                    true);

        }

        return bitmap;
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public int getCameraPhotoOrientation(Context context, Uri imageUri,
                                         String imagePath) {
        int rotate = 0;
        try {
            context.getContentResolver().notifyChange(imageUri, null);
            File imageFile = new File(imagePath);

            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                default:
                    rotate = 0;
                    break;
            }
            Log.i("RotateImage", "Exif orientation: " + orientation);
            Log.i("RotateImage", "Rotate value: " + rotate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public void onClick(View view) {
        try {
            if ((imageUploaded.getDrawable() == null)
                    && (editTextDescription.getText().toString().isEmpty())) {
                Toast toast = Toast
                        .makeText(
                                AssetUploadActivity.this,
                                "Upload fail. Please upload an image and enter a description.",
                                Toast.LENGTH_LONG);
                toast.show();
            } else if (imageUploaded.getDrawable() == null) {
                Toast toast = Toast.makeText(AssetUploadActivity.this,
                        "Upload fail. Please upload an image.",
                        Toast.LENGTH_LONG);
                toast.show();
            } else {
                int numberOfChunks = readImageFileIntoChunk();
                if (numberOfChunks > 1) {
                    System.out.println(Math.sqrt(numberOfChunks));

                    if ((numberOfChunks > 19) && (numberOfChunks < 30)) {
                        numberOfChunks = 16;
                    }
                    else if ((numberOfChunks > 9) && (numberOfChunks < 20)) {
                        numberOfChunks = 9;
                    }
                    else if ((numberOfChunks > 2) && (numberOfChunks < 10)) {
                        numberOfChunks = 4;
                    }
                    UploadAsset task = new UploadAsset();
                    task.execute();
                } else if (numberOfChunks == 1) {
                    UploadAsset task = new UploadAsset();
                    task.execute();
                } else {
                    Log.e(TAG, "readImageFileIntoChunk() returns 0.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int readImageFileIntoChunk() {
        if (0 < numberOfChunks) return numberOfChunks;
        try {

            InputStream inputStream = new BufferedInputStream(new FileInputStream(
                    selectedImagePath));

            fileSize = inputStream.available();
            Log.d(TAG, "fileSize: " + fileSize);
            if (fileSize > MAX_CONTENT_SIZE) {
                numberOfChunks = (fileSize / MAX_CONTENT_SIZE);
            } else numberOfChunks = 1;
            Log.d(TAG, "fileSize: " + fileSize);
            Log.d(TAG, "numberOfChunks: " + numberOfChunks);
            return numberOfChunks;
        } catch (Exception e) {
            Log.e(TAG, "IOException in readImageFileIntoChunk");
            e.printStackTrace();
        } return 0;

    }

    public class UploadAsset extends AsyncTask<Object, String, Object> {
        //        Handler updateBarHandler;

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "UploadAsset starts");
            if(numberOfChunks == 1){
                dialog = new ProgressDialog(AssetUploadActivity.this);
                dialog.setCancelable(false);
                dialog.setMessage("Uploading Asset (1 of 1)...");
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog.setProgressNumberFormat("0 MB/s");
                dialog.show();
            }
            else{
                dialog = new ProgressDialog(AssetUploadActivity.this);
                dialog.setCancelable(false);
                dialog.setMessage("Uploading Asset (" + (currentChunkNo + 1) + " of " + numberOfChunks + ")...");
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog.setProgressNumberFormat("0 MB/s");
                dialog.show();
            }
        }

        @Override
        protected String doInBackground(Object... arg0) {
            if (numberOfChunks == 1) {
                return uploadFile();
            } else {
                String response = uploadMultipleChunks();
                return response;
            }
        }
        protected void onProgressUpdate(String... progress){
            dialog.setProgress(Integer.parseInt(progress[0]));
        }
        @Override
        protected void onPostExecute(Object result) {
             dialog.dismiss();
            if (numberOfChunks == 1) {
                parseJSONResponse((String) result);
            } else {
                parseJSONChunksResponse((String) result);
            }
        }

        public String uploadFile() {
            String sResponse = "";
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(SZAAPIURL + "uploadChunk");

                MultipartEntity entity = new MultipartEntity(
                        HttpMultipartMode.BROWSER_COMPATIBLE);
                SharedPreferences settings = getSharedPreferences(SETTINGS, 0);

                entity.addPart(new FormBodyPart("tokenid", new StringBody(
                        settings.getString("tokenid", null))));
                Log.d(TAG, "tokenid: " + settings.getString("tokenid", null));
                entity.addPart(new FormBodyPart("userid", new StringBody(
                        settings.getString("userid", null))));
                Log.d(TAG, "userid: " + settings.getString("userid", null));
                entity.addPart(new FormBodyPart("projectid", new StringBody(
                        project_id)));
                Log.d(TAG, "projectid: " + project_id);
                if (null != folder_id) {
                    entity.addPart(new FormBodyPart("folderid", new StringBody(
                            folder_id)));
                    Log.d(TAG, "folderid: " + folder_id);
                }
                if (false == isNewRevision) {
                    entity.addPart(new FormBodyPart("resumableFilename",
                            new StringBody(
                                    selectedImagePath.substring(selectedImagePath
                                            .lastIndexOf("/") + 1))));
                    String resumableIdentifier = String.valueOf(fileSize) + "_"+selectedImagePath.substring(selectedImagePath
                            .lastIndexOf("/") + 1);
                    Log.d(TAG, "resumableIdentifier: " + resumableIdentifier);
                    entity.addPart(new FormBodyPart("resumableIdentifier",
                            new StringBody(resumableIdentifier)));
                    Log.d(TAG, "resumableFilename: " + selectedImagePath.substring(selectedImagePath
                            .lastIndexOf("/") + 1));
                } else {
                    entity.addPart(new FormBodyPart("resumableFilename",
                            new StringBody(assetFullName)));
                    String resumableIdentifier = String.valueOf(fileSize) + "_"+assetFullName;
                    Log.d(TAG, "resumableIdentifier: " + resumableIdentifier);
                    entity.addPart(new FormBodyPart("resumableIdentifier",
                            new StringBody(resumableIdentifier)));
                    Log.d(TAG, "resumableFilename: " + assetFullName);
                }
                entity.addPart(new FormBodyPart("fileSize",
                        new StringBody(String.valueOf(fileSize))));
                Log.d(TAG, "fileSize: " + fileSize);
                entity.addPart(new FormBodyPart("resumableChunkNumber",
                        new StringBody("1")));
                Log.d(TAG, "resumableChunkNumber: "+1);

                InputStream inputStream = new BufferedInputStream(new FileInputStream(
                        selectedImagePath));
                Log.d(TAG, "InputStream fileInputStream = new FileInputStream(selectedImagePath);");
                Log.d(TAG, "InputStream size byte: "+ inputStream.available());
                long startTime = System.nanoTime();
                int bytesAvailable = fileSize;
                byte[] buffer = new byte[1024];
                long sentBytes=0;
                String unit = "";
                double newSpeed = 0.00;
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();;
                // read file and write it into form...
                int count;
                while ((count = inputStream.read(buffer)) != -1){
                    // Update progress dialog
                    byteArrayOutputStream.write(buffer, 0, count);
                    sentBytes += count;
                    //calculate speed
                    long difference = sentBytes - count;
                    long elapsedTime = System.nanoTime() - startTime;
                    double speed = (sentBytes * 1000000000.000f/ elapsedTime);
                    System.out.println("Bytes send/seconds: " + difference);
                    if (speed > 1000.00 * 1000.00) {
                        newSpeed = Double.parseDouble(formatter.format(speed / (1000.00 * 1000.00)));
                        unit = " MB/s";
                    } else if ((speed > 1000.00) && (speed <= 1000.00 * 1000.00)){
                        newSpeed = Double.parseDouble(formatter.format(speed / 1000.00));
                        unit = " kB/s";
                    } else {
                        newSpeed = Double.parseDouble(formatter.format(speed));
                        unit = " bytes/s";
                    }

                    dialog.setProgressNumberFormat(newSpeed + unit);
                    //increase from 0-100%
                    publishProgress("" + (int)(sentBytes * 100 / bytesAvailable));
                    System.out.println("Elpased Time: " + elapsedTime);
                    System.out.println("Speed: " + speed);
                    System.out.println("New Speed: " + newSpeed);
                    System.out.println("Progress: " + (sentBytes * 100 / bytesAvailable));
                    System.out.println("Bytes Available: " + bytesAvailable);
                    bytesAvailable = inputStream.available();
                }
                entity.addPart(new FormBodyPart("file", new ByteArrayBody(byteArrayOutputStream.toByteArray(),
                        "blob")));

                httpPost.setEntity(entity);

                HttpResponse response = httpClient.execute(httpPost);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                response.getEntity().getContent(), "UTF-8"));

                sResponse = reader.readLine();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d(TAG, "sResponse: "+sResponse);
            return sResponse;
        }

        public String uploadMultipleChunks() {
            String sResponse = "";
            boolean isLast;
            try {
                Boolean isSuccess = true;
                String response = "";
                InputStream fileInputStream = new BufferedInputStream(new FileInputStream(new File(
                        selectedImagePath)));
                for (int resumableChunkNumber = 1; resumableChunkNumber < numberOfChunks; resumableChunkNumber++) {
                    if (isSuccess) {
                        if (resumableChunkNumber == numberOfChunks - 1)
                            isLast = true;
                        else
                            isLast = false;
                        Log.d(TAG, resumableChunkNumber + "-isLast: " + isLast);
                    } else break;

                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(SZAAPIURL + "uploadChunk");

                    MultipartEntity entity = new MultipartEntity(
                            HttpMultipartMode.BROWSER_COMPATIBLE);
                    SharedPreferences settings = getSharedPreferences(SETTINGS, 0);

                    entity.addPart(new FormBodyPart("resumableChunkNumber", new StringBody(
                            String.valueOf(resumableChunkNumber))));

                    int resumableChunkSize = MAX_CONTENT_SIZE ;
                    entity.addPart(new FormBodyPart("resumableChunkSize", new StringBody(
                            String.valueOf(resumableChunkSize))));

                    int resumableCurrentChunkSize = resumableChunkSize;
                    if (isLast) {
                        resumableCurrentChunkSize = fileSize - resumableChunkSize * (resumableChunkNumber - 1);
                    }
                    entity.addPart(new FormBodyPart("resumableCurrentChunkSize", new StringBody(
                            String.valueOf(resumableCurrentChunkSize))));

                    entity.addPart(new FormBodyPart("resumableTotalSize", new StringBody(
                            String.valueOf(fileSize))));

                    entity.addPart(new FormBodyPart("tokenid", new StringBody(
                            settings.getString("tokenid", null))));
                    Log.d(TAG, "tokenid: " + settings.getString("tokenid", null));
                    entity.addPart(new FormBodyPart("userid", new StringBody(
                            settings.getString("userid", null))));
                    Log.d(TAG, "userid: " + settings.getString("userid", null));
                    entity.addPart(new FormBodyPart("projectid", new StringBody(
                            project_id)));
                    Log.d(TAG, "projectid: " + project_id);
                    if (null != folder_id) {
                        entity.addPart(new FormBodyPart("folderid", new StringBody(
                                folder_id)));
                        Log.d(TAG, "folderid: " + folder_id);
                    }
                    if (false == isNewRevision) {
                        entity.addPart(new FormBodyPart("resumableFilename",
                                new StringBody(
                                        selectedImagePath.substring(selectedImagePath
                                                .lastIndexOf("/") + 1))));
                        String resumableIdentifier = String.valueOf(fileSize) + "_" + selectedImagePath.substring(selectedImagePath
                                .lastIndexOf("/") + 1);
                        Log.d(TAG, "resumableIdentifier: " + resumableIdentifier);
                        entity.addPart(new FormBodyPart("resumableIdentifier",
                                new StringBody(resumableIdentifier)));
                        Log.d(TAG, "resumableFilename: " + selectedImagePath.substring(selectedImagePath
                                .lastIndexOf("/") + 1));
                        entity.addPart(new FormBodyPart("resumableRelativePath",
                                new StringBody(resumableIdentifier)));
                        Log.d(TAG, "resumableRelativePath: " + selectedImagePath.substring(selectedImagePath
                                .lastIndexOf("/") + 1));
                    } else {
                        entity.addPart(new FormBodyPart("resumableFilename",
                                new StringBody(assetFullName)));
                        String resumableIdentifier = String.valueOf(fileSize) + "_" + assetFullName;
                        Log.d(TAG, "resumableIdentifier: " + resumableIdentifier);
                        entity.addPart(new FormBodyPart("resumableIdentifier",
                                new StringBody(resumableIdentifier)));
                        Log.d(TAG, "resumableFilename: " + assetFullName);
                        entity.addPart(new FormBodyPart("resumableRelativePath",
                                new StringBody(resumableIdentifier)));
                        Log.d(TAG, "resumableRelativePath: " + assetFullName);
                    }

                    entity.addPart(new FormBodyPart("fileSize",
                            new StringBody(String.valueOf(fileSize))));
                    Log.d(TAG, "fileSize: " + fileSize);

                    entity.addPart(new FormBodyPart("resumableChunkNumber",
                            new StringBody("1")));
                    Log.d(TAG, "resumableChunkNumber: " + 1);
                    long startTime = System.nanoTime();
                    int bytesAvailable = fileSize;
                    int bufferSize = resumableCurrentChunkSize;
                    byte[] buffer = new byte[bufferSize];
                    long sentBytes = 0;
                    String unit = "";
                    double newSpeed = 0.00;
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                    // read file and write it into form...
                    int startRead=(resumableChunkNumber-1)*resumableChunkSize;
                    int endRead = resumableCurrentChunkSize + startRead;
                    int bytesRead;

                    try {
                        fileInputStream.skip(resumableChunkSize * (resumableChunkNumber - 1));
                        Log.d(TAG, "skip unit: " + resumableChunkSize*(resumableChunkNumber-1));
//                        while ((bytesRead = fileInputStream.read(buffer, 0, bufferSize)) != -1) {
//                    int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                        int bytesReadAccum = 0;
                        while (bytesReadAccum < bufferSize) {
                            bytesRead = fileInputStream.read(buffer, 0, bufferSize - bytesReadAccum);
                            bytesReadAccum = bytesReadAccum + bytesRead;
                            Log.d(TAG, "bytesRead: " + bytesRead);
                            while (bytesRead > 0) {
                                try {
                                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                                } catch (OutOfMemoryError e) {
                                    e.printStackTrace();
                                    response = "outofmemoryerror";
                                    return response;
                                }
                                bytesAvailable = fileInputStream.available();
                                Log.d(TAG, "bytesAvailable: " + bytesAvailable);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        response = "";
                        return response;
                    }
//                        String lineEnd = "\r\n";
//                        String twoHyphens = "--";
//                        String boundary = "*****";
//                        byteArrayOutputStream.write(lineEnd);
//                        byteArrayOutputStream.write(twoHyphens + boundary + twoHyphens
//                                + lineEnd);

//                    startRead += bytesRead;
                    sentBytes += bufferSize;
                    //calculate speed
                    long difference = sentBytes - startRead;
                    ;
                    long elapsedTime = System.nanoTime() - startTime;
                    double speed = (sentBytes * 1000000000.000f / elapsedTime);
                    System.out.println("Bytes send/seconds: " + difference);
                    if (speed > 1000.00 * 1000.00) {
                        newSpeed = Double.parseDouble(formatter.format(speed / (1000.00 * 1000.00)));
                        unit = " MB/s";
                    } else if ((speed > 1000.00) && (speed <= 1000.00 * 1000.00)) {
                        newSpeed = Double.parseDouble(formatter.format(speed / 1000.00));
                        unit = " kB/s";
                    } else {
                        newSpeed = Double.parseDouble(formatter.format(speed));
                        unit = " bytes/s";
                    }

                    dialog.setProgressNumberFormat(newSpeed + unit);
                    //increase from 0-100%
                    publishProgress("" + (int) (sentBytes * 100 / bytesAvailable));
                    System.out.println("Elpased Time: " + elapsedTime);
                    System.out.println("Speed: " + speed);
                    System.out.println("New Speed: " + newSpeed);
                    System.out.println("Progress: " + (sentBytes * 100 / bytesAvailable));
                    System.out.println("Bytes Available: " + bytesAvailable);
                    bytesAvailable = fileInputStream.available();

                    entity.addPart(new FormBodyPart("file", new ByteArrayBody(byteArrayOutputStream.toByteArray(),
                            "blob")));

                    httpPost.setEntity(entity);

                    HttpResponse responseBody = httpClient.execute(httpPost);
                    isSuccess = parseJSONResponse(responseBody.toString());
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(
                                    responseBody.getEntity().getContent(), "UTF-8"));

                    sResponse = reader.readLine();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return sResponse;
        }

        public boolean parseJSONResponse(String responseBody) {
            JSONArray json;
            Boolean success = false;
            try {
                json = new JSONArray(responseBody);
                JSONObject job = json.getJSONObject(0);
                Log.d(TAG, responseBody);
                int error_code = job.getInt("error_code");
                dialog.dismiss();
                if (error_code == 0) {
                    success = true;
                    FinalizeAsset task = new FinalizeAsset(numberOfChunks);
                    task.execute();
                } else {
                    success = false;
                    Toast toast = Toast.makeText(AssetUploadActivity.this,
                            "Upload fail. Please upload again.",
                            Toast.LENGTH_LONG);
                    toast.show();
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                success = false;
                Log.d("className", e.getMessage());
                Toast toast = Toast.makeText(AssetUploadActivity.this,
                        "Upload fail. Please upload again.", Toast.LENGTH_LONG);
                toast.show();
                e.printStackTrace();
            }
            return success;
        }

        public void parseJSONChunksResponse(String responseBody) {
            JSONArray json;
            try {
                json = new JSONArray(responseBody);
                JSONObject job = json.getJSONObject(0);
                Log.d(TAG, responseBody);
                int error_code = job.getInt("error_code");
                dialog.dismiss();
                if(!dialog.isShowing()){
                    if (error_code == 0) {
                        if(currentChunkNo == numberOfChunks){
                            FinalizeAsset task = new FinalizeAsset(numberOfChunks);
                            task.execute();
                        }
                        else{
                            UploadAsset task = new UploadAsset();
                            currentChunkNo += 1;
                            task.execute();
                        }
                    } else {
                        Toast toast = Toast.makeText(AssetUploadActivity.this,
                                "Upload fail. Please upload again.",
                                Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                Log.d("className", e.getMessage());
                Toast toast = Toast.makeText(AssetUploadActivity.this,
                        "Upload fail. Please upload again.", Toast.LENGTH_LONG);
                toast.show();
                e.printStackTrace();
            }
        }
    }

    public class FinalizeAsset extends AsyncTask<Object, Object, Object> {
        int numberOfChunks;

        public FinalizeAsset(int numberOfChunks) {
            this.numberOfChunks = numberOfChunks;
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "FinalizeAsset starts");
            anotherDialog = new ProgressDialog(AssetUploadActivity.this);
            anotherDialog.setCancelable(true);
            anotherDialog.setMessage("Finalizing Asset ...");
            anotherDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            anotherDialog.setProgress(0);
            anotherDialog.setMax(100);
            anotherDialog.setProgressNumberFormat("");
            anotherDialog.show();
        }

        @Override
        protected String doInBackground(Object... arg0) {
            return finalizeUploadFile();
        }

        @Override
        protected void onPostExecute(Object result) {
            parseJSONResponse((String) result);
            anotherDialog.setProgress(100);
            anotherDialog.dismiss();

        }

        public String finalizeUploadFile() {
            String responseBody = "";
            // Instantiate an HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(SZAAPIURL + "finalizeV2");
            // Post parameters
            SharedPreferences settings = getSharedPreferences(SETTINGS, 0);
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("tokenid", settings
                    .getString("tokenid", null)));
            postParameters.add(new BasicNameValuePair("userid", settings
                    .getString("userid", null)));
            postParameters.add(new BasicNameValuePair("projectid", project_id));

            if (false == isNewRevision) {
                postParameters.add(new BasicNameValuePair("create_empty_asset", String.valueOf(1)));
            }
            if (false == isNewRevision) {
                postParameters.add(new BasicNameValuePair("folderid", folder_id));
            }
//            if (null != folder_id) {
//                postParameters.add(new BasicNameValuePair("folderid", folder_id));
//            }
            if (false == isNewRevision) {
                postParameters.add(new BasicNameValuePair("fileName",
                        selectedImagePath.substring(selectedImagePath
                                .lastIndexOf("/") + 1)));
                String resumableIdentifier = String.valueOf(fileSize) + "_"+(selectedImagePath.substring(selectedImagePath
                        .lastIndexOf("/") + 1));
                postParameters.add(new BasicNameValuePair("uniqueIdentifier", resumableIdentifier));
            } else {
                postParameters.add(new BasicNameValuePair("fileName", assetFullName));
                String resumableIdentifier = String.valueOf(fileSize) + "_"+assetFullName;
                postParameters.add(new BasicNameValuePair("uniqueIdentifier", resumableIdentifier));
            }
//            if (numberOfChunks == 1) {
//                postParameters.add(new BasicNameValuePair("chunkSize", String
//                        .valueOf(fileSize)));
//            } else {
            postParameters.add(new BasicNameValuePair("chunkSize", String
                    .valueOf(MAX_CONTENT_SIZE)));

//            }
            postParameters.add(new BasicNameValuePair("fileSize", String
                    .valueOf(fileSize)));

            Log.d(TAG, "FinalizeAsset: postParameters: "+postParameters);
            // Instantiate a POST HTTP method
            try {
                httppost.setEntity(new UrlEncodedFormEntity(postParameters));
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                responseBody = httpclient.execute(httppost, responseHandler);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return responseBody;
        }

        public void parseJSONResponse(String responseBody) {
            JSONArray json;
            JSONObject job;
            try {
                json = new JSONArray(responseBody);
                job = json.getJSONObject(0);
                Log.d(TAG, responseBody);
                String assetid = job.getJSONObject("data_array").getString(
                        "assetid");
                System.out.println(assetid);
                new_asset_id = assetid;
                if (true == isNewRevision) {
                    CreateRevision task = new CreateRevision();
                    task.execute();
                } else {
                    Toast toast = Toast.makeText(AssetUploadActivity.this,
                            "Upload successfully.", Toast.LENGTH_LONG);
                    toast.show();
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                Log.d("className", e.getMessage());
                Toast toast = Toast.makeText(AssetUploadActivity.this,
                        "Upload fail. Please upload again.", Toast.LENGTH_LONG);
                toast.show();
                e.printStackTrace();
            }
        }
    }

    public class GetNumberOfRevision extends AsyncTask<Object, Object, Object> {

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "GetNumberOfRevision starts");
            anotherDialog.setProgress(25);
        }

        @Override
        protected String doInBackground(Object... arg0) {
            return getNumberOfRevision();
        }

        @Override
        protected void onPostExecute(Object result) {
            parseJSONResponse((String) result);
        }

        public String getNumberOfRevision() {
            String responseBody = "";
            // Instantiate an HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(SZAAPIURL
                    + "getNumRevisionsOfAsset");
            // Post parameters
            SharedPreferences settings = getSharedPreferences(SETTINGS, 0);
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("tokenid", settings
                    .getString("tokenid", null)));
            postParameters.add(new BasicNameValuePair("userid", settings
                    .getString("userid", null)));
            postParameters.add(new BasicNameValuePair("projectid", project_id));
            if (isNewRevision) {
                postParameters.add(new BasicNameValuePair("assetid", asset_id));
            } else {
                postParameters.add(new BasicNameValuePair("assetid", new_asset_id));
            }

            // Instantiate a POST HTTP method
            try {
                httppost.setEntity(new UrlEncodedFormEntity(postParameters));
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                responseBody = httpclient.execute(httppost, responseHandler);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return responseBody;
        }

        public void parseJSONResponse(String responseBody) {
            JSONArray json;
            try {
                json = new JSONArray(responseBody);
                JSONObject job = json.getJSONObject(0);
                Log.d(TAG, responseBody);
                String revNum = job.getJSONObject("data_array").getString(
                        "num_rec");
                String latestRevNum = String
                        .valueOf(Integer.parseInt(revNum) - 1);
                System.out.println(latestRevNum);
                GetRevision task = new GetRevision(latestRevNum);
                task.execute();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                Log.d("className", e.getMessage());
                Toast toast = Toast.makeText(AssetUploadActivity.this,
                        "Upload fail. Please upload again.", Toast.LENGTH_LONG);
                toast.show();
                e.printStackTrace();
            }
        }
    }

    public class GetRevision extends AsyncTask<Object, Object, Object> {
        String revNum;

        public GetRevision(String revNum) {
            this.revNum = revNum;
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "GetRevision starts");
            anotherDialog.setProgress(50);
        }

        @Override
        protected String doInBackground(Object... arg0) {
            return getRevision();
        }

        @Override
        protected void onPostExecute(Object result) {
            parseJSONResponse((String) result);

        }

        public String getRevision() {
            String responseBody = "";
            // Instantiate an HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(SZAAPIURL + "getRevision");
            // Post parameters
            SharedPreferences settings = getSharedPreferences(SETTINGS, 0);
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("tokenid", settings
                    .getString("tokenid", null)));
            postParameters.add(new BasicNameValuePair("userid", settings
                    .getString("userid", null)));
            postParameters.add(new BasicNameValuePair("projectid", project_id));
            if (isNewRevision) {
                postParameters.add(new BasicNameValuePair("assetid", asset_id));
            } else {
                postParameters.add(new BasicNameValuePair("assetid", new_asset_id));
            }
            postParameters.add(new BasicNameValuePair("revnum", revNum));
            postParameters
                    .add(new BasicNameValuePair(
                            "select",
                            "[revid], [src_revid], [revnum], [iskey], [ismain], [name], [ext], [des], [checkin_userid], [checkin_datetime], [revsize]"));
            Log.d(TAG, "postParameters: " + postParameters.toString());
            // Instantiate a POST HTTP method
            try {
                httppost.setEntity(new UrlEncodedFormEntity(postParameters));
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                responseBody = httpclient.execute(httppost, responseHandler);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return responseBody;
        }

        public void parseJSONResponse(String responseBody) {
            JSONArray json, data_array;
            try {
                json = new JSONArray(responseBody);
                JSONObject job = json.getJSONObject(0);
                data_array = job.getJSONArray("data_array");
                JSONObject dataJob = new JSONObject(data_array.getString(0));
                String revId = dataJob.getString("revid");
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                Log.d("className", e.getMessage());
                Toast toast = Toast.makeText(AssetUploadActivity.this,
                        "Upload fail. Please upload again.", Toast.LENGTH_LONG);
                toast.show();
                e.printStackTrace();
            }
        }
    }

    public class CreateRevision extends AsyncTask<Object, Object, Object> {

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "CreateRevision starts");
            anotherDialog.setProgress(75);
        }

        @Override
        protected String doInBackground(Object... arg0) {
            return createRevision();
        }

        @Override
        protected void onPostExecute(Object result) {
            anotherDialog.setProgress(100);
            parseJSONResponse((String) result);

        }

        public String createRevision() {
            String responseBody = "";
            // Instantiate an HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(SZAAPIURL + "createRevision");
            // Post parameters
            SharedPreferences settings = getSharedPreferences(SETTINGS, 0);
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("tokenid", settings
                    .getString("tokenid", null)));
            postParameters.add(new BasicNameValuePair("userid", settings
                    .getString("userid", null)));
            postParameters.add(new BasicNameValuePair("projectid", project_id));
//            if (false == isNewRevision) {
//                postParameters.add(new BasicNameValuePair("folderid", folder_id));
//            }
            postParameters.add(new BasicNameValuePair("assetid", asset_id));
            // put the previous resvision id
            postParameters.add(new BasicNameValuePair("src_revid", latest_revid));
            postParameters
                    .add(new BasicNameValuePair("des_revid", new_asset_id));
            postParameters.add(new BasicNameValuePair("des_revfilename",
                    selectedImagePath.substring(selectedImagePath
                            .lastIndexOf("/") + 1)));
            postParameters.add(new BasicNameValuePair("ismain", "1"));
            postParameters.add(new BasicNameValuePair("des",
                    editTextDescription.getText().toString()));
            Log.d(TAG, "postParameters: " + postParameters.toString());
            // Instantiate a POST HTTP method
            try {
                httppost.setEntity(new UrlEncodedFormEntity(postParameters));
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                responseBody = httpclient.execute(httppost, responseHandler);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return responseBody;
        }

        public void parseJSONResponse(String responseBody) {
            JSONArray json;
            try {
                json = new JSONArray(responseBody);
                JSONObject job = json.getJSONObject(0);
                Log.d(TAG, responseBody);
                int error_code = job.getInt("error_code");
                if (error_code == 0) {
                    GetNumberOfRevision task = new GetNumberOfRevision();
                    task.execute();
                    anotherDialog.dismiss();
//                    Intent intent = new Intent(AssetUploadActivity.this,
//                            WorkingAssetsListActivity.class);
//                    startActivity(intent);
                } else {
                    Toast toast = Toast.makeText(AssetUploadActivity.this,
                            "Upload fail. Please upload again.",
                            Toast.LENGTH_LONG);
                    toast.show();
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                Log.d("className", e.getMessage());
                e.printStackTrace();
            }
        }
    }
}