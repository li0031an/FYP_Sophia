package com.example.amps.fyp_amps_android_yishan.asyncTask;

import android.app.Activity;
import android.app.ProgressDialog;
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

import com.example.amps.fyp_amps_android_yishan.ModifyAssetListener;
import com.example.amps.fyp_amps_android_yishan.Settings;

public class DeleteAsset extends AsyncTask<Object, Object, Object> implements Settings{

    private static String TAG = "DeleteAsset";
    Activity activity;
    ModifyAssetListener modifyAssetListener;
    ProgressDialog dialog;
    SharedPreferences settings;
    String projectId;
    String assetid;
    String assetFullName;
//    String revid;
//    int progressInt = 0;
//    ProgressDialog deleteDialog;

    public DeleteAsset(Activity activity, ModifyAssetListener modifyAssetListener, SharedPreferences settings, String assetid
            , String projectId, String assetFullName){
        this.activity = activity;
        this.modifyAssetListener = modifyAssetListener;
        this.settings = settings;
        this.assetid = assetid;
        this.projectId = projectId;
        this.assetFullName = assetFullName;
    }


    @Override
    protected void onPreExecute() {
        Log.d(TAG, "onPreExecute()");
        dialog = ProgressDialog.show(activity,
                "Deleting Asset", "Please wait...", true);
    }

    @Override
    protected String doInBackground(Object... arg0) {
        Log.d(TAG, "doInBackground()");
        return deleteAsset();
    }

    @Override
    protected void onPostExecute(Object result) {
        Log.d(TAG, "onPostExecute()");
        dialog.dismiss();
        parseJSONResponse((String) result);
    }

    public String deleteAsset() {
        String tokenid = settings.getString("tokenid", null);
        String userid = settings.getString("userid", null);
        String responseBody = "";
        // Instantiate an HttpClient
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(SZAAPIURL + "delAsset");

        // Post parameters
        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("tokenid", tokenid));
        postParameters.add(new BasicNameValuePair("userid", userid));
        postParameters.add(new BasicNameValuePair("projectid", projectId));
        postParameters.add(new BasicNameValuePair("assetid_list", assetid));

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
        Log.d(TAG, "responseBody: " + responseBody);
        JSONArray json;
        JSONObject job;
        try {
            json = new JSONArray(responseBody);
            job = json.getJSONObject(0);
            int errorCode = job.getInt("error_code");
            if (errorCode == 0) {
                showToast("the asset is deleted successfully");
                modifyAssetListener.onDeleteAsset();
            } else {
                String errorMsg = job.getString("error_messages");
                showToast(errorMsg.substring(2, errorMsg.length() - 2));
            }
        }catch (JSONException e) {
                e.printStackTrace();
            }
    }

    public void showToast(String info){
        Toast toast = Toast.makeText(
                activity,
                info,
                Toast.LENGTH_SHORT);
        toast.show();
    }
}