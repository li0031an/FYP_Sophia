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
import java.util.Objects;

public class AsyncTaskGetSharedResource extends AsyncTask<Object, Object, Object> implements Settings {
    private static String TAG = "AsyncTaskGetSharedResource";
    GetSharedResourceListener getSharedResourceListener;
    Context context;
    ProgressDialog dialog;
    SharedPreferences settings;
    String projectId;
    ArrayList<Asset> assetList = new ArrayList<Asset>();
    ArrayList<Folder> folderList = new ArrayList<Folder>();

    public AsyncTaskGetSharedResource(GetSharedResourceListener getSharedResourceListener, Context context, SharedPreferences settings, String projectId) {
        this.getSharedResourceListener = getSharedResourceListener;
        this.context = context;
        this.settings = settings;
        this.projectId = projectId;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(context,
                "Retrieving project files", "Please wait...", true);
    }

    @Override
    protected String doInBackground(Object... arg0) {
        return getSharedResource();

    }

    @Override
    protected void onPostExecute(Object result) {
        dialog.dismiss();
        Log.d(TAG, "onPostExecute starts");
        if (null != result) {
            parseSharedResource((String) result);
            if ((null != assetList && assetList.size() != 0) || (null != folderList && folderList.size() != 0)) {
                getSharedResourceListener.onGetSharedResourceReady();
            } else {
//                showToast("The folder is empty");
            }
        }
    }

    protected ArrayList<Folder> getFolderList() {
        return folderList;
    }

    protected ArrayList<Asset> getAssetList() {
        return assetList;
    }

    public String getSharedResource() {
        String responseBody = "";
        // Instantiate an HttpClient
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(SZAAPIURL + "getSharedResource");

        // Post parameters
        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("projectid", projectId));
        Log.d(TAG, "projectid: " + projectId);
        String selectAttributes = "[resourceid], [resource_name], [des], [parent_lst], [is_asset], [show_parent]";
        postParameters.add(new BasicNameValuePair("select", selectAttributes));
        postParameters.add(new BasicNameValuePair("shared_targetid", settings
                .getString("userid", null)));
        postParameters.add(new BasicNameValuePair("tokenid", settings
                .getString("tokenid", null)));
        Log.d(TAG, "tokenid: " + settings.getString("tokenid", null));
        postParameters.add(new BasicNameValuePair("userid", settings
                .getString("userid", null)));
        Log.d(TAG, "userid: " + settings.getString("userid", null));
        Log.d(TAG, "shared_targetid: " + settings.getString("userid", null));

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

    public void parseSharedResource(String responseBody) {
        if (null == responseBody || 0 == responseBody.length()) return;
        JSONArray json, data_array;
        JSONObject job;
        try {
            json = new JSONArray(responseBody);
            job = json.getJSONObject(0);
            int errorCode = job.getInt("error_code");
//            if (errorCode == 0) showToast("get one level child successfully");
            if (errorCode != 0) {
                String errorMsg = job.getString("error_messages");
                if (null != errorMsg && !errorMsg.equalsIgnoreCase("[]")) {
                    showToast(errorMsg.substring(2, errorMsg.length() - 2));
                } else {
                    showToast("Error: cannot get shared resources.");
                }
                assetList = null;
                folderList = null;
            }

            data_array = job.getJSONArray("data_array");
            Log.d(TAG, "JSON: " + data_array.toString());
            if (null != data_array) {
                for (int i = 0; i < data_array.length(); i++) {
                    JSONObject object = data_array.getJSONObject(i);
                    if (!object.isNull("is_asset")) {
                        int type = object.getInt("is_asset");
                        Log.d(TAG, "parseObject: is_asset()" + type);
                        if (1 == type) {
                            Asset tempAsset = new Asset();
                            if (!object.isNull("resourceid")) {
                                tempAsset.setAsset_id(object.getString("resourceid"));
                                Log.d(TAG, "parseObject: resourceid()" + tempAsset.getAsset_id());
                            }
                            if (!object.isNull("resource_name")) {
                                tempAsset.setName(object.getString("resource_name"));
                                Log.d(TAG, "asset name: " + tempAsset.getName());
                            }
                            if (null != tempAsset)
                                assetList.add(tempAsset);
                        } else if (0 == type) {
                            Folder tempFolder = new Folder();
                            if (!object.isNull("resourceid")) {
                                tempFolder.setFolder_id(object.getString("resourceid"));
                                Log.d(TAG, "parseObject: resourceid()" + tempFolder.getFolder_id());
                            }
                            if (!object.isNull("resource_name")) {
                                tempFolder.setName(object.getString("resource_name"));
                                Log.d(TAG, "folder name: " + tempFolder.getName());
                            }
                            if (null != tempFolder)
                                folderList.add(tempFolder);
                        } else {
                            Log.e(TAG, "is_asset is invalid: " + type);
                        }
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void showToast(String info) {
        Toast toast = Toast.makeText(
                context,
                info,
                Toast.LENGTH_SHORT);
        toast.show();
    }

}
