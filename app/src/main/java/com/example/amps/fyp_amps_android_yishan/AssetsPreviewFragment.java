package com.example.amps.fyp_amps_android_yishan;

import java.io.File;
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

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.amps.fyp_amps_android_yishan.preview.ImageReviewFullScreenActivity;
import com.example.amps.fyp_amps_android_yishan.preview.VideoPlayerActivity;

public class AssetsPreviewFragment extends Fragment implements Settings, GetAssetListener, ModifyAssetListener, View.OnClickListener, DownloadAssetListener {
    private final static String TAG = "AssetsPreviewFragment";
    GetAssetDetail getAssetDetail;
    SharedPreferences settings;
    ProgressDialog dialog;
    ProgressDialog downloadDialog;
    Asset asset;
    String userid;
    String tokenid;
    String asset_id;
    String project_id, folderId;
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
    public void setFolderId(String folderId) {
        this.folderId = folderId;
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
    public void onDownloadAssetReady(Uri uri) {
//        --todo: to be implemented
        Intent reviewImageFullScreen = new Intent(getActivity(),ImageReviewFullScreenActivity.class);
        reviewImageFullScreen.putExtra("imageExt", asset.getExt());
        reviewImageFullScreen.putExtra("imageUri", uri.toString());
        getActivity().startActivity(reviewImageFullScreen);
    };

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
        String selectAttributes = "[asset_id], [name], [ext], [file_size]" +
                ", [latest_revid], [latest_revnum], [updated_userid], [updated_username]" +
                ", [updated_datetime]" +
                ", [base64_thumbnail], [latest_revsize]";
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

    public void showToast(String message) {
        Toast toast = Toast.makeText(getActivity(),
                message, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.imageButtonReview:
                    if (null == asset.getFileType()) asset.setFileType(setAssetFileType(asset.getExt()));

                    if (asset.getFileType() == Asset.FileType.IMAGE) {
                        ImageButton imageButtonReview = (ImageButton) getActivity().findViewById(R.id.imageButtonReview);
                        imageButtonReview.setOnTouchListener(new OnTouchListener() {
                            @Override
                            public boolean onTouch(View arg0, MotionEvent event) {
                                int action = event.getAction();
                                switch (action) {
                                    case MotionEvent.ACTION_UP:
//                                        Intent reviewImageFullScreen = new Intent(getActivity(),ImageReviewFullScreenActivity.class);
//                                        reviewImageFullScreen.putExtra("imageExt", asset.getExt());
//                                        reviewImageFullScreen.putExtra("imageDecodedString", decodedString);
//                                        getActivity().startActivity(reviewImageFullScreen);
                                        String assetFullNameDownloaded = asset.getName() + "." + asset.getExt();
                                        DownloadAsset taskDownload = new DownloadAsset(getActivity(), settings
                                                , asset.asset_id, project_id, assetFullNameDownloaded, asset.getExt(), asset.getLatest_revid(), AssetsPreviewFragment.this, true);
                                        taskDownload.execute();
                                        break;
                                }
                                return true;
                            }
                        });
                    } else if (asset.getFileType() == Asset.FileType.VIDEO) {
                        Intent i = new Intent(getActivity(), VideoPlayerActivity.class);
                        i.putExtra("asset_id", asset_id);
                        i.putExtra("token_id", tokenid);
                        i.putExtra("user_id", userid);
                        i.putExtra("project_id", project_id);
                        i.putExtra("revNum",asset.getLatest_revnum());
                        startActivity(i);
                    } else {
                        //todo -- preview for audio, document/text to be implemented
                        showToast("Sorry, this kind of file is not supported for preview yet");
                }
                    break;
                case R.id.imageButtonUpload:
                    Intent uploadImage = new Intent(getActivity(),AssetUploadActivity.class);
                    uploadImage.putExtra("asset_id", asset_id);
                    uploadImage.putExtra("project_id", project_id);
                    uploadImage.putExtra("folder_id", folderId);
                    uploadImage.putExtra("isNewRevision", true);
                    Log.d("AssetUploadActivity", "pass to folder_id: " + folderId);
                    String assetFullName = asset.getName() + asset.getExt();
                    uploadImage.putExtra("assetFullName", assetFullName);
                    Log.d("AssetUploadActivity", "pass to assetFullName: "+assetFullName);
                    uploadImage.putExtra("latest_revid", asset.getLatest_revid());
                    Log.d("AssetUploadActivity", "pass to latest_revid: "+ asset.getLatest_revid());
                    getActivity().startActivity(uploadImage);
                    break;
                case R.id.imageButtonDownload:
                    String assetFullNameDownloaded = asset.getName() + "." + asset.getExt();
                    DownloadAsset taskDownload = new DownloadAsset(getActivity(), settings
                            , asset.asset_id, project_id, assetFullNameDownloaded, asset.getExt(), asset.getLatest_revid(), this, false);
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

        Asset.FileType fileType = setAssetFileType(asset.getExt());
        asset.setFileType(fileType);

        if(asset.getFileType() == Asset.FileType.IMAGE) {
            if(!asset.getBase64_thumbnail().isEmpty()){
                decodedString = Base64.decode(asset.getBase64_thumbnail(), Base64.DEFAULT);
            }
        }

        GetCreatedUserInfo task = new GetCreatedUserInfo();
        task.execute();
    }

    protected Asset.FileType setAssetFileType (String ext) {
        Asset.FileType fileType;
        if((ext.equals("jpg") || (ext.equals("png") || (ext.equals("jpeg")) || (ext.equals("gif"))))){
            fileType = Asset.FileType.IMAGE;
        }
        else if((ext.equals("avi") || (ext.equals("flv") || (ext.equals("mp4")) || (ext.equals("webm"))
                || (ext.equals("wmv"))))){
            fileType = Asset.FileType.VIDEO;
        } else if ((ext.equals("pdf") || (ext.equals("txt") || (ext.equals("doc")) || (ext.equals("xml")) || (ext.equals("pptx"))))) {
            fileType = Asset.FileType.DOCUMENT;
        } else if ((ext.equals("mp3"))) {
            fileType = Asset.FileType.AUDIO;
        } else {
            fileType = Asset.FileType.OTHER;
        }
        return fileType;
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
