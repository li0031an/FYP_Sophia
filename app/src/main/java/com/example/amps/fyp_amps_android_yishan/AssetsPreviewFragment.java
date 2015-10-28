package com.example.amps.fyp_amps_android_yishan;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import com.example.amps.mediaPlayer.VideoPlayerActivity;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class AssetsPreviewFragment extends Fragment implements Settings, GetAssetListener, View.OnClickListener {
    private final static String TAG = "AssetsPreviewFragment";
    GetAssetDetail getAssetDetail;
    SharedPreferences settings;
    ProgressDialog dialog;
    ProgressDialog downloadDialog;
    Asset asset;
    String userid;
    String tokenid;
    String asset_id;
    String project_id;
    String video_url;
    String revId;
    double fileSize;
    static ImageView imageViewPreview;
    TextView textViewRevisionNo2;
    TextView textViewUploadedBy2;
    TextView textViewUploadedDate2;
    TextView textViewComment2;
    TextView tvFileSize2;
    String fileSizeUnit;
    byte[] decodedString;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    DecimalFormat twoDP = new DecimalFormat("#.##");

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public void setTokenid(String tokenid) {
        this.tokenid = tokenid;
    }

    public void setAsset_id(String asset_id) {
        this.asset_id = asset_id;
    }

    public void setProject_id(String project_id) {
        this.project_id = project_id;
    }

    public void setVideo_Url(String video_url){
        this.video_url = video_url;
    }

    public void setRevId(String revId){
        this.revId = revId;
    }
    public void setFile_size(double fileSize){
        this.fileSize = fileSize;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_assets_preview,
                container, false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        imageViewPreview = (ImageView) getActivity().findViewById(
//                R.id.imageViewPreview);
//        textViewRevisionNo2 = (TextView) getActivity().findViewById(
//                R.id.textViewRevisionNo2);
//        textViewUploadedBy2 = (TextView) getActivity().findViewById(
//                R.id.textViewUploadedBy2);
//        textViewUploadedDate2 = (TextView) getActivity().findViewById(
//                R.id.textViewUploadedDate2);
//        textViewComment2 = (TextView) getActivity().findViewById(
//                R.id.textViewComment2);
//        tvFileSize2 = (TextView)getActivity().findViewById(R.id.tvFileSize2);
        settings = getActivity().getSharedPreferences(SETTINGS, 0);
        String selectAttributes = "[asset_id], [name], [ext], [file_size], [latest_revid], [latest_revnum], [updated_userid], [updated_datetime], [base64_thumbnail], [latest_revsize]";
        ArrayList<String> assetIdList = new ArrayList<>();
        assetIdList.add(asset_id);
        getAssetDetail = new GetAssetDetail(this, getActivity(), settings, assetIdList, project_id, selectAttributes);
        getAssetDetail.execute();

        /////
        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        /////
    }

//    public void onClick(View view) {
//        try {
//            switch (view.getId()) {
//                case R.id.imageButtonReview:
//                    if((a.getExt().equals("jpg") || (a.getExt().equals("png") || (a.getExt().equals("jpeg")) || (a.getExt().equals("gif"))))){
//
//                        ImageButton imageButtonReview = (ImageButton)getActivity().findViewById(R.id.imageButtonReview);
//                        imageButtonReview.setOnTouchListener(new OnTouchListener(){
//                            @Override
//                            public boolean onTouch(View arg0, MotionEvent event) {
//                                int action = event.getAction();
//                                switch (action) {
//                                    case MotionEvent.ACTION_UP:
//                                        Intent reviewImageFullScreen = new Intent(getActivity(),ImageReviewFullScreenActivity.class);
//                                        reviewImageFullScreen.putExtra("imageDecodedString", decodedString);
//                                        getActivity().startActivity(reviewImageFullScreen);
//                                        break;
//                                }
//                                return true;
//                            }
//                        });
//                    }
//                    else if((a.getExt().equals("avi") || (a.getExt().equals("flv") || (a.getExt().equals("3gp")) || (a.getExt().equals("webm"))))){
//                        Intent i = new Intent(getActivity(), VideoPlayerActivity.class);
//                        i.putExtra("asset_id", asset_id);
//                        i.putExtra("token_id", tokenid);
//                        i.putExtra("user_id", userid);
//                        i.putExtra("project_id", project_id);
//                        i.putExtra("revNum",a.getRevNum());
//                        startActivity(i);
//                    }
//                    break;
//                case R.id.imageButtonUpload:
//                    Intent uploadImage = new Intent(getActivity(),ImageUploadActivity.class);
//                    uploadImage.putExtra("asset_id", asset_id);
//                    uploadImage.putExtra("project_id", project_id);
//                    getActivity().startActivity(uploadImage);
//                    break;
//                case R.id.imageButtonDownload:
//                    DownloadAsset taskDownload = new DownloadAsset();
//                    taskDownload.execute();
//                    break;
//                case R.id.imageButtonDelete:
//                    DeleteAsset taskDelete = new DeleteAsset();
//                    taskDelete.execute();
//                    break;
//                default:
//                    getActivity().finish();
//                    break;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

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


    @Override
    public void onClick(View view) {
        Log.i(TAG, " Clicked on Item ");

    }

    public void onAssetReady(){

    }

    public void onAssetDetailReady(){
        asset = getAssetDetail.getAssetDetail().get(0);
        ArrayList<Object> arrayList = new ArrayList<>();
        arrayList.add(asset);
        mAdapter = new PreviewRecyclerViewAdapter(getActivity(),arrayList);
        ((PreviewRecyclerViewAdapter) mAdapter).setOnItemClickListener(new PreviewRecyclerViewAdapter.MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Log.i(TAG, " Clicked on Item ");

            }
        });
        mRecyclerView.setAdapter(mAdapter);


            GetCreatedUserInfo task = new GetCreatedUserInfo();
            task.execute();
    }

    public class GetCreatedUserInfo extends AsyncTask<Object, Object, Object> {

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(
                    AssetsPreviewFragment.this.getActivity(),
                    "Retrieving User Information", "Please wait...", true);
        }

        @Override
        protected String doInBackground(Object... arg0) {
            return retrieveUser();
        }

        @Override
        protected void onPostExecute(Object result) {
            dialog.dismiss();
            parseJSONResponse((String) result);
        }

        public String retrieveUser() {
            String responseBody = "";
            // Instantiate an HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(SZAAPIURL + "getUserInfo");

            // Post parameters
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("tokenid", tokenid));
            postParameters.add(new BasicNameValuePair("userid", userid));
            postParameters.add(new BasicNameValuePair("condition",
                    "[userid] = '" + asset.getUpdated_userid() + "'"));

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
            JSONObject job;
            try {
                json = new JSONArray(responseBody);
                job = json.getJSONObject(0);
                data_array = job.getJSONArray("data_array");
                JSONObject dataJob = new JSONObject(data_array.getString(0));
//                textViewUploadedBy2.setText(dataJob.getString("username"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class DownloadAsset extends AsyncTask<Object, String, Object> {
        @Override
        protected void onPreExecute() {
            /**
             dialog = ProgressDialog.show(
             WorkingAssetsPreviewFragment.this.getActivity(),
             "Downloading Asset", "Please wait...", true);
             **/
            //After end user clicks download button,
            //the progress bar will pop out and show its progress(%) and speed
            //super.onPreExecute();
            downloadDialog = new ProgressDialog(AssetsPreviewFragment.this.getActivity());
            downloadDialog.setMessage("Downloading " + asset.getName() + "." + asset.getExt() + ".. Please wait..");
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
            String req = SZAAPIURL + "downloadAsset?tokenid=" + tokenid +
                    "&userid=" + userid +
                    "&projectid=" + project_id +
                    "&assetid_lst=" + asset_id +
                    "&revid=" + asset.getRevId() ;
            String downloadedFileName = asset.getName() + "." + asset.getExt();
            //TrafficStats traffic = new TrafficStats();
            //double totalNetworkBytes = traffic.getTotalTxBytes();
            try{
                URL url = new URL(req);
                long startTime = System.currentTimeMillis();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.connect();

                File downloadLocation = new File(
                        Environment.getExternalStoragePublicDirectory
                                (Environment.DIRECTORY_DOWNLOADS),
                        downloadedFileName);
                //get downloaded data
                //FileOutputStream fileOutput = new FileOutputStream (downloadLocation);
                OutputStream fileOutput = new FileOutputStream (downloadLocation);

                //get data from internet
                InputStream inputStream = connection.getInputStream();

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
                while ((count = inputStream.read(data)) != -1)
                {
                    total += count;
                    //calculate speed
                    double speed = total * 1000.0f / elapsedTime;

                    if (speed < 1024)
                    {
                        newSpeed = Double.parseDouble(twoDP.format(speed));
                        unit = "Bytes/ sec";
                    }
                    else if (speed < 1024*1024){
                        newSpeed = Double.parseDouble(twoDP.format(speed / 1024));
                        unit = "kB/s";
                    }
                    else
                    {
                        newSpeed = Double.parseDouble(twoDP.format((speed / 1024 * 1024)/1000000));
                        unit = "MB/s";
                    }

                    downloadDialog.setProgressNumberFormat(newSpeed + unit);
                    //increase from 0-100%
                    publishProgress("" + (int)((total*100)/totalSize));
                    fileOutput.write(data, 0, count);
                }

                //close connection
                fileOutput.flush();
                fileOutput.close();
                inputStream.close();
            }
            catch(Exception e){
                //e.printStackTrace();
                downloadDialog.dismiss();
                AlertDialog downloadError = new AlertDialog.Builder(getActivity()).create();
                downloadError.setTitle("Download Status");
                downloadError.setMessage("Failed to download " + asset.getName() + "." + asset.getExt() + ".. Please try again later..");
                downloadError.setButton("OK", new DialogInterface.OnClickListener() {
                @Override
                    public void onClick(DialogInterface dialog, int which) {
                    //  TODO Auto-generated method stub

                }
                });
                downloadError.show();
            }

            return null;
        }

        //Updating Progress Bar
        //Focus:
        protected void onProgressUpdate(String... progress){
            //Log.d("",progress[0]);
            //super.onProgressUpdate(progress);
            downloadDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void onPostExecute(Object result) {
            /**
             dialog.dismiss();
             Toast toast = Toast.makeText(
             WorkingAssetsPreviewFragment.this.getActivity(),
             "Downloaded successfully!", Toast.LENGTH_LONG);
             toast.show();
             **/
            downloadDialog.dismiss();
            AlertDialog downloadComplete = new AlertDialog.Builder(getActivity()).create();
            downloadComplete.setTitle("Download Status");
            downloadComplete.setMessage(asset.getName() + "." + asset.getExt() + " is downloaded successfully.");
            downloadComplete.setButton("OK", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub

                }
            });
            downloadComplete.show();
        }

        /**
         public String downloadAsset() {
         String req = SZAAPIURL + "downloadAsset?tokenid=" + tokenid +
         "&userid=" + userid +
         "&projectid=" + project_id +
         "&assetid_lst=" + asset_id +
         "&revid=" + a.getRevId() ;
         try {
         DownloadManager downloadManager;
         downloadManager = (DownloadManager)WorkingAssetsPreviewFragment.this.getActivity().getSystemService("download");
         Uri uri = Uri.parse(req);
         DownloadManager.Request request = new DownloadManager.Request(uri);
         request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, a.getName() + "." + a.getExt());
         downloadManager.enqueue(request);
         } catch (Exception e) {
         e.printStackTrace();
         }
         return null;
         }**/
    }

    public class DeleteAsset extends AsyncTask<Object, Object, Object> {

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(AssetsPreviewFragment.this.getActivity(),
                    "Deleting Asset", "Please wait...", true);
        }

        @Override
        protected String doInBackground(Object... arg0) {
            return deleteAsset();
        }

        @Override
        protected void onPostExecute(Object result) {
            dialog.dismiss();
            parseJSONResponse((String) result);

        }

        public String deleteAsset() {
            String responseBody = "";
            // Instantiate an HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(SZAAPIURL + "delAsset");

            // Post parameters
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("tokenid", tokenid));
            postParameters.add(new BasicNameValuePair("userid", userid));
            postParameters.add(new BasicNameValuePair("projectid", project_id));
            postParameters.add(new BasicNameValuePair("assetid_list", asset_id));

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
            Intent intent = new Intent(AssetsPreviewFragment.this.getActivity(), ProjectDetailsActivity.class);
            startActivity(intent);
        }
    }
}
