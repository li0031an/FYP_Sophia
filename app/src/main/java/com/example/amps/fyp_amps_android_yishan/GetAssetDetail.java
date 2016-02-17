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

public class GetAssetDetail extends AsyncTask<Object, Object, ArrayList<String>> implements Settings{
    private static String TAG = "GetAssetDetail";
    GetAssetListener getAssetListener;
    Context context;
    ProgressDialog dialog;
    SharedPreferences settings;
    String projectId;
    String selectAttributes = "[created_username], [updated_username], [latest_revsize]";
    ArrayList<String> assetIdList = new ArrayList<String>()  ;
    ArrayList<Asset> assetList = new ArrayList<Asset>();

    public GetAssetDetail(GetAssetListener getAssetListener, Context context,
                          SharedPreferences settings, ArrayList<String> assetIdList,
                          String projectId, String selectAttributes){
        Log.d(TAG, "GetAssetDetail constructor starts");
        this.getAssetListener = getAssetListener;
        this.context = context;
        this.settings = settings;
        this.assetIdList = assetIdList;
        this.projectId = projectId;
        this.selectAttributes = selectAttributes;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(context,
                "Retrieving Projects", "Please wait...", true);
    }

    @Override
    protected ArrayList<String> doInBackground(Object... arg0) {
        Log.d(TAG, "doInBackground starts");
        ArrayList<String> jsonStringList = new ArrayList<String>();
        for(int i = 0; i<assetIdList.size(); i++) {
            String temp = getAssetObject(assetIdList.get(i));
            if (null != temp) {
                jsonStringList.add(temp);
            }
        } return jsonStringList;
    }

    @Override
    protected void onPostExecute(ArrayList<String> result) {
        dialog.dismiss();
        Log.d(TAG, "onPostExecute starts");
        if (null != result && result.size() > 0) {
            for (int i = 0; i<result.size(); i++) {
                Asset asset = parseAssetObject(result.get(i));
                if (null != asset) {
                    assetList.add(asset);
                } else {
                    showToast("The asset is empty");
                }
            }
        } if (null != assetList && assetList.size() > 0) {
            getAssetListener.onAssetDetailReady();
        }
    }

    public String getAssetObject(String assetId) {
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
        postParameters.add(new BasicNameValuePair("select", selectAttributes));

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
//            if (errorCode == 0) showToast("get one level child successfully");
            if (errorCode != 0) {
                String errorMsg = job.getString("error_messages");
                showToast(errorMsg.substring(2, errorMsg.length() - 2));
                asset = null;
            }
            data_array = job.getJSONArray("data_array");
            Log.d(TAG, "JSON: " + data_array.toString());
            if (null != data_array && data_array.length() > 0) {
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
            } if ((!data_array.isNull("base64_thumbnail")) || (!data_array.isNull("base64_small_file"))) {
                if (!data_array.isNull("base64_thumbnail")) {
                    asset.setBase64_thumbnail(data_array.getString("base64_thumbnail"));
                } else {
                    asset.setBase64_thumbnail(data_array.getString("base64_small_file"));
                }
            } if (!data_array.isNull("statusid")) {
                asset.setStatusid(data_array.getString("statusid"));
            } if (!data_array.isNull("latest_revid")) {
                asset.setLatest_revid(data_array.getString("latest_revid"));
            } if (!data_array.isNull("latest_revnum")) {
                asset.setLatest_revnum(data_array.getString("latest_revnum"));
            } if (!data_array.isNull("latest_revsize")) {
                asset.setLatest_revsize(data_array.getDouble("latest_revsize"));
            } if (!data_array.isNull("created_username")) {
                asset.setCreated_username(data_array.getString("created_username"));
            } if (!data_array.isNull("updated_username")) {
                asset.setUpdated_username(data_array.getString("updated_username"));
            } if (!data_array.isNull("assigned_userid")) {
                asset.setAssigned_userid(data_array.getString("assigned_userid"));
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
                Toast.LENGTH_SHORT);
        toast.show();
    }

    protected ArrayList<Asset> getAssetDetail(){
        return assetList;
    }
}
