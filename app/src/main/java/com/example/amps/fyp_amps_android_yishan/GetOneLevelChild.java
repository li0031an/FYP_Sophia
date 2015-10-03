package com.example.amps.fyp_amps_android_yishan;

//get one level child

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
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

public class GetOneLevelChild extends AsyncTask<Object, Object, Object> implements Settings {

    private static String TAG = "GetOneLevelChild";
    GetOneLevelChildListener getOneLevelChildListener;
    Context context;
    ProgressDialog dialog;
    SharedPreferences settings;
    String projectId;
    String rootFolderId;
    ArrayList<Folder>folderList = new ArrayList<Folder>();

    public GetOneLevelChild(GetOneLevelChildListener getOneLevelChildListener, Context context, SharedPreferences settings, String rootId, String projectId){
        this.getOneLevelChildListener = getOneLevelChildListener;
        this.context = context;
        this.settings = settings;
        this.rootFolderId = rootId;
        this.projectId = projectId;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(context,
                "Retrieving Projects", "Please wait...", true);
    }

    @Override
    protected String doInBackground(Object... arg0) {
        return getOneLevelChildObject();

    }

    @Override
    protected void onPostExecute(Object result) {
        dialog.dismiss();
        Log.d(TAG, "onPostExecute starts");
        OneLevelChild oneLevelChild = parseOneLevelChildObject((String) result);
        if (null != oneLevelChild) {
            if (Integer.parseInt(oneLevelChild.error_code) == 0) {
                folderList = oneLevelChild.getFolderList();
                getOneLevelChildListener.onOneLevelChildReady();
            }

            Log.d(TAG, "oneLevelChild.getError_code: " + oneLevelChild.getError_code());
            Log.d(TAG, "oneLevelChild.getError_message: " + oneLevelChild.getError_message());
        }
        //Log.d(TAG, "getRootId: " + rootFolderId.getRootFolderInfo().getFolder_id());



    }

    public String getOneLevelChildObject() {
        String responseBody = "";
        // Instantiate an HttpClient
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(SZAAPIURL + "getOneLevelChild");

        // Post parameters
        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("tokenid", settings
                .getString("tokenid", null)));
        postParameters.add(new BasicNameValuePair("userid", settings
                .getString("userid", null)));
        postParameters.add(new BasicNameValuePair("projectid", projectId));
        postParameters.add(new BasicNameValuePair("parent_id", rootFolderId));

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

    public OneLevelChild parseOneLevelChildObject(String responseBody) {
        JSONArray json, data_array;
        JSONObject job;
        OneLevelChild oneLevelChild = new OneLevelChild();
        try {
            json = new JSONArray(responseBody);
            job = json.getJSONObject(0);
            int errorCode= job.getInt("error_code");
            oneLevelChild.setError_code(String.valueOf(errorCode));
            //todo -- handle null string return for error message
            oneLevelChild.setError_message(job.getString("error_messages"));

            if (errorCode == 0) showToast("get one level child successfully");
            if (errorCode != 0) {
                showToast(oneLevelChild.getError_message());
                return oneLevelChild;
            }
            data_array = job.getJSONArray("data_array");
            ArrayList<Folder> folderArrayList = new ArrayList<>();
            for (int i=0; i<data_array.length(); i++) {
                Folder folder = parseFolder(data_array.getJSONObject(i));
                Log.d(TAG, "folder.id" + folder.getFolder_id());
                if (null != folder) {
                    folderArrayList.add(folder);
                }
            }
            oneLevelChild.setFolderList(folderArrayList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return oneLevelChild;
    }

    private Folder parseFolder(JSONObject data_array){
        Folder folder = new Folder();
        try {
            folder.setFolder_id(data_array.getString("folder_id"));
            Log.d(TAG, "parseFolder: setFolder_id()" + folder.getFolder_id());
            folder.setName(data_array.getString("name"));
            folder.setDes(data_array.getString("des"));
            folder.setCreated_userid(data_array.getString("created_userid"));
            folder.setCreated_username(data_array.getString("created_username"));
            folder.setCreated_datetime(data_array.getString("created_datetime"));
            folder.setUpdated_userid(data_array.getString("updated_userid"));
            folder.setUpdated_username(data_array.getString("updated_username"));
            folder.setUpdated_datetime(data_array.getString("updated_datetime"));
            folder.setIs_shared(data_array.getString("is_shared"));
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
}