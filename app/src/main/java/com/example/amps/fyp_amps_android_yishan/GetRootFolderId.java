package com.example.amps.fyp_amps_android_yishan;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetRootFolderId extends AsyncTask<Object, Object, Object> implements Settings{

    private static String TAG = "GetRootFolderId";
    GetRootFolderIdListener getRootFolderIdListener;
    Context context;
    ProgressDialog dialog;
    SharedPreferences settings;
    String projectId;
    RootFolderId rootFolderId;
    Folder rootFolderInfo;

    public GetRootFolderId(GetRootFolderIdListener getRootFolderIdListener, Context context, SharedPreferences settings, String projectId){
        this.getRootFolderIdListener = getRootFolderIdListener;
        this.context = context;
        this.settings = settings;
        this.projectId = projectId;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(context,
                "Retrieving Projects", "Please wait...", true);
    }

    @Override
    protected String doInBackground(Object... arg0) {
        return getRootId();

    }

    @Override
    protected void onPostExecute(Object result) {
        dialog.dismiss();
        rootFolderId = parseRootFolderId((String) result);

        if (null != rootFolderId) {
            if (Integer.parseInt(rootFolderId.error_code) == 0) {
                rootFolderInfo = rootFolderId.getRootFolderInfo();
                if (null != rootFolderInfo) {
                    Log.d(TAG, "IN rootFolderInfo ID" + rootFolderInfo.getFolder_id());
                    getRootFolderIdListener.onGetRootFolderIdReady();
                } else {
                    Log.d(TAG, "rootFolderInfo() is null unexpectedly.");
                }
            }
            Log.d(TAG, "getRootId.getError_code: " + rootFolderId.getError_code());
            Log.d(TAG, "getRootId.getError_message: " + rootFolderId.getError_message());

        }
        //Log.d(TAG, "getRootId: " + rootFolderId.getRootFolderInfo().getFolder_id());

//            View v = ProjectDetailsActivity.this
//                    .findViewById(android.R.id.content).getRootView();
        //createTableRow(v);
    }

    public String getRootId() {
        String responseBody = "";
        // Instantiate an HttpClient
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(SZAAPIURL + "getRootFolder");

        // Post parameters
        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("tokenid", settings
                .getString("tokenid", null)));
        postParameters.add(new BasicNameValuePair("userid", settings
                .getString("userid", null)));
        postParameters.add(new BasicNameValuePair("projectid", projectId));

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

    public RootFolderId parseRootFolderId(String responseBody) {
        JSONArray json, errorMsg;
        JSONObject job, data_array;
        RootFolderId rootFolderId = new RootFolderId();
        try {
            json = new JSONArray(responseBody);
            job = json.getJSONObject(0);
            int errorCode= job.getInt("error_code");
            rootFolderId.setError_code(String.valueOf(errorCode));
//                rootFolderId.setError_message(job.getJSONArray("error_messages").getString(0));
////            errorMsg = job.getJSONArray("error_messages");
//            if (!job.isNull("error_messages")) {
//                errorMsg = job.getJSONArray("error_messages");
//                String error = errorMsg.getString(0);
//                rootFolderId.setError_message(error);
//            } else {
//                rootFolderId.setError_message("");
//            }
            rootFolderId.setError_message(job.getString("error_messages"));

            if (errorCode == 0) showToast("get root folder id successfully");
            if (errorCode != 0) {
                showToast(rootFolderId.getError_message().substring(2, rootFolderId.getError_message().length()-2));
                return rootFolderId;
            }
            data_array = job.getJSONObject("data_array");
            Folder folder = parseFolder(data_array);
            if (null != folder) {
                rootFolderId.setRootFolderInfo(folder);
            } else {
                Log.e(TAG, "root folder id is null unexpectedly");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rootFolderId;
    }

    private Folder parseFolder(JSONObject data_array){
        Folder folder = new Folder();
        try {
            if (!data_array.isNull("folder_id")) {
                folder.setFolder_id(data_array.getString("folder_id"));
                Log.d(TAG, "parseFolder: setFolder_id()" + folder.getFolder_id());
            } if (!data_array.isNull("name")) {
                folder.setName(data_array.getString("name"));
            } if (!data_array.isNull("des")) {
                folder.setDes(data_array.getString("des"));
            } if (!data_array.isNull("created_userid")) {
                folder.setCreated_userid(data_array.getString("created_userid"));
            } if (!data_array.isNull("created_username")) {
                folder.setCreated_username(data_array.getString("created_username"));
            } if (!data_array.isNull("created_datetime")) {
                folder.setCreated_datetime(data_array.getString("created_datetime"));
            } if (!data_array.isNull("updated_userid")) {
                folder.setUpdated_userid(data_array.getString("updated_userid"));
            } if (!data_array.isNull("updated_username")) {
                folder.setUpdated_username(data_array.getString("updated_username"));
            } if (!data_array.isNull("updated_datetime")) {
                folder.setUpdated_datetime(data_array.getString("updated_datetime"));
            } if (!data_array.isNull("is_shared")) {
                folder.setIs_shared(data_array.getString("is_shared"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return folder;

    }

    public void showToast(String info){
        Toast toast = Toast.makeText(
                context,
                info,
                Toast.LENGTH_LONG);
        toast.show();
    }

    protected Folder getRootFolder(){
        return rootFolderInfo;
    }

}

