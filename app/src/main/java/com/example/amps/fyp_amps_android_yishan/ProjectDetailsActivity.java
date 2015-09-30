package com.example.amps.fyp_amps_android_yishan;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.audiofx.BassBoost;
import android.os.AsyncTask;
import android.provider.DocumentsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
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
        Log.d(TAG, "onNewIntent - project_id" + projectId );
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
            RootFolderId rootFolderId = parseJSONResponse((String) result);

            Log.d(TAG, "getRootId.getError_code: " + rootFolderId.getError_code());
            Log.d(TAG, "getRootId.getError_message: " + rootFolderId.getError_message());
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

        public RootFolderId parseJSONResponse(String responseBody) {
            JSONArray json;
            JSONObject job, data_array;
            RootFolderId rootFolderId = new RootFolderId();
            try {
                json = new JSONArray(responseBody);
                job = json.getJSONObject(0);
                int errorCode= job.getInt("error_code");
                rootFolderId.setError_code(String.valueOf(errorCode));
                rootFolderId.setError_message(job.getJSONArray("error_messages").getString(0));

                if (errorCode == 0) showToast("get root folder id successfully");
                if (errorCode != 0) {
                    showToast(rootFolderId.getError_message());
                    return rootFolderId;
                }

                data_array = job.getJSONObject("data_array");
                Folder folder = new Folder();
                folder.setFolder_id(data_array.getString("folder_id"));
                folder.setName(data_array.getString("name"));
                folder.setDes(data_array.getString("des"));
                folder.setCreated_userid(data_array.getString("created_userid"));
                folder.setCreated_username(data_array.getString("created_username"));
                folder.setCreated_datetime(data_array.getString("created_datetime"));
                folder.setUpdated_userid(data_array.getString("updated_userid"));
                folder.setUpdated_username(data_array.getString("updated_username"));
                folder.setUpdated_datetime(data_array.getString("updated_datetime"));
                folder.setIs_shared(data_array.getString("is_shared"));

                rootFolderId.setRootFolderInfo(folder);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return rootFolderId;
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
