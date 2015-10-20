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

public class GetAssetDetail extends AsyncTask<Object, Object, Object> implements Settings{
    private static String TAG = "GetAssetDetail";
    GetAssetListener getAssetListener;
    Context context;
    ProgressDialog dialog;
    SharedPreferences settings;
    String projectId;
    String assetId;
    Asset asset = null;

    public GetAssetDetail(GetAssetListener getAssetListener, Context context, SharedPreferences settings, String assetId, String projectId){
        Log.d(TAG, "GetAssetDetail constructor starts");
        this.getAssetListener = getAssetListener;
        this.context = context;
        this.settings = settings;
        this.assetId = assetId;
        this.projectId = projectId;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(context,
                "Retrieving Projects", "Please wait...", true);
    }

    @Override
    protected String doInBackground(Object... arg0) {
        Log.d(TAG, "doInBackground starts");
        return getAssetObject();

    }

    @Override
    protected void onPostExecute(Object result) {
        dialog.dismiss();
        Log.d(TAG, "onPostExecute starts");
        if (null != result) {
            asset = parseAssetObject((String) result);
            if (null != asset ) {
                getAssetListener.onAssetDetailReady();
            } else {
                showToast("The folder is empty");
            }
        }
    }

    public String getAssetObject() {
        Log.d(TAG, "getAssetObject() starts");
        String responseBody = "";
        // Instantiate an HttpClient
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(SZAAPIURL + "getAsset");

        // Post parameters
        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("tokenid", settings
                .getString("tokenid", null)));
        postParameters.add(new BasicNameValuePair("userid", settings
                .getString("userid", null)));
        postParameters.add(new BasicNameValuePair("projectid", projectId));
        postParameters.add(new BasicNameValuePair("condition", "[asset_id] = '"+assetId+"'"));
        postParameters.add(new BasicNameValuePair("select", "[asset_id],[base64_thumbnail],[ext]"));

        // Instantiate a POST HTTP method
        try {
            Log.d(TAG, "postParameters: " + postParameters.toString());
            httppost.setEntity(new UrlEncodedFormEntity(postParameters));
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            responseBody = httpclient.execute(httppost, responseHandler);
            Log.d(TAG, "response body: " + responseBody.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseBody;
    }

    public Asset parseAssetObject(String responseBody) {
        JSONArray json, data_array;
        JSONObject job;
        Asset asset = null;
        try {
            json = new JSONArray(responseBody);
            job = json.getJSONObject(0);
            int errorCode = job.getInt("error_code");
            if (errorCode == 0) showToast("get one level child successfully");
            if (errorCode != 0) {
                String errorMsg = job.getString("error_messages");
                showToast(errorMsg.substring(2, errorMsg.length() - 2));
                asset = null;
            }
            data_array = job.getJSONArray("data_array");
            Log.d(TAG, "JSON: " + data_array.toString());
            if (null != data_array) {
                asset = parseAsset(data_array.getJSONObject(0));
                Log.d(TAG, "asset.id" + asset.getAsset_id());
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        return asset;
    }

    private Asset parseAsset(JSONObject data_array){
        Asset asset = new Asset();
        try {
            if (!data_array.isNull("asset_id")) {
                asset.setAsset_id(data_array.getString("asset_id"));
                Log.d(TAG, "parseAsset: setAsset_id()" + asset.getAsset_id());
            } if (!data_array.isNull("name")) {
                asset.setName(data_array.getString("name"));
            } if (!data_array.isNull("ext")) {
                asset.setExt(data_array.getString("ext"));
            } if (!data_array.isNull("created_userid")) {
                asset.setCreated_userid(data_array.getString("created_userid"));
            } if (!data_array.isNull("estimated_datestart")) {
                asset.setEstimated_datestart(data_array.getString("estimated_datestart"));
            } if (!data_array.isNull("created_datetime")) {
                asset.setCreated_datetime(data_array.getString("created_datetime"));
            } if (!data_array.isNull("updated_userid")) {
                asset.setUpdated_userid(data_array.getString("updated_userid"));
            } if (!data_array.isNull("estimated_dateend")) {
                asset.setEstimated_dateend(data_array.getString("estimated_dateend"));
            } if (!data_array.isNull("updated_datetime")) {
                asset.setUpdated_datetime(data_array.getString("updated_datetime"));
            } if (!data_array.isNull("base64_thumbnail")) {
                asset.setBase64_thumbnail(data_array.getString("base64_thumbnail"));
            } if (!data_array.isNull("statusid")) {
                asset.setStatusid(data_array.getString("statusid"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return asset;
    }

    public void showToast(String info){
        Toast toast = Toast.makeText(
                context,
                info,
                Toast.LENGTH_LONG);
        toast.show();
    }

    protected Asset getAssetDetail(){
        return asset;
    }
}
