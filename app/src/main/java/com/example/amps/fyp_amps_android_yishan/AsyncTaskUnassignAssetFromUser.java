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

public class AsyncTaskUnassignAssetFromUser extends AsyncTask<Object, Object, Object> implements Settings {
    private static String TAG = "AsyncTaskUnassignAssetFromUser";
    //    GetAssignedUserOfProjectInfoListener GetAssignedUserOfProjectInfoListener;
    UnassignAssetFromUserListener unassignAssetFromUserListener;
    Context context;
    ProgressDialog dialog;
    SharedPreferences settings;
    String projectId;
    String assetId;
    String unassignedUserId;
    String newAssignUserId;

    public AsyncTaskUnassignAssetFromUser(UnassignAssetFromUserListener unassignAssetFromUserListener
            , Context context, SharedPreferences settings, String projectId
            , String assetId, String unassignedUserId, String newAssignUserId) {
//        this.GetAssignedUserOfProjectInfoListener = GetAssignedUserOfProjectInfoListener;
        this.unassignAssetFromUserListener = unassignAssetFromUserListener;
        this.context = context;
        this.settings = settings;
        this.projectId = projectId;
        this.assetId = assetId;
        this.unassignedUserId = unassignedUserId;
        this.newAssignUserId = newAssignUserId;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(context,
                "Assigning asset to user", "Please wait...", true);
    }

    @Override
    protected JSONArray doInBackground(Object... arg0) {
        return assignAsset2User();

    }

    @Override
    protected void onPostExecute(Object result) {
        dialog.dismiss();
        Log.d(TAG, result.toString());
        JSONArray json, data_array;
        JSONObject job;
        try {
            json = (JSONArray) result;
            job = json.getJSONObject(0);
            int errorCode = job.getInt("error_code");
//            if (errorCode == 0) showToast("get one level child successfully");
            if (errorCode != 0) {
                String errorMsg = job.getString("error_messages");
                if (null != errorMsg && !errorMsg.equalsIgnoreCase("[]")) {
                    showToast(errorMsg.substring(2, errorMsg.length() - 2));
                } else {
                    showToast("Error: cannot unassign asset from user.");
                }
                return;
            } else {
                unassignAssetFromUserListener.onUnassignAssetFromUserReady(newAssignUserId);
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    public JSONArray assignAsset2User() {
        JSONArray responseBody = null;
        // Instantiate an HttpClient
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(SZAAPIURL + "unassignAssetFromUser");

        // Post parameters
        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("tokenid", settings
                .getString("tokenid", null)));
        Log.d(TAG, "tokenid: " + settings.getString("tokenid", null));
        postParameters.add(new BasicNameValuePair("userid", settings
                .getString("userid", null)));
        Log.d(TAG, "userid: " + settings.getString("userid", null));
        postParameters.add(new BasicNameValuePair("projectid", projectId));
        Log.d(TAG, "projectid: " + projectId);
        postParameters.add(new BasicNameValuePair("assetid_list", assetId));
        Log.d(TAG, "assetId: " + assetId);
        postParameters.add(new BasicNameValuePair("unassigned_userid", unassignedUserId));
        Log.d(TAG, "assigned_userid: " + unassignedUserId);

        // Instantiate a POST HTTP method
        try {
            httppost.setEntity(new UrlEncodedFormEntity(postParameters));
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBodyString = httpclient.execute(httppost, responseHandler);
            responseBody = new JSONArray(responseBodyString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseBody;
    }

    public void showToast(String info){
        Toast toast = Toast.makeText(
                context,
                info,
                Toast.LENGTH_SHORT);
        toast.show();
    }

}