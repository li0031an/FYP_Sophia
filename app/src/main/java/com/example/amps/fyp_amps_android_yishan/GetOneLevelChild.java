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
    boolean valid = false;
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
        OneLevelChild oneLevelChild = null;
        if (null != result) {
            oneLevelChild = parseOneLevelChildObject((String) result);
        }

            if (Integer.parseInt(oneLevelChild.getError_code()) == 0) {
                valid = true;
                if (null != oneLevelChild) {
                    folderList = oneLevelChild.getFolderList();
                    if (null != folderList) {
                        getOneLevelChildListener.onOneLevelChildReady();
                    } else {
//                        showToast("The project is empty.");
                    }
                } else {
                    folderList = null;
                    getOneLevelChildListener.onOneLevelChildReady();
                    Log.d(TAG, "no error, but the one level child is empty");
                }
            } else {
                valid = false;
            }

            Log.d(TAG, "oneLevelChild.getError_code: " + oneLevelChild.getError_code());
            Log.d(TAG, "oneLevelChild.getError_message: " + oneLevelChild.getError_message());

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
            responseBody = null;
        }
        return responseBody;
    }

    public boolean getValid(){
        return valid;
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
            oneLevelChild.setError_message(job.getString("error_messages"));

//            if (errorCode == 0) showToast("get one level child successfully");
            if (errorCode != 0) {
                showToast(oneLevelChild.getError_message().substring(2, oneLevelChild.getError_message().length() - 2));
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
            if (!data_array.isNull("folder_id")) {
                folder.setFolder_id(data_array.getString("folder_id"));
                Log.d(TAG, "parseFolder: setFolder_id()" + folder.getFolder_id());
            } if (!data_array.isNull("name")) {
                folder.setName(data_array.getString("name"));
            } if (!data_array.isNull("des")) {
                folder.setDes(data_array.getString("des"));
            } if (!data_array.isNull("created_userid")) {
                folder.setCreated_userid(data_array.getString("created_userid"));
            } if (!data_array.isNull("created_username")) {
                folder.setCreated_username(data_array.getString("created_username"));
            } if (!data_array.isNull("created_datetime")) {
                folder.setCreated_datetime(data_array.getString("created_datetime"));
            } if (!data_array.isNull("updated_userid")) {
                folder.setUpdated_userid(data_array.getString("updated_userid"));
            } if (!data_array.isNull("updated_username")) {
                folder.setUpdated_username(data_array.getString("updated_username"));
            } if (!data_array.isNull("updated_datetime")) {
                folder.setUpdated_datetime(data_array.getString("updated_datetime"));
            } if (!data_array.isNull("is_shared")) {
                folder.setIs_shared(data_array.getString("is_shared"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return folder;
    }

    public void showToast(String info){
        Toast toast = Toast.makeText(
                context,
                info,
                Toast.LENGTH_SHORT);
        toast.show();
    }

    protected ArrayList<Folder> getFolderList(){
        return folderList;
    }
}