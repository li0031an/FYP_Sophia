package com.example.amps.fyp_amps_android_yishan;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
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


public class ProjectDetailsActivity extends BaseActivity implements Settings, GetRootFolderIdListener, GetOneLevelChildListener {

    private static String TAG = "ProjectDetailsActivity";
    private String projectId;
    private Folder rootFolder;
    private ArrayList<Folder> folderList = new ArrayList<Folder>();
    GetRootFolderId getRootFolderId;
    GetOneLevelChild getOneLevelChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_details);
        setTitle("Project folders");
        Intent intent = getIntent();
        projectId = intent.getStringExtra("project_id");
        settings = getSharedPreferences(SETTINGS, 0);
        getRootFolderId = new GetRootFolderId(this, ProjectDetailsActivity.this, settings, projectId);
        getRootFolderId.execute();

    }

    @Override
    protected void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);
        setIntent(newIntent);
        projectId = newIntent.getStringExtra("project_id");
        Log.d(TAG, "onNewIntent - project_id" + projectId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_project_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onGetRootFolderIdReady(){
        rootFolder = getRootFolderId.getRootFolder();
        if (null != rootFolder) {
            showToast("root folder id has not been implemented.");
            String rootId = rootFolder.folder_id;
            if (null != rootId) {
                getOneLevelChild = new GetOneLevelChild(this, ProjectDetailsActivity.this, settings, rootId, projectId);
                getOneLevelChild.execute();
            }
        }else {
            showToast("Sorry cannot get one level child because root id not found.");
            Log.d(TAG, "rootId not found");
        }
    }

    @Override
    public void onOneLevelChildReady(){
        folderList = getOneLevelChild.getFolderList();
        if (null != folderList) {
            Log.d(TAG, "folderList.getFolder_id: " + folderList.get(0).getFolder_id());
            View v = ProjectDetailsActivity.this
                    .findViewById(android.R.id.content).getRootView();
            createTableRow(v);
        }
    }

    public void createTableRow(View v) {
        TableLayout tl = (TableLayout) findViewById(R.id.tableRowFolderList);

        for (int i = 0; i < folderList.size(); i++) {
            Folder folder = folderList.get(i);
            TableRow tr = new TableRow(this);

            if (i % 2 == 0)
                tr.setBackgroundColor(Color.WHITE);

            tr.setPadding(0, 16, 0, 16);
            TextView textViewName = new TextView(this);
            textViewName.setText(folder.getName());
            textViewName.setGravity(Gravity.CENTER_VERTICAL);
            textViewName.setPadding(16, 16, 16, 16);
            textViewName.setId(i);
            //todo -- onclick
            //textViewName.setOnClickListener(this);


            tr.addView(textViewName, new TableRow.LayoutParams(0,
                    TableRow.LayoutParams.WRAP_CONTENT, (float) 1));

            tl.addView(tr, new TableLayout.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        }
    }


    public void showToast(String info){
        Toast toast = Toast.makeText(
                ProjectDetailsActivity.this,
                info,
                Toast.LENGTH_LONG);
        toast.show();
    }

}
