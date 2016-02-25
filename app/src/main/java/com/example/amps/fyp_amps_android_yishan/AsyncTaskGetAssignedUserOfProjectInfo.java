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

public class AsyncTaskGetAssignedUserOfProjectInfo extends AsyncTask<Object, Object, Object> implements Settings {

    private static String TAG = "AsyncTaskGetAssignedUserOfProjectInfo";
    GetAssignedUserOfProjectInfoListener GetAssignedUserOfProjectInfoListener;
    Context context;
    ProgressDialog dialog;
    SharedPreferences settings;
    String projectId;
    String selectAttributes = "[userid], [username]";
    String[] userNameList = null;
    String[] userIdList = null;


    public AsyncTaskGetAssignedUserOfProjectInfo(GetAssignedUserOfProjectInfoListener
                                                         GetAssignedUserOfProjectInfoListener
            , Context context, SharedPreferences settings, String projectId) {
        this.GetAssignedUserOfProjectInfoListener = GetAssignedUserOfProjectInfoListener;
        this.context = context;
        this.settings = settings;
        this.projectId = projectId;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(context,
                "Retrieving users", "Please wait...", true);
    }

    @Override
    protected JSONArray doInBackground(Object... arg0) {
        return getAssignedUserOfProjectInfo();

    }

    @Override
    protected void onPostExecute(Object result) {
        dialog.dismiss();
        if (null == result) return ;
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
                    showToast("Error: cannot get assigned users of project info.");
                }
                return;
            }
            data_array = job.getJSONArray("data_array");
            Log.d(TAG, "JSON: " + data_array.toString());
            int length = data_array.length();
            if (null != data_array && length > 0) {
                userIdList = new String[length];
                userNameList = new String[length];
                for (int i=0; i<length; i++) {
                    job = data_array.getJSONObject(i);
                    if (!job.isNull("userid")) {
                        userIdList[i] = (job.getString("userid"));
                    } if (!job.isNull("username")) {
                        userNameList[i] = (job.getString("username"));
                    }
                }
            } else {
                showToast("There is no user assigned for this project yet.");
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        GetAssignedUserOfProjectInfoListener.onGetAssignedUserOfProjectInfoReady();
    }

    protected String[] getUserIdList() {
        if (null != userIdList) {
            return userIdList;
        } else {
            return null;
        }
    }

    protected String[] getUserNameList() {
        if (null != userNameList) {
            return userNameList;
        } else {
            return null;
        }
    }

    public JSONArray getAssignedUserOfProjectInfo() {
        JSONArray responseBody = null;
        // Instantiate an HttpClient
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(SZAAPIURL + "getAssignedUserOfProjectInfo");

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
        postParameters.add(new BasicNameValuePair("select", selectAttributes));

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
