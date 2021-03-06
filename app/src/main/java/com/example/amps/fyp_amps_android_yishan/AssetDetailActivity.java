package com.example.amps.fyp_amps_android_yishan;

import android.os.Bundle;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.amps.fyp_amps_android_yishan.BaseActivity;
import com.example.amps.fyp_amps_android_yishan.Settings;

public class AssetDetailActivity extends BaseActivity implements TabListener,
        Settings {
    private static String TAG = "AssetDetailActivity";
    RelativeLayout r;
    FragmentTransaction fragmentTra = null;
    AssetsPreviewFragment previewFragment;
    AssetsPropertiesFragment propertiesFragment;
    AssetsCommentsFragment commentsFragment;
    AssetsRevisionsFragment revisionsFragment;
    AssetsLogHistoryFragment logHistoryFragment;
    private String asset_id;
    private String project_id, folderId;
    private String videoUrl;
    private String asset_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "oncreate()");
        setContentView(R.layout.activity_asset_detail);
        settings = getSharedPreferences(SETTINGS, 0);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            asset_id = extras.getString("asset_id");
            project_id = extras.getString("project_id");
            folderId = extras.getString("folderId");
            videoUrl = extras.getString("videoUrl");
            asset_name = extras.getString("asset_name");
        }

        getActionBar().setDisplayShowTitleEnabled(true);
        if (null != asset_name) {
            setTitle(asset_name);
        } else {
            setTitle("Assets");
        }

        try {
            r = (RelativeLayout) findViewById(R.id.activity_asset_detail);
            fragmentTra = getFragmentManager().beginTransaction();
            ActionBar bar = getActionBar();
            bar.addTab(bar.newTab().setText("Preview").setTabListener(this));
            bar.addTab(bar.newTab().setText("Properties").setTabListener(this));
            bar.addTab(bar.newTab().setText("Comments").setTabListener(this));
            bar.addTab(bar.newTab().setText("Revisions").setTabListener(this));
            bar.addTab(bar.newTab().setText("Log History").setTabListener(this));

            bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
                    | ActionBar.DISPLAY_USE_LOGO);
            bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            bar.setDisplayShowHomeEnabled(true);
            bar.setDisplayShowTitleEnabled(false);
            bar.show();

        } catch (Exception e) {
            e.getMessage();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction arg1) {
        if (tab.getText().equals("Properties")) {
            try {
                r.removeAllViews();
            } catch (Exception e) {
            }
            propertiesFragment = new AssetsPropertiesFragment();
            propertiesFragment.setUserid(settings.getString("userid", null));
            propertiesFragment.setTokenid(settings.getString("tokenid", null));
            propertiesFragment.setAsset_id(asset_id);
            propertiesFragment.setProject_id(project_id);
            fragmentTra.addToBackStack(null);
            fragmentTra = getFragmentManager().beginTransaction();
            fragmentTra.add(r.getId(), propertiesFragment);
            fragmentTra.commit();
        } else if (tab.getText().equals("Preview")) {
            try {
                r.removeAllViews();
            } catch (Exception e) {
            }
            previewFragment = new AssetsPreviewFragment();
            previewFragment.setUserid(settings.getString("userid", null));
            previewFragment.setTokenid(settings.getString("tokenid", null));
            previewFragment.setFolderId(folderId);
            previewFragment.setAsset_id(asset_id);
            previewFragment.setProject_id(project_id);
            Log.d(TAG, "projectid pass to assets preview: " + project_id);
            previewFragment.setVideo_Url(videoUrl);
            fragmentTra.addToBackStack(null);
            fragmentTra = getFragmentManager().beginTransaction();
            fragmentTra.add(r.getId(), previewFragment);
            fragmentTra.commit();
        } else if (tab.getText().equals("Comments")) {
            try {
                r.removeAllViews();
            } catch (Exception e) {
            }
            commentsFragment = new AssetsCommentsFragment();
            commentsFragment.setUserid(settings.getString("userid", null));
            commentsFragment.setTokenid(settings.getString("tokenid", null));
            commentsFragment.setAsset_id(asset_id);
            commentsFragment.setProject_id(project_id);
            fragmentTra.addToBackStack(null);
            fragmentTra = getFragmentManager().beginTransaction();
            fragmentTra.add(r.getId(), commentsFragment);
            fragmentTra.commit();
        } else if (tab.getText().equals("Revisions")) {
            try {
                r.removeAllViews();
            } catch (Exception e) {
            }
            revisionsFragment = new AssetsRevisionsFragment();
            revisionsFragment.setUserid(settings.getString("userid", null));
            revisionsFragment.setTokenid(settings.getString("tokenid", null));
            revisionsFragment.setAsset_id(asset_id);
            revisionsFragment.setProject_id(project_id);
            fragmentTra.addToBackStack(null);
            fragmentTra = getFragmentManager().beginTransaction();
            fragmentTra.add(r.getId(), revisionsFragment);
            fragmentTra.commit();
        } else if (tab.getText().equals("Log History")) {
            try {
                r.removeAllViews();
            } catch (Exception e) {
            }
            logHistoryFragment = new AssetsLogHistoryFragment();
            logHistoryFragment.setUserid(settings.getString("userid", null));
            logHistoryFragment.setTokenid(settings.getString("tokenid", null));
            logHistoryFragment.setAsset_id(asset_id);
            logHistoryFragment.setProject_id(project_id);
            fragmentTra.addToBackStack(null);
            fragmentTra = getFragmentManager().beginTransaction();
            fragmentTra.add(r.getId(), logHistoryFragment);
            fragmentTra.commit();
        }
    }

    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.cardImage:
                    previewFragment.onClick(view);
                    break;
                case R.id.imageButtonUpload:
                    previewFragment.onClick(view);
                    break;
                case R.id.imageButtonAssign:
                    previewFragment.onClick(view);
                    break;
                case R.id.imageButtonDownload:
                    previewFragment.onClick(view);
                    break;
                case R.id.imageButtonDelete:
                    previewFragment.onClick(view);
                    break;
                default:
                    Log.e(TAG, "no matching in onClick()");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
