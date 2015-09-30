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


public class ProjectDetailsActivity extends BaseActivity implements Settings {

    private static String TAG = "ProjectDetailsActivity";
    private String projectId;
    ArrayList<Folder> folderList = new ArrayList<Folder>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_details);
        setTitle("Project folders");
        Intent intent = getIntent();
        projectId = intent.getStringExtra("project_id");
        settings = getSharedPreferences(SETTINGS, 0);
        GetRootFolderId task = new GetRootFolderId();
        task.execute();

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

    public class GetRootFolderId extends AsyncTask<Object, Object, Object> {

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(ProjectDetailsActivity.this,
                    "Retrieving Projects", "Please wait...", true);
        }

        @Override
        protected String doInBackground(Object... arg0) {
            return getRootId();

        }

        @Override
        protected void onPostExecute(Object result) {
            dialog.dismiss();
            RootFolderId rootFolderId = parseRootFolderId((String) result);

            Log.d(TAG, "getRootId.getError_code: " + rootFolderId.getError_code());
            Log.d(TAG, "getRootId.getError_message: " + rootFolderId.getError_message());

            Folder folder = rootFolderId.getRootFolderInfo();
            if (null != folder) {
                String rootId = folder.folder_id;
                if (null != rootId) {
                    GetOneLevelChild task = new GetOneLevelChild(rootId);
                    task.execute();
                }
            }else {
                    showToast("Sorry cannot get one level child because root id not found.");
                    Log.d(TAG, "rootId not found");
            }

            //Log.d(TAG, "getRootId: " + rootFolderId.getRootFolderInfo().getFolder_id());

//            View v = ProjectDetailsActivity.this
//                    .findViewById(android.R.id.content).getRootView();
            //createTableRow(v);
        }

        public String getRootId() {
            String responseBody = "";
            // Instantiate an HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(SZAAPIURL + "getRootFolder");

            // Post parameters
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("tokenid", settings
                    .getString("tokenid", null)));
            postParameters.add(new BasicNameValuePair("userid", settings
                    .getString("userid", null)));
            postParameters.add(new BasicNameValuePair("projectid", projectId));

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

        public RootFolderId parseRootFolderId(String responseBody) {
            JSONArray json;
            JSONObject job, data_array;
            RootFolderId rootFolderId = new RootFolderId();
            try {
                json = new JSONArray(responseBody);
                job = json.getJSONObject(0);
                int errorCode= job.getInt("error_code");
                rootFolderId.setError_code(String.valueOf(errorCode));
//                rootFolderId.setError_message(job.getJSONArray("error_messages").getString(0));
                rootFolderId.setError_message(job.getString("error_messages"));

                if (errorCode == 0) showToast("get root folder id successfully");
                if (errorCode != 0) {
                    showToast(rootFolderId.getError_message());
                    return rootFolderId;
                }
                data_array = job.getJSONObject("data_array");
                Folder folder = parseFolder(data_array);
                if (null != folder) {
                    rootFolderId.setRootFolderInfo(folder);
                } else {
                    Log.e(TAG, "root folder id is null unexpectedly");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return rootFolderId;
        }
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
                ProjectDetailsActivity.this,
                info,
                Toast.LENGTH_LONG);
        toast.show();
    }

    //get one level child

    public class GetOneLevelChild extends AsyncTask<Object, Object, Object> {

        private String rootId;
        GetOneLevelChild(String rootId) {
            this.rootId = rootId;
        }
        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(ProjectDetailsActivity.this,
                    "Retrieving Projects", "Please wait...", true);
        }

        @Override
        protected String doInBackground(Object... arg0) {
            return getOneLevelChildObject();

        }

        @Override
        protected void onPostExecute(Object result) {
            dialog.dismiss();
            OneLevelChild oneLevelChild = parseOneLevelChildObject((String) result);
            folderList = oneLevelChild.getFolderList();
            Log.d(TAG, "oneLevelChild.getError_code: " + oneLevelChild.getError_code());
            Log.d(TAG, "oneLevelChild.getError_message: " + oneLevelChild.getError_message());

            //Log.d(TAG, "getRootId: " + rootFolderId.getRootFolderInfo().getFolder_id());


            if (null != folderList) {
                Log.d(TAG, "folderList.getFolder_id: " + folderList.get(0).getFolder_id());
                View v = ProjectDetailsActivity.this
                        .findViewById(android.R.id.content).getRootView();
                createTableRow(v);
            }

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
            postParameters.add(new BasicNameValuePair("parent_id", rootId));

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
    }
}
