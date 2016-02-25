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

public class AsyncTaskCreateProjectFolder extends AsyncTask<Object, Object, Object> implements Settings{
    private static String TAG = "AsyncTaskCreateProjectFolder";
    Context context;
    ProgressDialog dialog;
    SharedPreferences settings;
    String newFolderName;
    String projectId;
    String parentFolderId;
    String newFolerId;
    CreateDeleteProjectFolderListener createDeleteProjectFolderListener;

    public AsyncTaskCreateProjectFolder(CreateDeleteProjectFolderListener createDeleteProjectFolderListener, String newFolderName, Context context, String parentFolderId, SharedPreferences settings, String projectId){
        this.createDeleteProjectFolderListener = createDeleteProjectFolderListener;
        this.newFolderName = newFolderName;
        this.context = context;
        this.parentFolderId = parentFolderId;
        this.settings = settings;
        this.projectId = projectId;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(context,
                "Creating new folder: " + newFolderName, "Please wait...", true);
    }

    @Override
    protected String doInBackground(Object... arg0) {
        return createNewProjectFolder();

    }

    @Override
    protected void onPostExecute(Object result) {
        dialog.dismiss();
        Log.d(TAG, "onPostExecute starts");
        if (null != result) {
            newFolerId = parseResultObject((String) result);
            if (null != newFolerId) {
                createDeleteProjectFolderListener.onCreateProjectFolderReady(newFolerId);
            } else {
                //do nothing
            }
        }
    }

    public String createNewProjectFolder() {
        String responseBody = "";
        // Instantiate an HttpClient
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(SZAAPIURL + "createProjectFolder");

        // Post parameters
        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("tokenid", settings
                .getString("tokenid", null)));
        postParameters.add(new BasicNameValuePair("userid", settings
                .getString("userid", null)));
        postParameters.add(new BasicNameValuePair("projectid", projectId));
        postParameters.add(new BasicNameValuePair("parent_folder_id", parentFolderId));
        postParameters.add(new BasicNameValuePair("name", newFolderName));

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

    public String parseResultObject(String responseBody) {
        if (null == responseBody || 0 == responseBody.length()) return null;
        JSONArray json;
        JSONObject job, data_array;
        String tempFolderId = "";
        try {
            json = new JSONArray(responseBody);
            job = json.getJSONObject(0);
            int errorCode = job.getInt("error_code");
//            if (errorCode == 0) showToast("create new folder successfully");
            if (errorCode != 0) {
                String errorMsg = job.getString("error_messages");
                if (null != errorMsg && !errorMsg.equalsIgnoreCase("[]")) {
                    showToast(errorMsg.substring(2, errorMsg.length() - 2));
                } else {
                    showToast("Sorry, you cannot create new folders here.");
                }
            }

            data_array = job.getJSONObject("data_array");
            Log.d(TAG, "JSON: " + data_array.toString());
            if (null != data_array && (!data_array.isNull("folder_id"))) {
                tempFolderId = data_array.getString("folder_id");
                Log.d(TAG, "tempFolderId: " + tempFolderId);
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        return tempFolderId;
    }

    public void showToast(String info){
        Toast toast = Toast.makeText(
                context,
                info,
                Toast.LENGTH_SHORT);
        toast.show();
    }

}
