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

//import com.example.amps.ProjectInformationFragment.GetCreatedUserInfo;
//import com.example.amps.ProjectInformationFragment.GetProjectInfo;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class AssetsPropertiesFragment extends Fragment implements Settings {
    ProgressDialog dialog;
    String userid;
    String tokenid;
    String asset_id;
    String project_id;
    Asset a = new Asset();
    EditText editTextAssetName;
    EditText editTextAssignedUser;
    EditText editTextTrackingStatus;
    EditText editTextEstimatedStart;
    EditText editTextEstimatedEnd;
    EditText editTextActualStart;
    EditText editTextActualEnd;

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public void setTokenid(String tokenid) {
        this.tokenid = tokenid;
    }

    public void setAsset_id(String asset_id) {
        this.asset_id = asset_id;
    }

    public void setProject_id(String project_id) {
        this.project_id = project_id;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_assets_properties, container,
                false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        editTextAssetName = (EditText) getActivity().findViewById(R.id.editTextAssetName);
        editTextAssignedUser = (EditText) getActivity().findViewById(R.id.editTextAssignedUser);
        editTextAssignedUser = (EditText) getActivity().findViewById(R.id.editTextAssignedUser);
        editTextTrackingStatus = (EditText) getActivity().findViewById(R.id.editTextTrackingStatus);
        editTextEstimatedStart = (EditText) getActivity().findViewById(R.id.editTextEstimatedStart);
        editTextEstimatedEnd = (EditText) getActivity().findViewById(R.id.editTextEstimatedEnd);
        editTextActualStart = (EditText) getActivity().findViewById(R.id.editTextActualStart);
        editTextActualEnd = (EditText) getActivity().findViewById(R.id.editTextActualEnd);
        GetAssetInfo task = new GetAssetInfo();
        task.execute();
    }

    public class GetAssetInfo extends AsyncTask<Object, Object, Object> {

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(
                    AssetsPropertiesFragment.this.getActivity(),
                    "Retrieving Asset Information", "Please wait...", true);
        }

        @Override
        protected String doInBackground(Object... arg0) {
            return retrieveProject();
        }

        @Override
        protected void onPostExecute(Object result) {
            dialog.dismiss();
            parseJSONResponse((String) result);
            editTextAssetName.setText(a.getName());
            editTextAssignedUser.setText(a.getAssigned_userid());
            editTextTrackingStatus.setText(a.getTracking_status());
            editTextEstimatedStart.setText(a.getEstimated_datestart());
            editTextEstimatedEnd.setText(a.getEstimated_dateend());
            editTextActualStart.setText(a.getActual_datestart());
            editTextActualEnd.setText(a.getActual_dateend());
			/*GetCreatedUserInfo task = new GetCreatedUserInfo();
			task.execute();*/
        }

        public String retrieveProject() {
            String responseBody = "";
            // Instantiate an HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(SZAAPIURL + "getAsset");

            // Post parameters
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("tokenid", tokenid));
            postParameters.add(new BasicNameValuePair("userid", userid));
            postParameters.add(new BasicNameValuePair("projectid", project_id));
            postParameters.add(new BasicNameValuePair("select",
                    "[asset_id], [name], [ext], [estimated_datestart], [estimated_dateend], [tags], [trash], [created_userid], [created_datetime], [updated_userid], [updated_datetime], [actual_datestart], [actual_dateend]"));
            postParameters.add(new BasicNameValuePair("condition", "[asset_id] IN ('" + asset_id + "')"));

            postParameters.add(new BasicNameValuePair("start_pos", "1"));
            postParameters.add(new BasicNameValuePair("end_pos", "5"));

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
            try {
                json = new JSONArray(responseBody);
                job = json.getJSONObject(0);
                data_array = job.getJSONArray("data_array");
                JSONObject dataJob = new JSONObject(data_array.getString(0));
                a.setName(dataJob.getString("name"));
                a.setAssigned_userid(dataJob.getString("assigned_userid"));
                a.setTracking_status(dataJob.getString("tracking_status"));
                a.setEstimated_datestart(dataJob.getString("estimated_datestart"));
                a.setEstimated_dateend(dataJob.getString("estimated_dateend"));
                a.setActual_datestart(dataJob.getString("actual_datestart"));
                a.setActual_dateend(dataJob.getString("actual_dateend"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
