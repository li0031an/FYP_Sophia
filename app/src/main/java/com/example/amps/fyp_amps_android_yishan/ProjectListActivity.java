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
import android.widget.Toast;

public class ProjectListActivity extends BaseActivity implements Settings,
        View.OnClickListener {
    ArrayList<Project> projectArray;
    GetProjectInfo task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);
        setTitle("Projects");
        settings = getSharedPreferences(SETTINGS, 0);
        task = new GetProjectInfo(this, settings);
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

    public void onGetProjectInfoReady(){
        projectArray = task.projectArray;
        if (null != projectArray && projectArray.size() != 0) {
            View v = ProjectListActivity.this
                    .findViewById(android.R.id.content).getRootView();
            createTableRow(v);
        } else {
            showToast("No project found via GetProjectInfo");
        }
    }
    public void showToast(String info){
        Toast toast = Toast.makeText(
                ProjectListActivity.this,
                info,
                Toast.LENGTH_LONG);
        toast.show();
    }


}