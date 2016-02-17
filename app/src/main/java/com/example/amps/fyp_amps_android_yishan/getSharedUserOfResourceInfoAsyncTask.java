package com.example.amps.fyp_amps_android_yishan;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;

public class GetSharedUserOfResourceInfoAsyncTask extends AsyncTask<Object, Object, Object> implements Settings {

    private static String TAG = "GetSharedUserOfResourceInfoAsyncTask";
    GetSharedUserOfResourceInfoListener GetSharedUserOfResourceInfoListener;
    Context context;
    ProgressDialog dialog;
    SharedPreferences settings;
    String projectId;
    String resourceId;
    RootFolderId rootFolderId;
    Folder rootFolderInfo;

    public GetSharedUserOfResourceInfoAsyncTask(GetSharedUserOfResourceInfoListener
                                                        GetSharedUserOfResourceInfoListener
            , Context context, SharedPreferences settings, String projectId
            , String resourceId) {
        this.GetSharedUserOfResourceInfoListener = GetSharedUserOfResourceInfoListener;
        this.context = context;
        this.settings = settings;
        this.projectId = projectId;
        this.resourceId = resourceId;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(context,
                "Retrieving Projects", "Please wait...", true);
    }

    @Override
    protected String doInBackground(Object... arg0) {
        return getSharedUserOfResourceInfo();

    }

    @Override
    protected void onPostExecute(Object result) {
        dialog.dismiss();
        Log.d(TAG, result.toString());
        GetSharedUserOfResourceInfoListener.onGetSharedUserOfResourceInfoReady();

    }

    public String getSharedUserOfResourceInfo() {
        String responseBody = "";
        // Instantiate an HttpClient
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(SZAAPIURL + "getSharedUserOfProjectInfo");

        // Post parameters
        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("tokenid", settings
                .getString("tokenid", null)));
        Log.d(TAG, "tokenid: " + settings.getString("tokenid", null));
        postParameters.add(new BasicNameValuePair("userid", settings
                .getString("userid", null)));
        Log.d(TAG, "userid: " + settings.getString("userid", null));
        postParameters.add(new BasicNameValuePair("projectid", projectId));
        Log.d(TAG, "projectid: " + settings.getString("projectid", null));
        postParameters.add(new BasicNameValuePair("shared_resourceid", resourceId));
        Log.d(TAG, "shared_resourceid: " + resourceId);

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


}
