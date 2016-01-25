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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
        View.OnClickListener, GetProjectInfoListener {
    ArrayList<Object> projectArray = new ArrayList<Object>();
    GetProjectInfo task;
    private static String TAG = "ProjectListActivity";

    //////
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    //////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);
        setTitle("Projects");
        settings = getSharedPreferences(SETTINGS, 0);
        task = new GetProjectInfo(this, ProjectListActivity.this, settings);
        task.execute();
        /////
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);


        // Code to Add an item with default animation
        //((RecyclerViewAdapter) mAdapter).addItem(obj, index);

        // todo-- Code to remove an item with default animation
        //((RecyclerViewAdapter) mAdapter).deleteItem(index);
        /////
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.d(TAG, "onStart()");
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        Log.d(TAG, "onRestart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != mAdapter) {
            ((RecyclerViewAdapter) mAdapter).setOnItemClickListener(new RecyclerViewAdapter.MyClickListener() {
                @Override
                public void onItemClick(int position, View v) {
//                    Log.i(TAG, " Clicked on Item ");
                    displayFolderList(position);
                }
            });
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d(TAG, "onPause()");
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.d(TAG, "onStop()");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

//    public void createTableRow(View v) {
//        TableLayout tl = (TableLayout) findViewById(R.id.tableRowProjectList);
//
//        for (int i = 0; i < projectArray.size(); i++) {
//            Project p = projectArray.get(i);
//            TableRow tr = new TableRow(this);
//
//            if (i % 2 == 0)
//                tr.setBackgroundColor(Color.WHITE);
//
//            tr.setPadding(0, 16, 0, 16);
//            TextView textViewName = new TextView(this);
//            textViewName.setText(p.getName());
//            textViewName.setGravity(Gravity.CENTER_VERTICAL);
//            textViewName.setPadding(16, 16, 16, 16);
//            textViewName.setId(i);
//            textViewName.setOnClickListener(this);
//
//            tr.addView(textViewName, new TableRow.LayoutParams(0,
//                    LayoutParams.WRAP_CONTENT, (float) 1));
//
//            tl.addView(tr, new TableLayout.LayoutParams(
//                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
//        }
//    }

    @Override
    public void onClick(View view) {
        displayFolderList(view.getId());
    }

    protected void displayFolderList(int id){
        Object object = projectArray.get(id);
        if (object instanceof Project) {
            Project p = (Project) projectArray.get(id);
            Intent intent = new Intent(ProjectListActivity.this, ProjectDetailsActivity.class);
            intent.putExtra("project_id", p.getProject_id());
            intent.putExtra("project_name", p.getName());
            startActivity(intent);
        } else {
            Log.e(TAG, "object id: "+ id + " is not project in displayFolderList");
        }
    }

    @Override
    public void onGetProjectInfoReady(){
        ArrayList<Project> arrayProjectArray = task.getProjectArray();
        if (null != arrayProjectArray && arrayProjectArray.size() != 0) {
            View v = ProjectListActivity.this
                    .findViewById(android.R.id.content).getRootView();
//            createTableRow(v);
            for (int i = 0; i<arrayProjectArray.size(); i++) {
                projectArray.add((Object) arrayProjectArray.get(i));
            }
            mAdapter = new RecyclerViewAdapter(this,projectArray, null);
            ((RecyclerViewAdapter) mAdapter).setOnItemClickListener(new RecyclerViewAdapter.MyClickListener() {
                @Override
                public void onItemClick(int position, View v) {
//                    Log.i(TAG, " Clicked on Item ");
                    displayFolderList(position);
                }
            });
            mRecyclerView.setAdapter(mAdapter);
        } else {
//            showToast("No project found via GetProjectInfo");
            showToast("No project found");
        }
    }
    public void showToast(String info){
        Toast toast = Toast.makeText(
                ProjectListActivity.this,
                info,
                Toast.LENGTH_SHORT);
        toast.show();
    }


}