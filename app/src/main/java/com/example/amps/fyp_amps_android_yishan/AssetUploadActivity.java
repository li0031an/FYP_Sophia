package com.example.amps.fyp_amps_android_yishan;

import android.content.res.Resources;
import android.os.Environment;
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
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;

//@TargetApi(19)
public class AssetUploadActivity extends Activity implements Settings {
    private static String TAG = "AssetUploadActivity";
    private static final int PICK_IMAGE_VIDEO_RESULT_CODE = 10;
    private static final int PICK_FILE_RESULT_CODE = 11;
    private static final int MAX_CONTENT_SIZE = 1024 * 1024; //1024*1024byte = 1MB
    private static final String ERR_MSG_UPLOAD_FAIL = "Upload fail. Please upload again.";
    private static final String ERR_MSG_NO_IMAGE_SELECTED = "Upload fail. Please upload an image.";
    private static final String ERR_MSG_NO_IMAGE_NO_DES = "Upload fail. Please upload an image and enter a description.";
    private static final String MSG_SUCCESS = "Upload successfully.";
    private String project_id;
    private String folder_id;
    private String asset_id;
    private String new_asset_id;
    private String latest_revid;
    ProgressDialog dialog;
    private Uri uploadFileUri;
    private FileType uploadFileType;
    private String selectedFilePath;
    private String pickEnvironment = "";
    private int numberOfChunks;
    private int fileSize = 0;
    boolean isNewRevision;
    ImageView imageUploaded;
    EditText editTextDescription;
    DecimalFormat formatter = new DecimalFormat("#.##");
    public enum FileType {
        IMAGE,
        VIDEO,
        AUDIO,
        DOCUMENT,
        OTHER
    }


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
            pickEnvironment = extras.getString("environment");
            Log.d(TAG, "GET from intent folder_id: " + folder_id);
            Log.d(TAG, "GET from intent isNewRevision: " + String.valueOf(isNewRevision));
            Log.d(TAG, "GET from intent latest_revid: " + latest_revid);
            Log.d(TAG, "GET from intent pickEnvironment: " + pickEnvironment);
        }
        if (pickEnvironment.equalsIgnoreCase("Pictures")) startPickImageVideoIntent();
        else startPickFileIntent();
    }

    private void startPickImageVideoIntent() {
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("*/*");
        String[] mimetypes = {"image/*|video/*|audio/*"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
//        startActivityForResult(intent, PICKFILE_RESULT_CODE);
        startActivityForResult(Intent.createChooser(intent, "Select picture or video"),
                PICK_IMAGE_VIDEO_RESULT_CODE);
    }

    private void startPickFileIntent() {
        //        Intent intent = new Intent();
//        intent.setType("image/*|video/*");
////        intent.setType("image/*, audio/*, video/*");
//        intent.setAction(Intent.ACTION_PICK);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        String[] mimetypes = {"image/*|video/*|audio/*"};
//        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/*");
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/" + pickEnvironment + "/AMPS/*");
        Log.d(TAG, "Environment.getExternalStorageDirectory(): " + Environment.getExternalStorageDirectory());
        Log.d(TAG, "PATH: " + uri);
        intent.setData(uri);
        startActivityForResult(Intent.createChooser(intent, "Select " + pickEnvironment), PICK_FILE_RESULT_CODE);
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
            switch (requestCode) {
                case PICK_IMAGE_VIDEO_RESULT_CODE:
                case PICK_FILE_RESULT_CODE:
                    uploadFileUri = data.getData();
                    Log.d(TAG, "uri: " + uploadFileUri);
                    break;
                default:
                    Log.e(TAG, "requestCode is not valid: " + requestCode);
                    return;
            }
        }
        uploadFileUri = data.getData();
        Log.d(TAG, "uri: " + uploadFileUri);
        if (uploadFileUri.toString().contains("image")) uploadFileType = FileType.IMAGE;
        else if (uploadFileUri.toString().contains("video")) uploadFileType = FileType.VIDEO;
        else uploadFileType = FileType.OTHER;

        selectedFilePath = getPath(uploadFileUri);
        Log.d(TAG, "selectedFilePath: " + selectedFilePath);
        numberOfChunks = readImageFileIntoChunk();
        if (uploadFileType == FileType.IMAGE) {
            if (numberOfChunks > 1) {
                Bitmap bit = decodeSampleBitmapFromLargeImage(selectedFilePath);
                imageUploaded.setImageBitmap(bit);
            } else {
                imageUploaded.setImageURI(uploadFileUri);
            }
        } else {
            imageUploaded.setImageDrawable(getResources().getDrawable((R.mipmap.no_image)));
        }
        //Try to create another image file as png inside a cache
        int rotateImage = getCameraPhotoOrientation(
                AssetUploadActivity.this, uploadFileUri,
                selectedFilePath);

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
//                }

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

    //IMAGE:  uploadFileUri: content://media/external/images/media/9460
//        uploadFileUri: content://media/external/images/media/9164
//uri: content://media/external/video/media/9479
    public String getPath(Uri uri) {
        // Perform the query on the contact to get the MediaStore column
        // We don't need a selection or sort order (there's only one result for the given URI)
        // CAUTION: The query() method should be called from a separate thread to avoid blocking
        // your app's UI thread. (For simplicity of the sample, this code doesn't do that.)
        // Consider using CursorLoader to perform the query.
        Log.d(TAG, "uri: " + uri);
        String[] projection;
        switch (uploadFileType) {
            case IMAGE:
                projection = new String[] {MediaStore.Images.Media.DATA};
                break;
            case VIDEO:
                projection = new String[] {MediaStore.Video.Media.DATA};
                break;
            case AUDIO:
                projection = new String[] {MediaStore.Audio.Media.DATA};
                break;
            default: projection = new String[] {MediaStore.Files.FileColumns.DATA};
        }
//        projection ;
        Cursor cursor = AssetUploadActivity.this.getContentResolver().query(uri, projection, null, null, null);
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
        switch (view.getId()) {
            case R.id.btn_upload:
                checkUpload();
                break;
            case R.id.btn_reset:
                startPickFileIntent();
                editTextDescription.setText("");
                break;
            default: //do nothing;
        }
    }

    private void checkUpload() {
        try {
            if ((imageUploaded.getDrawable() == null)
                    && (editTextDescription.getText().toString().isEmpty())) {
                showToast(ERR_MSG_NO_IMAGE_NO_DES);
            } else if (imageUploaded.getDrawable() == null) {
                showToast(ERR_MSG_NO_IMAGE_SELECTED);
            } else {
                if (0 == numberOfChunks) {
                    numberOfChunks = readImageFileIntoChunk();
                }
                if (numberOfChunks > 0) {
                    UploadAsset task = new UploadAsset();
                    task.execute();
                } else {
                    Log.e(TAG, "numberOfChunks = readImageFileIntoChunk() returns 0 or less.");
                    Log.e(TAG, "numberOfChunks: " + numberOfChunks);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int readImageFileIntoChunk() {
        if (0 < numberOfChunks) {
            // do nothing
        } else {
            numberOfChunks = 0;
            InputStream inputStream = null;
            try {
                inputStream = new BufferedInputStream(new FileInputStream(
                        selectedFilePath));
                fileSize = inputStream.available();
                inputStream.close();
                Log.d(TAG, "fileSize: " + fileSize);
                if (fileSize > MAX_CONTENT_SIZE) {
                    numberOfChunks = (int) Math.floor((fileSize / MAX_CONTENT_SIZE));  //the last section is between [BUFFER_SIZE, 2*BUFFER_SIZE)
                } else if (fileSize < MAX_CONTENT_SIZE && fileSize > 0) {
                    numberOfChunks = 1;
                } else {
                    Log.e(TAG, "fileSize < 0, fileSize: " + fileSize);
                }
                Log.d(TAG, "numberOfChunks: " + numberOfChunks);
            } catch (Exception e) {
                Log.e(TAG, "IOException in readImageFileIntoChunk");
                e.printStackTrace();
            } finally {
                if (null != inputStream) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "inputStream cannot be closed.");
                        e.printStackTrace();
                    }
                }
            }
        }
        return numberOfChunks;
    }

    public class UploadAsset extends AsyncTask<Object, String, Object> {
        //        Handler updateBarHandler;

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "UploadAsset starts");
            dialog = new ProgressDialog(AssetUploadActivity.this);
            dialog.setCancelable(false);
            dialog.setMessage("Uploading Asset " + selectedFilePath + "...");
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setProgressNumberFormat("0 MB/s");
            dialog.show();
        }

        @Override
        protected String doInBackground(Object... arg0) {
//            if (numberOfChunks == 1) {
//                return uploadMultipleChunks();
//            } else {
//                return uploadMultipleChunks();
//            }
            // uploadMultipleChunks() function works for single or multiple chunks
            return uploadMultipleChunks();
        }

        protected void onProgressUpdate(String... progress) {
            dialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(Object result) {
            dialog.dismiss();

            boolean isSuccess = parseJSONResponse((String) result);
            if (isSuccess) {
                FinalizeAsset task = new FinalizeAsset(numberOfChunks);
                task.execute();
            } else {
                showToast(ERR_MSG_UPLOAD_FAIL);
                finish();
            }
        }

        public String uploadMultipleChunks() {
            String sResponse = "";
            boolean isLast;
            InputStream fileInputStream = null;
            ByteArrayOutputStream byteArrayOutputStream = null;
            try {
                Boolean isSuccess = true;
                fileInputStream = new BufferedInputStream(new FileInputStream(new File(
                        selectedFilePath)));
                for (int resumableChunkNumber = 1; resumableChunkNumber <= numberOfChunks; resumableChunkNumber++) {
                    if (isSuccess) {
                        if (resumableChunkNumber == numberOfChunks)
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
                    Log.d(TAG, "resumableChunkNumber: " + resumableChunkNumber);

                    int resumableChunkSize = MAX_CONTENT_SIZE;
                    entity.addPart(new FormBodyPart("resumableChunkSize", new StringBody(
                            String.valueOf(resumableChunkSize))));
                    Log.d(TAG, "resumableChunkSize: " + resumableChunkSize);

                    int resumableCurrentChunkSize = resumableChunkSize;
                    if (isLast) {
                        resumableCurrentChunkSize = fileSize - resumableChunkSize * (resumableChunkNumber - 1);
                    }
                    entity.addPart(new FormBodyPart("resumableCurrentChunkSize", new StringBody(
                            String.valueOf(resumableCurrentChunkSize))));
                    Log.d(TAG, "resumableCurrentChunkSize: " + resumableCurrentChunkSize);

                    entity.addPart(new FormBodyPart("resumableTotalSize", new StringBody(
                            String.valueOf(fileSize))));
                    Log.d(TAG, "resumableTotalSize: " + fileSize);

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

                    String resumableFilename = selectedFilePath.substring(selectedFilePath
                            .lastIndexOf("/") + 1);
                    entity.addPart(new FormBodyPart("resumableFilename",
                            new StringBody(resumableFilename)));
                    Log.d(TAG, "resumableFilename: " + resumableFilename);

                    String resumableIdentifier = String.valueOf(fileSize) + "_" + selectedFilePath.substring(selectedFilePath
                            .lastIndexOf("/") + 1);
                    Log.d(TAG, "resumableIdentifier: " + resumableIdentifier);
                    entity.addPart(new FormBodyPart("resumableIdentifier",
                            new StringBody(resumableIdentifier)));

                    entity.addPart(new FormBodyPart("resumableRelativePath",
                            new StringBody(resumableFilename)));
                    Log.d(TAG, "resumableRelativePath: " + resumableFilename);

                    entity.addPart(new FormBodyPart("fileSize",
                            new StringBody(String.valueOf(fileSize))));
                    Log.d(TAG, "fileSize: " + fileSize);

                    long startTime = System.nanoTime();
                    int bufferSize = resumableCurrentChunkSize;
                    byte[] buffer = new byte[bufferSize];
                    int startRead = (resumableChunkNumber - 1) * MAX_CONTENT_SIZE;
                    long sentBytes = startRead;
                    String unit = "";
                    double newSpeed = 0.00;
                    byteArrayOutputStream = new ByteArrayOutputStream();

                    // read file and write it into form...
                    int bytesReadAccum = 0;
                    int bytesRead = 0;
                    while (bytesReadAccum < bufferSize) {
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize - bytesReadAccum);
                        if (bytesRead == -1) break;
                        bytesReadAccum = bytesReadAccum + bytesRead;
                        Log.d(TAG, "resumableChunkNumber, bytesRead, bytesReadAccum, bufferSize: " + resumableChunkNumber + ", " + bytesRead + ", " + bytesReadAccum + ", " + bufferSize);
                        while (bytesRead > 0) {
                            try {
                                byteArrayOutputStream.write(buffer, 0, bytesRead);
                                bytesRead = 0;
                            } catch (OutOfMemoryError e) {
                                e.printStackTrace();
                                sResponse = "outofmemoryerror";
                            }
                        }
                    }

                    sentBytes += bufferSize;
                    long difference = sentBytes - startRead;
                    long elapsedTime = System.nanoTime() - startTime;
                    double speed = (bufferSize * 1000000000.000f / elapsedTime);
                    Log.d(TAG, "Bytes send/seconds: " + difference);
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
                    publishProgress("" + (int) (sentBytes * 100 / fileSize));
                    Log.d(TAG, "Elpased Time: " + elapsedTime);
                    Log.d(TAG, "Speed: " + speed);
                    Log.d(TAG, "New Speed: " + newSpeed);
                    Log.d(TAG, "Progress: " + (sentBytes * 100 / fileSize));
                    Log.d(TAG, "sentBytes: " + sentBytes);
                    Log.d(TAG, "fileSize: " + fileSize);

                    entity.addPart(new FormBodyPart("file", new ByteArrayBody(byteArrayOutputStream.toByteArray(),
                            "blob")));

                    httpPost.setEntity(entity);

                    HttpResponse responseBody = httpClient.execute(httpPost);
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(
                                    responseBody.getEntity().getContent(), "UTF-8"));
                    sResponse = reader.readLine();
                    isSuccess = parseJSONResponse(sResponse);
                    Log.d(TAG, "isSuccess: " + isSuccess);
                    Log.d(TAG, "sResponse: " + sResponse);
                    if (!isSuccess) return sResponse;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (null != fileInputStream) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "fileInputStream cannot be closed.");
                        e.printStackTrace();
                    }
                }
                if (null != byteArrayOutputStream) {
                    try {
                        byteArrayOutputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "byteArrayOutputStream cannot be closed.");
                        e.printStackTrace();
                    }
                }
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
                if (error_code == 0) {
                    success = true;
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                success = false;
                Log.d("className", e.getMessage());
            }
            return success;
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
            dialog = new ProgressDialog(AssetUploadActivity.this);
            dialog.setCancelable(true);
            dialog.setMessage("Finalizing Asset ...");
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setProgress(0);
            dialog.setMax(100);
            dialog.setProgressNumberFormat("");
            dialog.show();
        }

        @Override
        protected String doInBackground(Object... arg0) {
            return finalizeUploadFile();
        }

        @Override
        protected void onPostExecute(Object result) {
            parseJSONResponse((String) result);
            dialog.setProgress(100);
            dialog.dismiss();

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
            postParameters.add(new BasicNameValuePair("fileName",
                    selectedFilePath.substring(selectedFilePath
                            .lastIndexOf("/") + 1)));
            String resumableIdentifier = String.valueOf(fileSize) + "_" + (selectedFilePath.substring(selectedFilePath
                    .lastIndexOf("/") + 1));
            postParameters.add(new BasicNameValuePair("uniqueIdentifier", resumableIdentifier));

            postParameters.add(new BasicNameValuePair("chunkSize", String
                    .valueOf(MAX_CONTENT_SIZE)));

            postParameters.add(new BasicNameValuePair("fileSize", String
                    .valueOf(fileSize)));

            Log.d(TAG, "FinalizeAsset: postParameters: " + postParameters);
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
                    showToast(MSG_SUCCESS);
                    finish();
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                Log.d("className", e.getMessage());
                showToast(ERR_MSG_UPLOAD_FAIL);
                e.printStackTrace();
                finish();
            }
        }
    }

    public class GetNumberOfRevision extends AsyncTask<Object, Object, Object> {

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "GetNumberOfRevision starts");
            dialog.setProgress(25);
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
                Log.d(TAG, e.getMessage());
                showToast(ERR_MSG_UPLOAD_FAIL);
                e.printStackTrace();
                finish();
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
            dialog.setProgress(50);
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
                Log.d(TAG, e.getMessage());
                showToast(ERR_MSG_UPLOAD_FAIL);
                e.printStackTrace();
                finish();
            }
        }
    }

    public class CreateRevision extends AsyncTask<Object, Object, Object> {

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "CreateRevision starts");
            dialog.setProgress(75);
        }

        @Override
        protected String doInBackground(Object... arg0) {
            return createRevision();
        }

        @Override
        protected void onPostExecute(Object result) {
            dialog.setProgress(100);
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
                    selectedFilePath.substring(selectedFilePath
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
                    showToast(MSG_SUCCESS);
//                    Intent intent = new Intent(AssetUploadActivity.this,
//                            WorkingAssetsListActivity.class);
//                    startActivity(intent);
                } else {
                    showToast(ERR_MSG_UPLOAD_FAIL);
                    finish();
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                Log.d(TAG, e.getMessage());
                e.printStackTrace();
            }
            finish();
        }
    }

    public void showToast(String message) {
        Toast toast = Toast.makeText(AssetUploadActivity.this,
                message, Toast.LENGTH_SHORT);
        toast.show();
    }
}
