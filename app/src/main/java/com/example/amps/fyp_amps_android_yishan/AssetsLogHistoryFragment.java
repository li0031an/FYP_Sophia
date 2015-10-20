package com.example.amps.fyp_amps_android_yishan;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AssetsLogHistoryFragment extends Fragment implements Settings {
    ProgressDialog dialog;
    String userid;
    String tokenid;
    String asset_id;
    String project_id;

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
        return inflater.inflate(R.layout.fragment_assets_log_history, container,
                false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
}
