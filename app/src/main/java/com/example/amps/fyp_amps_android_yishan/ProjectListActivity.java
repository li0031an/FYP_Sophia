package com.example.amps.fyp_amps_android_yishan;

import java.util.ArrayList;

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

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

public class ProjectListActivity extends BaseActivity implements Settings,
        View.OnClickListener {
    ProgressDialog dialog;
    String error_code;
    ArrayList<Project> projectArray = new ArrayList<Project>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);
        setTitle("Projects");
        settings = getSharedPreferences(SETTINGS, 0);
        GetProjectInfo task = new GetProjectInfo();
        task.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void createTableRow(View v) {
        TableLayout tl = (TableLayout) findViewById(R.id.tableRowProjectList);

        for (int i = 0; i < projectArray.size(); i++) {
            Project p = projectArray.get(i);
            TableRow tr = new TableRow(this);

            if (i % 2 == 0)
                tr.setBackgroundColor(Color.WHITE);

            tr.setPadding(0, 16, 0, 16);
            TextView textViewName = new TextView(this);
            textViewName.setText(p.getName());
            textViewName.setGravity(Gravity.CENTER_VERTICAL);
            textViewName.setPadding(16, 16, 16, 16);
            textViewName.setId(i);
            textViewName.setOnClickListener(this);


            tr.addView(textViewName, new TableRow.LayoutParams(0,
                    LayoutParams.WRAP_CONTENT, (float) 1));

            tl.addView(tr, new TableLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
    }

    @Override
    public void onClick(View view) {
        Project p = projectArray.get(view.getId());
        Intent intent = new Intent(ProjectListActivity.this, ProjectDetailsActivity.class);
        intent.putExtra("project_id", p.getProject_id());
        startActivity(intent);
    }

    public class GetProjectInfo extends AsyncTask<Object, Object, Object> {

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(ProjectListActivity.this,
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

            View v = ProjectListActivity.this
                    .findViewById(android.R.id.content).getRootView();
            createTableRow(v);
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
}