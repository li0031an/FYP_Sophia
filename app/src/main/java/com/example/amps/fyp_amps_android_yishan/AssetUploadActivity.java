package com.example.amps.fyp_amps_android_yishan;

import android.util.Log;
import android.annotation.TargetApi;
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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;

//@TargetApi(19)
public class AssetUploadActivity extends Activity implements Settings {
    private static String TAG = "AssetUploadActivity";
    private static final int PICK_IMAGE = 1;
    private String project_id, folder_id;
    private String asset_id;
    private String new_asset_id;
    private String assetFullName;
    private String latest_revid;
    ProgressDialog dialog;
    ProgressDialog anotherDialog;
    private Uri uri;
    private String selectedImagePath;
    boolean isNewRevision;
    ImageView imageUploaded;
    EditText editTextDescription;
    Bitmap thumbnail;
    DecimalFormat formatter = new DecimalFormat("#.##");
    int fileSize = 0;
    double totalNetworkBytes = 0.00;
    ByteArrayOutputStream stream;
    int currentChunkNo = 0;
    ArrayList<Bitmap> chunkedImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_asset_upload);
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
                uri = selectedImageUri;
                selectedImagePath = getPath(selectedImageUri);
                //Try to create another image file as png inside a cache
                int rotateImage = getCameraPhotoOrientation(
                        AssetUploadActivity.this, selectedImageUri,
                        selectedImagePath);

                Bitmap image = decodeSampledBitmapFromResource(selectedImagePath,250,150);
                //Bitmap.createScaledBitmap(image,200,200,true);
                Log.d(TAG, "image.getConfig(): " + image.getConfig());
                imageUploaded.setImageBitmap(image);
                imageUploaded.setAdjustViewBounds(true);

                int width = imageUploaded.getWidth();
                int height = imageUploaded.getHeight();
                //Bitmap newImage = decodeSampledBitmapFromResource(selectedImagePath,width,height);
                //imageUploaded.setImageBitmap(newImage);
                // imageUploaded.setImageURI(selectedImageUri);
                Matrix matrix = new Matrix();
                imageUploaded.setScaleType(ImageView.ScaleType.MATRIX); // required
                matrix.postRotate((float) rotateImage, imageUploaded
                        .getDrawable().getBounds().width() / 2, imageUploaded
                        .getDrawable().getBounds().height() / 2);
                imageUploaded.setImageMatrix(matrix);

            }
        }
    }


    public static Bitmap decodeSampledBitmapFromResource(String pathName,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inSampleSize = 1;
//        BitmapFactory.decodeFile(pathName, options);

        // Calculate inSampleSize
//        options.inSampleSize = calculateInSampleSie(options, reqWidth,
//                reqHeight);

        // Decode bitmap with inSampleSize set
//        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
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
            } else if ((imageUploaded.getDrawable() == null)
                    || (editTextDescription.getText().toString().isEmpty())) {
                if (imageUploaded.getDrawable() == null) {
                    Toast toast = Toast.makeText(AssetUploadActivity.this,
                            "Upload fail. Please upload an image.",
                            Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    Toast toast = Toast.makeText(AssetUploadActivity.this,
                            "Upload fail. Please enter a description.",
                            Toast.LENGTH_LONG);
                    toast.show();
                }
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
                    splitImage(imageUploaded, numberOfChunks);
                    UploadAsset task = new UploadAsset();
                    task.numberOfChunks = numberOfChunks;
                    task.execute();
                }
                else {
                    UploadAsset task = new UploadAsset();
                    task.numberOfChunks = 1;
                    chunkedImages = new ArrayList<Bitmap>(0);
                    task.execute();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int readImageFileIntoChunk() throws IOException {
        imageUploaded.buildDrawingCache();
        Bitmap bitmap = imageUploaded.getDrawingCache();
        stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        fileSize = stream.toByteArray().length;
        Log.d(TAG, "fileSize: " + fileSize);
        int numberOfChunks = 1;
        if (fileSize > 1  * 1024 * 1024) {
            fileSize = (int) Math.ceil(stream.size());
            numberOfChunks = (fileSize / (1 * 1024 * 1024)) + 1;
            numberOfChunks = (int) Math.ceil(Double.parseDouble(String
                    .valueOf(numberOfChunks)));
        }

        Log.d(TAG, "fileSize: " + fileSize);
        Log.d(TAG, "numberOfChunks: " + numberOfChunks);
        return numberOfChunks;
    }

    private void splitImage(ImageView image, int chunkNumbers) {

        // For the number of rows and columns of the grid to be displayed
        int rows, cols;

        // For height and width of the small image chunks
        int chunkHeight, chunkWidth;

        // To store all the small image chunks in bitmap format in this list
        chunkedImages = new ArrayList<Bitmap>(chunkNumbers);

        // Getting the scaled bitmap of the source image
        BitmapDrawable drawable = (BitmapDrawable) image.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,
                bitmap.getWidth(), bitmap.getHeight(), true);

        rows = cols = (int) Math.sqrt(chunkNumbers);
        chunkHeight = bitmap.getHeight() / rows;
        chunkWidth = bitmap.getWidth() / cols;

        // xCoord and yCoord are the pixel positions of the image chunks
        int yCoord = 0;
        for (int x = 0; x < rows; x++) {
            int xCoord = 0;
            for (int y = 0; y < cols; y++) {
                chunkedImages.add(Bitmap.createBitmap(scaledBitmap, xCoord,
                        yCoord, chunkWidth, chunkHeight));
                xCoord += chunkWidth;
            }
            yCoord += chunkHeight;
        }
        Log.d(TAG, "chunkedImages.size()" + chunkedImages.size());
    }

    public class UploadAsset extends AsyncTask<Object, String, Object> {
        Handler updateBarHandler;
        int numberOfChunks;
        int incrementalForMultipleChunks;

		/*public UploadAsset(int numberOfChunks) {
			this.numberOfChunks = numberOfChunks;
		}*/

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
                dialog.setMessage("Uploading Asset ("+ (currentChunkNo + 1)+" of " + numberOfChunks + ")...");
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
                return uploadMultipleChunks();
            }
        }
        protected void onProgressUpdate(String... progress){
//            Log.d("",progress[0]);
            //super.onProgressUpdate(progress);
            dialog.setProgress(Integer.parseInt(progress[0]));
        }
        @Override
        protected void onPostExecute(Object result) {
            // dialog.dismiss();
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
                byte[] data = stream.toByteArray();
//                Log.d(TAG, "data size + " + data.length);
                String base64 = Base64.encodeToString(data, Base64.CRLF);
                TrafficStats ts = new TrafficStats();
                totalNetworkBytes = ts.getTotalTxBytes();

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
                } if (false == isNewRevision) {
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

                /////TESTING
                entity.addPart(new FormBodyPart("fileSize",
                        new StringBody(String.valueOf(fileSize))));
                Log.d(TAG, "fileSize: " + fileSize);
                ////
                entity.addPart(new FormBodyPart("resumableChunkNumber",
                        new StringBody("1")));
                Log.d(TAG, "resumableChunkNumber: "+1);
                int count;
                FileInputStream fileInputStream = new FileInputStream(
                        selectedImagePath);

                long startTime = System.nanoTime();
                int bytesAvailable = fileSize;
                byte[] buffer = new byte[1024];
                long beforeSentBytes = 0;
                long sentBytes=0;
                String unit = "";
                double newSpeed = 0.00;
                // read file and write it into form...
                while ((count = fileInputStream.read(buffer)) != -1){
                    // Update progress dialog
                    sentBytes += count;
                    //calculate speed
                    long difference = sentBytes - count;
                    long elapsedTime = System.nanoTime() - startTime;
                    double speed = (sentBytes * 1000000000.000f/ elapsedTime);
                    System.out.println("Bytes send/seconds: " + difference);
                    if (speed > 1000.00 * 1000.00)
                    {
                        newSpeed = Double.parseDouble(formatter.format(speed / (1000.00 * 1000.00)));
                        unit = " MB/s";
                    }
                    else if ((speed > 1000.00) && (speed <= 1000.00 * 1000.00)){
                        newSpeed = Double.parseDouble(formatter.format(speed / 1000.00));
                        unit = " kB/s";
                    }
                    else
                    {
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
                    bytesAvailable = fileInputStream.available();
                }
                fileInputStream.close();

                Log.d(TAG, "data: " + data.toString());
//                entity.addPart(new FormBodyPart("file", new ByteArrayBody(data,
//                        selectedImagePath.substring(selectedImagePath
//                                .lastIndexOf("/") + 1))));
                entity.addPart(new FormBodyPart("file", new ByteArrayBody(data,
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
            try {
                long startTime = (System.currentTimeMillis() / 1000);
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(SZAAPIURL + "uploadChunk");

                MultipartEntity entity = new MultipartEntity(
                        HttpMultipartMode.BROWSER_COMPATIBLE);

                SharedPreferences settings = getSharedPreferences(SETTINGS, 0);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                Log.d(TAG, "currentChunkNo: " + currentChunkNo);
                Bitmap bitmap = chunkedImages.get(currentChunkNo);
                bitmap.compress(Bitmap.CompressFormat.JPEG,100, bos);
                byte[] data = bos.toByteArray();
                TrafficStats ts = new TrafficStats();
                totalNetworkBytes = ts.getTotalTxBytes();

                entity.addPart(new FormBodyPart("tokenid", new StringBody(settings.getString("tokenid", null))));
                Log.d(TAG, "projectid" + settings.getString("tokenid", null));
                entity.addPart(new FormBodyPart("userid", new StringBody(settings.getString("userid", null))));
                Log.d(TAG, "projectid" + settings.getString("userid", null));
                entity.addPart(new FormBodyPart("projectid", new StringBody(project_id)));
                Log.d(TAG,"projectid"+project_id);
                if (null != folder_id) {
                    entity.addPart(new FormBodyPart("folderid", new StringBody(
                            folder_id)));
                    Log.d(TAG, "folder_id"+folder_id);
                }
                String resumableFilename=selectedImagePath.substring(selectedImagePath.lastIndexOf("/") + 1);
                Log.d(TAG, "resumableFilename: "+resumableFilename);
                entity.addPart(new FormBodyPart("resumableFilename", new StringBody(resumableFilename)));
                String resumableIdentifier=fileSize+"-"+selectedImagePath.substring(selectedImagePath.lastIndexOf("/") + 1);
                Log.d(TAG, "resumableIdentifier: " + resumableIdentifier);
                entity.addPart(new FormBodyPart("resumableIdentifier", new StringBody(resumableIdentifier)));
                Log.d(TAG, "resumableChunkNumber: " + String.valueOf(currentChunkNo + 1));
                entity.addPart(new FormBodyPart("resumableChunkNumber",new StringBody(String.valueOf(currentChunkNo + 1))));
                Log.d(TAG, "in uploadMultipleChunks()");
                Log.d(TAG, "entity: " + entity.toString());

                int count;
                FileInputStream fileInputStream = new FileInputStream(
                        selectedImagePath);
                Log.d(TAG, "fileInputStream: "+ fileInputStream.toString());
                int bytesAvailable = fileSize;
                byte[] buffer = new byte[1024];
                long sentBytes=0;
                String unit = "";
                double newSpeed = 0.00;
                double seconds = 1.00;
                // read file and write it into form...
                while ((count = fileInputStream.read(buffer)) != -1){
                    // Update progress dialog
                    sentBytes += count;
                    //calculate speed
                    int difference = (int) (sentBytes - count);
                    long elapsedTime =(long) (((System.currentTimeMillis()/1000.00) - startTime) + seconds);
                    double speed = Math.random() * (difference * 1.0 / elapsedTime);
                    seconds += 1;

                    if (speed > 1000.00 * 1000.00)
                    {
                        newSpeed = Double.parseDouble(formatter.format(speed / 1000.00 * 1000.00));
                        unit = " MB/s";
                    }
                    else if ((speed > 1000.00) && (speed <= 1000.00 * 1000.00)){
                        newSpeed = Double.parseDouble(formatter.format(speed / 1000.00));
                        unit = " kB/s";
                    }
                    else
                    {
                        newSpeed = Double.parseDouble(formatter.format(speed));
                        unit = " bytes/s";
                    }

                    dialog.setProgressNumberFormat(newSpeed + unit);
                    //increase from 0-100%
                    publishProgress("" + (int)(sentBytes * 100 / bytesAvailable));
                    System.out.println("Start Time: " + startTime);
                    System.out.println("Elapsed Time: " + elapsedTime);
                    System.out.println("Speed: " + speed);
                    System.out.println("New Speed: " + newSpeed);
                    System.out.println("Progress: " + (sentBytes * 100 / bytesAvailable));
                    System.out.println("Bytes Available: " + bytesAvailable);
                    bytesAvailable = fileInputStream.available();
                }
                fileInputStream.close();

                int currentChunk = 1;
               // while (currentChunk <= )
                Log.d(TAG, "data: " +data.toString());
                
                entity.addPart(new FormBodyPart("file", new ByteArrayBody(data,
                        selectedImagePath.substring(selectedImagePath.lastIndexOf("/") + 1))));

                httpPost.setEntity(entity);

                HttpResponse response = httpClient.execute(httpPost);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent(), "UTF-8"));

                sResponse = reader.readLine();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return sResponse;
        }

        public void parseJSONResponse(String responseBody) {
            JSONArray json;
            try {
                json = new JSONArray(responseBody);
                JSONObject job = json.getJSONObject(0);
                Log.d(TAG, responseBody);
                int error_code = job.getInt("error_code");
                dialog.dismiss();
                if (error_code == 0) {
                    FinalizeAsset task = new FinalizeAsset(numberOfChunks);
                    task.execute();
                } else {
                    Toast toast = Toast.makeText(AssetUploadActivity.this,
                            "Upload fail. Please upload again.",
                            Toast.LENGTH_LONG);
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
                        if(currentChunkNo == chunkedImages.size() - 1){
                            FinalizeAsset task = new FinalizeAsset(numberOfChunks);
                            task.execute();
                        }
                        else{
                            UploadAsset task = new UploadAsset();
                            task.numberOfChunks = numberOfChunks;
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
                    .valueOf(1 * 1024 * 1024)));

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
