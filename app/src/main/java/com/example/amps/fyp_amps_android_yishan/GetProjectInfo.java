package com.example.amps.fyp_amps_android_yishan;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.view.View;
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

public class GetProjectInfo extends AsyncTask<Object, Object, Object> implements Settings {

    ProgressDialog dialog;
    GetProjectInfoListener getProjectInfoListener;
    Context context;
    SharedPreferences settings;
    ArrayList<Project> projectArray = new ArrayList<Project>();

    public GetProjectInfo(GetProjectInfoListener getProjectInfoListener,
            Context context, SharedPreferences settings) {
        this.getProjectInfoListener = getProjectInfoListener;
        this.settings = settings;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(context,
                "Retrieving Projects", "Please wait...", true);
    }

    @Override
    protected String doInBackground(Object... arg0) {
        return retrieveProjects();

    }

    @Override
    protected void onPostExecute(Object result) {
        dialog.dismiss();
        projectArray = parseJSONResponse((String) result);
        if (null != projectArray && 0 != projectArray.size()) {
            getProjectInfoListener.onGetProjectInfoReady();
        } else {
            showToast("you do not admin any projects.");
        }
    }

    public String retrieveProjects() {
        String responseBody = "";
        // Instantiate an HttpClient
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(SZAAPIURL + "getProjectInfo");

        // Post parameters
        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("tokenid", settings
                .getString("tokenid", null)));
        postParameters.add(new BasicNameValuePair("userid", settings
                .getString("userid", null)));

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

    public ArrayList<Project> parseJSONResponse(String responseBody) {
        JSONArray json, data_array;
        JSONObject job;
        Project p;
        ArrayList<Project> array = new ArrayList<Project>();
        try {
            json = new JSONArray(responseBody);
            job = json.getJSONObject(0);
            int errorCode= job.getInt("error_code");
            String errorMsg = job.getString("error_messages");

            if (errorCode == 0) showToast("get one level child successfully");
            if (errorCode != 0) {
                showToast(errorMsg.substring(2, errorMsg.length() - 2));
                return array;
            }
            data_array = job.getJSONArray("data_array");
            for (int i = 0; i < data_array.length(); i++) {
                JSONObject dataJob = new JSONObject(data_array.getString(i));
                p = new Project();
                if (!dataJob.isNull("project_id")) {
                    p.setProject_id(dataJob.getString("project_id"));
                } if (!dataJob.isNull("name")) {
                    p.setName(dataJob.getString("name"));
                } if (!dataJob.isNull("des")) {
                    p.setDes(dataJob.getString("des"));
                } if (!dataJob.isNull("estimated_datestart")) {
                    p.setEstimated_datestart(dataJob.getString("estimated_datestart"));
                } if (!dataJob.isNull("estimated_dateend")) {
                    p.setEstimated_dateend(dataJob.getString("estimated_dateend"));
                } if (!dataJob.isNull("actual_datestart")) {
                    p.setActual_datestart(dataJob.getString("actual_datestart"));
                } if (!dataJob.isNull("actual_dateend")) {
                    p.setActual_dateend(dataJob.getString("actual_dateend"));
                } if (!dataJob.isNull("duration")) {
                    p.setDuration(dataJob.getString("duration"));
                } if (!dataJob.isNull("created_userid")) {
                    p.setCreated_userid(dataJob.getString("created_userid"));
                } if (!dataJob.isNull("created_datetime")) {
                    p.setCreated_datetime(dataJob.getString("created_datetime"));
                } if (!dataJob.isNull("updated_userid")) {
                    p.setUpdated_userid(dataJob.getString("updated_userid"));
                } if (!dataJob.isNull("updated_datetime")) {
                    p.setUpdated_datetime(dataJob.getString("updated_datetime"));
                }
                array.add(p);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } return array;
    }

    protected ArrayList<Project> getProjectArray(){
        return projectArray;
    }

    public void showToast(String info){
        Toast toast = Toast.makeText(
                context,
                info,
                Toast.LENGTH_LONG);
        toast.show();
    }
}