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

public class AsyncTaskCanAccessFolder extends AsyncTask<Object, Object, Object> implements Settings {

    private static String TAG = "AsyncTaskCanAccessFolder";
    Context context;
//    ProgressDialog dialog;
    SharedPreferences settings;
    String projectId;
    CanAccessFolderListener canAccessFolderListener;

    public AsyncTaskCanAccessFolder(CanAccessFolderListener canAccessFolderListener
            , Context context, SharedPreferences settings, String projectId) {
        this.canAccessFolderListener = canAccessFolderListener;
        this.context = context;
        this.settings = settings;
        this.projectId = projectId;
    }

    @Override
    protected void onPreExecute() {
//        dialog = ProgressDialog.show(context,
//                "Creating new folder: " + newFolderName, "Please wait...", true);
    }

    @Override
    protected String doInBackground(Object... arg0) {
        return canAccessFolder();

    }

    @Override
    protected void onPostExecute(Object result) {
//        dialog.dismiss();
        Log.d(TAG, "onPostExecute starts");
        if (null != result) {
            int canAccess = parseResultObject((String) result);
            boolean isProjectAdmin;
            if (1 == canAccess) {
                isProjectAdmin = true;
                canAccessFolderListener.onCanAccessFolderReady(isProjectAdmin);
            } else if (0 == canAccess) {
                isProjectAdmin = false;
                canAccessFolderListener.onCanAccessFolderReady(isProjectAdmin);
            }
        }
    }

    public String canAccessFolder() {
        String responseBody = "";
        // Instantiate an HttpClient
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(SZAAPIURL + "canAccessFolder");

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
            Log.d(TAG, "response body: " + responseBody.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseBody;
    }

    public int parseResultObject(String responseBody) {
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
                if (null != errorMsg) {
                    showToast(errorMsg.substring(2, errorMsg.length() - 2));
                }
            } else {
                int canAccess = job.getInt("can_access");
                if (1 == canAccess || 0 == canAccess) {
                    return canAccess;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void showToast(String info) {
        Toast toast = Toast.makeText(
                context,
                info,
                Toast.LENGTH_SHORT);
        toast.show();
    }

}

