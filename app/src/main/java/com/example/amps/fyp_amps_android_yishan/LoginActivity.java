package com.example.amps.fyp_amps_android_yishan;

import java.util.ArrayList;
import java.security.*;

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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends BaseActivity implements Settings {
    public static String TAG = "LoginActivity";
    public static String ERROR_MSG_INCORRECT_INPUT_INFO = "Incorrect username or password";
    private static final String SHARED_PREFERENCE_LOGIN_NAME = "preference_login";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String IS_REMEMBER = "isremember";
    private EditText etUserName;
    private EditText etPassword;
    private CheckBox cbIsRememberPassword;
    ProgressDialog dialog;
    String szUsername = "";
    String szPassword = "";
    Boolean isRememberPassword;
    int error_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etUserName = (EditText) findViewById(R.id.editTextUsername);
        etPassword = (EditText) findViewById(R.id.editTextPassword);
        cbIsRememberPassword = (CheckBox) findViewById(R.id.isRememberPassword);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
		/* super.onCreateOptionsMenu(menu); */
        SharedPreferences loginPreferences = getSharedPreferences(SHARED_PREFERENCE_LOGIN_NAME,
                Context.MODE_PRIVATE);

        //  read saved username & password next time opening application
        etUserName.setText(loginPreferences.getString(USERNAME, ""));
        etPassword.setText(loginPreferences.getString(PASSWORD, ""));
        if (loginPreferences.getBoolean(IS_REMEMBER, false)) {
            cbIsRememberPassword.setChecked(true);
        } else {
            cbIsRememberPassword.setChecked(false);
        }
        return true;
    }

    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.buttonLogin:
                    authenticate();
                    break;
                case R.id.buttonCancel:
                    LoginActivity.this.finish();
                    showToast("System is exiting...");
                    System.exit(0);
                    break;
                default:
                    LoginActivity.this.finish();
                    break;
            }
        } catch (Exception e) {
        }
    }

    private void authenticate() {
        szUsername = etUserName.getText().toString().trim();
        szPassword = etPassword.getText().toString().trim();
        isRememberPassword = cbIsRememberPassword.isChecked();

        if (szUsername == null | szUsername.length()<1) {
            showToast("Please enter user name.");
        } else if (szPassword == null | szPassword.length()<1) {
            showToast("Please enter password.");
        } else {
            AuthenticateUser task = new AuthenticateUser();
            task.execute();
        }
    }

    // -TODO: encode the password before saving, decode the password when needed
    private void onSuccessfulLogin() {
        if (null == szUsername || szUsername.length() == 0) {
            Log.d(TAG, "szUsername is null in onSuccessfulLogin()");
            return;
        } else if (null == szPassword || szPassword.length() == 0) {
            Log.d(TAG, "szPassword is null in onSuccessfulLogin()");
            return;
        } else {
            if (isRememberPassword) {
                SharedPreferences loginPreferences = getSharedPreferences(SHARED_PREFERENCE_LOGIN_NAME, Context.MODE_PRIVATE);
                loginPreferences.edit().putString(USERNAME, szUsername)
                        .putString(PASSWORD, szPassword)
                        .putBoolean(IS_REMEMBER, isRememberPassword).commit();
            } else {
                SharedPreferences loginPreferences = getSharedPreferences(SHARED_PREFERENCE_LOGIN_NAME, Context.MODE_PRIVATE);
                loginPreferences.edit().clear().commit();
            }

            Intent intent = new Intent(LoginActivity.this, ProjectListActivity.class);
            intent.putExtra("username", etUserName.getText().toString().trim());
            startActivity(intent);
        }
    }

    public class AuthenticateUser extends AsyncTask<Object, Object, Object> {

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(LoginActivity.this,
                    "Authenticating user", "Please wait...", true);
        }

        @Override
        protected String doInBackground(Object... arg0) {
            return login();

        }

        @Override
        protected void onPostExecute(Object result) {
            dialog.dismiss();
            if (null == result) {
                showToast("Please make sure internet connection is available.");
            } else {
                parseJSONResponse((String) result);
            }
        }

        public String login() {
            String responseBody = "";
            // Instantiate an HttpClient
            Log.d(TAG, "szUsername: " + szUsername);
            Log.d(TAG, "szPassword: " + szPassword);
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(SZAAPIURL + "Authenticate");

            // Post parameters
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("username", szUsername));
            postParameters.add(new BasicNameValuePair("password",
                    hash(szPassword)));

            // Instantiate a POST HTTP method
            try {
                httppost.setEntity(new UrlEncodedFormEntity(postParameters));
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                responseBody = httpclient.execute(httppost, responseHandler);
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
                e.printStackTrace();
                responseBody = null;
            }
            return responseBody;
        }

        public String hash(String plaintext) {
            try {
                MessageDigest md = java.security.MessageDigest
                        .getInstance("MD5");
                byte[] array = md.digest(plaintext.getBytes());
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < array.length; ++i) {
                    sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
                            .substring(1, 3));
                }
                return sb.toString();
            } catch (java.security.NoSuchAlgorithmException e) {
                Log.d(TAG, e.getMessage());
            }
            return null;

        }

        public void parseJSONResponse(String responseBody) {
            JSONArray json;
            Log.d(TAG, "responseBody: " + responseBody.toString());
            try {
                json = new JSONArray(responseBody);
                JSONObject job = json.getJSONObject(0);
                error_code = job.getInt("error_code");
                SharedPreferences settings = getSharedPreferences(SETTINGS, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("userid", job.getString("userid"));
                editor.putString("tokenid", job.getString("tokenid"));
                Log.d(TAG, "userid: " + job.getString("userid"));
                Log.d(TAG, "tokenid: " + job.getString("tokenid"));
                editor.commit();
                if (error_code == 0) {
                    onSuccessfulLogin();
                }
                else {
                    Log.d(TAG, "error_code: " + error_code);
                    showToast(ERROR_MSG_INCORRECT_INPUT_INFO);
                }
            } catch (JSONException e) {
                Log.d(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void showToast(String info){
        Toast toast = Toast.makeText(
                LoginActivity.this,
                info,
                Toast.LENGTH_LONG);
        toast.show();
    }
}
