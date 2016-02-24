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

public class AsyncTaskDeleteProjectFolder extends AsyncTask<Object, Object, Object> implements Settings{
    private static String TAG = "AsyncTaskDeleteProjectFolder";
    Context context;
    ProgressDialog dialog;
    SharedPreferences settings;
    String folderId;
    String projectId;
    String folderName;
    CreateDeleteProjectFolderListener createDeleteProjectFolderListener;

    public AsyncTaskDeleteProjectFolder(CreateDeleteProjectFolderListener createDeleteProjectFolderListener,
                                        String folderId, String folderName, Context context, SharedPreferences settings, String projectId){
        this.createDeleteProjectFolderListener = createDeleteProjectFolderListener;
        this.folderId = folderId;
        this.folderName = folderName;
        this.context = context;
        this.settings = settings;
        this.projectId = projectId;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(context,
                "Delete folder: " + folderName, "Please wait...", true);
    }

    @Override
    protected String doInBackground(Object... arg0) {
        return deleteProjectFolder();

    }

    @Override
    protected void onPostExecute(Object result) {
        dialog.dismiss();
        Log.d(TAG, "onPostExecute starts");
        if (null != result && parseResultObject((String) result) == true) {
            createDeleteProjectFolderListener.onDeleteProjectFolderReady(folderId);
        }
    }

    public String deleteProjectFolder() {
        String responseBody = "";
        // Instantiate an HttpClient
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(SZAAPIURL + "delRecurProjectFolder");

        // Post parameters
        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("tokenid", settings
                .getString("tokenid", null)));
        postParameters.add(new BasicNameValuePair("userid", settings
                .getString("userid", null)));
        postParameters.add(new BasicNameValuePair("projectid", projectId));
        postParameters.add(new BasicNameValuePair("folderid_lst", folderId));
        Log.d(TAG, "postParameters: " + postParameters.toString());
        // Instantiate a POST HTTP method
        try {
            httppost.setEntity(new UrlEncodedFormEntity(postParameters));
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            responseBody = httpclient.execute(httppost, responseHandler);
            Log.d(TAG, "response body: " + responseBody.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseBody;
    }

    public boolean parseResultObject(String responseBody) {
        JSONArray json, errorMsg;
        JSONObject job, data_array;
        boolean isSuccessful = false;
        try {
            json = new JSONArray(responseBody);
            job = json.getJSONObject(0);
            int errorCode = job.getInt("error_code");
//            if (errorCode == 0) showToast("create new folder successfully");
            if (errorCode != 0 && errorCode != 33) {
                errorMsg = job.getJSONArray("error_messages");
                if (null != errorMsg && ! errorMsg.toString().equalsIgnoreCase("[]")) {
                    showToast(errorMsg.toString());
                } else {
                    showToast("Sorry, you cannot delete the folder here.");
                }
            } else {
                isSuccessful = true;
            }

//            data_array = job.getJSONObject("data_array");
//            Log.d(TAG, "JSON: " + data_array.toString());

        }catch(JSONException e){
            e.printStackTrace();
        }
        return isSuccessful;
    }

    public void showToast(String info){
        Toast toast = Toast.makeText(
                context,
                info,
                Toast.LENGTH_SHORT);
        toast.show();
    }

}

