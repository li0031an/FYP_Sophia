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

public class AssetsPreviewFragment extends Fragment implements Settings, GetAssetListener, DeleteAssetListener, View.OnClickListener {
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

    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.imageButtonReview:
                    if ((asset.getExt().equals("jpg") || (asset.getExt().equals("png") || (asset.getExt().equals("jpeg")) || (asset.getExt().equals("gif"))))) {

                        ImageButton imageButtonReview = (ImageButton) getActivity().findViewById(R.id.imageButtonReview);
                        imageButtonReview.setOnTouchListener(new OnTouchListener() {
                            @Override
                            public boolean onTouch(View arg0, MotionEvent event) {
                                int action = event.getAction();
                                switch (action) {
                                    case MotionEvent.ACTION_UP:
//                                        Intent reviewImageFullScreen = new Intent(getActivity(),ImageReviewFullScreenActivity.class);
//                                        reviewImageFullScreen.putExtra("imageDecodedString", decodedString);
//                                        getActivity().startActivity(reviewImageFullScreen);
//                                        break;
                                }
                                return true;
                            }
                        });
                    } else if ((asset.getExt().equals("avi") || (asset.getExt().equals("flv") || (asset.getExt().equals("3gp")) || (asset.getExt().equals("webm"))))) {
//                        Intent i = new Intent(getActivity(), VideoPlayerActivity.class);
//                        i.putExtra("asset_id", asset_id);
//                        i.putExtra("token_id", tokenid);
//                        i.putExtra("user_id", userid);
//                        i.putExtra("project_id", project_id);
//                        i.putExtra("revNum",a.getRevNum());
//                        startActivity(i);
                    }
                    break;
                case R.id.imageButtonUpload:
//                    Intent uploadImage = new Intent(getActivity(),ImageUploadActivity.class);
//                    uploadImage.putExtra("asset_id", asset_id);
//                    uploadImage.putExtra("project_id", project_id);
//                    getActivity().startActivity(uploadImage);
                    break;
                case R.id.imageButtonDownload:
                    String assetFullNameDownloaded = asset.getName() + "." + asset.getExt();
                    DownloadAsset taskDownload = new DownloadAsset(getActivity(), settings
                            , asset.asset_id, project_id, assetFullNameDownloaded, asset.getRevId());
                    taskDownload.execute();
                    break;
                case R.id.imageButtonDelete:
                    String assetFullNameDeleted = asset.getName() + "." + asset.getExt();
                    DeleteAsset taskDelete = new DeleteAsset(getActivity(), this, settings
                            , asset.asset_id, project_id, assetFullNameDeleted);
                    taskDelete.execute();
                    break;
                default:
//                    getActivity().finish();
                    Log.e(TAG, "no case matched in onClick()");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDeleteAsset(){
        getActivity().finish();
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

    public void onAssetReady() {

    }

    public void onAssetDetailReady() {
        asset = getAssetDetail.getAssetDetail().get(0);
        ArrayList<Object> arrayList = new ArrayList<>();
        arrayList.add(asset);
        mAdapter = new PreviewRecyclerViewAdapter(getActivity(), arrayList);
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

}
