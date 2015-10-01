package com.example.amps.fyp_amps_android_yishan;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.view.View;

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
    String error_code;
    ProjectListActivity projectListActivity;
    SharedPreferences settings;
    ArrayList<Project> projectArray = new ArrayList<Project>();

    public GetProjectInfo(ProjectListActivity projectListActivity, SharedPreferences settings) {
        this.projectListActivity = projectListActivity;
        this.settings = settings;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(projectListActivity,
                "Retrieving Projects", "Please wait...", true);
    }

    @Override
    protected String doInBackground(Object... arg0) {
        return retrieveProjects();

    }

    @Override
    protected void onPostExecute(Object result) {
        dialog.dismiss();
        parseJSONResponse((String) result);
        projectListActivity.onGetProjectInfoReady();
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

    public void parseJSONResponse(String responseBody) {
        JSONArray json, data_array;
        JSONObject job;
        Project p;
        try {
            json = new JSONArray(responseBody);
            job = json.getJSONObject(0);
            data_array = job.getJSONArray("data_array");
            for (int i = 0; i < data_array.length(); i++) {
                JSONObject dataJob = new JSONObject(data_array.getString(i));

                p = new Project();
                p.setProject_id(dataJob.getString("project_id"));
                p.setName(dataJob.getString("name"));
                p.setDes(dataJob.getString("des"));
                p.setEstimated_datestart(dataJob
                        .getString("estimated_datestart"));
                p.setEstimated_dateend(dataJob
                        .getString("estimated_dateend"));
                p.setActual_datestart(dataJob.getString("actual_datestart"));
                p.setActual_dateend(dataJob.getString("actual_dateend"));
                p.setDuration(dataJob.getString("duration"));
                p.setCreated_userid(dataJob.getString("created_userid"));
                p.setCreated_datetime(dataJob.getString("created_datetime"));
                p.setUpdated_userid(dataJob.getString("updated_userid"));
                p.setUpdated_datetime(dataJob.getString("updated_datetime"));
                projectArray.add(p);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}