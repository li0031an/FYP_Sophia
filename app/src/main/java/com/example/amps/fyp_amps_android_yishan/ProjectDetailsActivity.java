package com.example.amps.fyp_amps_android_yishan;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class ProjectDetailsActivity extends BaseActivity implements Settings, View.OnClickListener,
        GetRootFolderIdListener, GetOneLevelChildListener, GetAssetListener {

    private static String TAG = "ProjectDetailsActivity";
    private String projectId;
    private String rootFolderId;
    private Folder rootFolder;
    private ArrayList<Object> folderList = new ArrayList<Object>();
    private ArrayList<Object> assetList = new ArrayList<Object>();
    GetRootFolderId getRootFolderId;
    GetOneLevelChild getOneLevelChild;
    GetAsset getAsset;
    GetAssetDetail getAssetDetail;
    //////
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    //////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_details);
        setTitle("Project folders");
        settings = getSharedPreferences(SETTINGS, 0);
        Intent intent = getIntent();
        projectId = intent.getStringExtra("project_id");
        String intentRootId = intent.getStringExtra("rootFolderId");
        if (null != intentRootId) {
            rootFolderId = intentRootId;
            Log.d(TAG, "intent - rootFolderId" + rootFolderId);
            getAsset = new GetAsset(this, ProjectDetailsActivity.this, settings, rootFolderId, projectId);
            getAsset.execute();
        } else {
            getRootFolderId = new GetRootFolderId(this, ProjectDetailsActivity.this, settings, projectId);
            getRootFolderId.execute();
        }
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
    protected void onResume() {
        super.onResume();
        if (null != mAdapter) {
            ((RecyclerViewAdapter) mAdapter).setOnItemClickListener(new RecyclerViewAdapter.MyClickListener() {
                @Override
                public void onItemClick(int position, View v) {
                    Log.i(TAG, " Clicked on Item ");
                    displayAssetList(position);
                }
            });
        }
    }

    @Override
    protected void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);
        setIntent(newIntent);
        String intentProjectId = newIntent.getStringExtra("project_id");
        if (null != intentProjectId) {
            projectId = intentProjectId;
            Log.d(TAG, "onNewIntent - project_id" + projectId);
        }
        String intentRootId = newIntent.getStringExtra("rootFolderId");
        if (null != intentRootId) {
            rootFolderId = intentRootId;
            Log.d(TAG, "onNewIntent - rootFolderId" + rootFolderId);
        }
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
    public void onClick(View view) {
        displayAssetList(view.getId());
    }

    protected void displayAssetList(int id){
        Folder folder = (Folder)folderList.get(id);
        showToast("folder name: " + folder.getName() + " is opened.");
        Intent intent = new Intent(ProjectDetailsActivity.this, ProjectDetailsActivity.class);
        intent.putExtra("project_id", projectId);
        intent.putExtra("rootFolderId", folder.getFolder_id());
        startActivity(intent);
    }


    @Override
    public void onGetRootFolderIdReady(){
        rootFolder = getRootFolderId.getRootFolder();
        if (null != rootFolder) {
            rootFolderId = rootFolder.folder_id;
            if (null != rootFolderId) {
                getOneLevelChild = new GetOneLevelChild(this, ProjectDetailsActivity.this, settings, rootFolderId, projectId);
                getOneLevelChild.execute();
            }
        }else {
            showToast("Sorry cannot get one level child because root id not found.");
            Log.d(TAG, "rootId not found");
        }
    }

    @Override
    public void onOneLevelChildReady(){
        ArrayList<Folder> arrayfolderList = getOneLevelChild.getFolderList();
        if (null != arrayfolderList) {
            Log.d(TAG, "folderList.getFolder_id: " + arrayfolderList.get(0).getFolder_id());
            View v = ProjectDetailsActivity.this
                    .findViewById(android.R.id.content).getRootView();
//            createFolderCardRow(v);
            for (int i = 0; i<arrayfolderList.size(); i++) {
                folderList.add((Object) arrayfolderList.get(i));
            }
            mAdapter = new RecyclerViewAdapter(this,folderList);
            ((RecyclerViewAdapter) mAdapter).setOnItemClickListener(new RecyclerViewAdapter.MyClickListener() {
                @Override
                public void onItemClick(int position, View v) {
                    Log.i(TAG, " Clicked on Item ");
                    displayAssetList(position);
                }
            });
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    public void onAssetReady(){
        ArrayList<Asset> arrayAssetList = getAsset.getAssetList();

        if (null != arrayAssetList) {
            Log.d(TAG, "assetList is gotten");
            Boolean needThumbNailUpdate = false;
            ArrayList<String> assetIdList = new ArrayList<String>();
            for (int i = 0; i<arrayAssetList.size(); i++) {
                Asset temp  = arrayAssetList.get(i);
                Log.d(TAG, "array type: " + temp.getExt());
                if(((temp.getExt().equals("jpg") || (temp.getExt().equals("png") || (temp.getExt().equals("jpeg")) || (temp.getExt().equals("gif")))))) {
                    Log.d(TAG, "call GetAssetDetail()");
                    needThumbNailUpdate = true;
                    assetIdList.add(temp.asset_id);
                } //Todo -- if several images are calling GetAssetDetail() at almost the same time
                assetList.add(temp);
            }if (needThumbNailUpdate) {
                getAssetDetail = new GetAssetDetail(this, ProjectDetailsActivity.this, settings, assetIdList, projectId);
                getAssetDetail.execute();
            }
            mAdapter = new RecyclerViewAdapter(this, assetList);
            ((RecyclerViewAdapter) mAdapter).setOnItemClickListener(new RecyclerViewAdapter.MyClickListener() {
                @Override
                public void onItemClick(int position, View v) {
                    Log.i(TAG, " Clicked on Item ");
                }
            });
            mRecyclerView.setAdapter(mAdapter);

        }
    }
    public void onAssetDetailReady(){
        ArrayList<Asset> assetDetailList = getAssetDetail.getAssetDetail();
        boolean newAssetUsed = false;
        if (null != assetDetailList && assetDetailList.size() > 0) {
            for (int j=0; j<assetDetailList.size(); j++) {
                Asset assetDetail = assetDetailList.get(j);
                if (null != assetDetail) {
                    Log.d(TAG, "assetDetail is gotten");
                    for (int i = 0; i < assetList.size(); i++) {
                        Asset temp = (Asset) assetList.get(i);
                        if (temp.getAsset_id().equalsIgnoreCase(assetDetail.getAsset_id())) {
                            String base64_thumbnail = assetDetail.getBase64_thumbnail();
                            if (null == base64_thumbnail) {
                                Log.d(TAG, "base64_thumbnail is null");
                            }
                            ((Asset) assetList.get(i)).setBase64_thumbnail(base64_thumbnail);
                            newAssetUsed = true;
                            if (null != ((Asset) assetList.get(i)).getBase64_thumbnail()) {
                                Log.d(TAG, "Base64_thumbnail is available");
                            }
                            break;
                        }
                    }
                    if (!newAssetUsed) {
                        Log.d(TAG, "new asset is not used, asset id: " + assetDetail.getAsset_id());
                        Log.d(TAG, "new asset is not used, asset type: " + assetDetail.getExt());
                    }
                    mAdapter = new RecyclerViewAdapter(this, assetList);
                    ((RecyclerViewAdapter) mAdapter).setOnItemClickListener(new RecyclerViewAdapter.MyClickListener() {
                        @Override
                        public void onItemClick(int position, View v) {
                            Log.i(TAG, " Clicked on Item ");
//                    displayAssetList(position);
                        }
                    });
                    mRecyclerView.setAdapter(mAdapter);
                }
            }
        }
    }

    public void createFolderCardRow(View v) {
//        CardView tl = (CardView) findViewById(R.id.cardRowFolderList);

        for (int i = 0; i < folderList.size(); i++) {
//            Folder folder = folderList.get(i);
            TableRow tr = new TableRow(this);

//            if (i % 2 == 0)
//                    tr.setBackgroundColor(Color.WHITE);
//            else {
//                Resources res = getResources();
//                tr.setBackgroundColor(res.getColor(R.color.siqi));
//            }
            tr.setPadding(0, 16, 0, 16);

            ImageView imageView = new ImageView(this);
            Resources res = getResources();
            imageView.setImageDrawable(res.getDrawable(R.mipmap.folder));

            TextView textViewName = new TextView(this);
//            textViewName.setText(folder.getName());
            textViewName.setGravity(Gravity.CENTER_VERTICAL);
            textViewName.setPadding(16, 16, 16, 16);
            textViewName.setId(i);
            textViewName.setOnClickListener(this);


            tr.addView(textViewName, new TableRow.LayoutParams(0,
                    TableRow.LayoutParams.WRAP_CONTENT, (float) 1));

//            tl.addView(tr, new TableLayout.LayoutParams(
//                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        }
    }

//TODO - implement status shown

    public void createAssetTableRow(View v) {
//        TableLayout tl = (TableLayout) findViewById(R.id.tableRowFolderList);

        for (int i = 0; i < assetList.size(); i++) {
//            Asset asset = assetList.get(i);
            TableRow tr = new TableRow(this);

//            if (i % 2 == 0)
//                tr.setBackgroundColor(Color.WHITE);
//            else {
//                Resources res = getResources();
//                tr.setBackgroundColor(res.getColor(R.color.siqi));
//            }

            tr.setPadding(0, 16, 0, 16);

//            ImageView imageView = new ImageView(this);
//            if(((asset.getExt().equals("jpg") || (asset.getExt().equals("png") || (asset.getExt().equals("jpeg")) || (asset.getExt().equals("gif")))))) {
//                if(null != asset.getBase64_thumbnail() && !asset.getBase64_thumbnail().isEmpty()) {
//                    byte[] decodedString = Base64.decode(
//                            asset.getBase64_thumbnail(), Base64.DEFAULT);
//                    Bitmap decodedByte = BitmapFactory.decodeByteArray(
//                            decodedString, 0, decodedString.length);
//                    imageView.setImageBitmap(decodedByte);
//                } else{
//                    Resources res = getResources();
//                    imageView.setImageDrawable(res.getDrawable(R.drawable.content_picture));
//                }
//            } else if((asset.getExt().equals("avi") || (asset.getExt().equals("flv") || (asset.getExt().equals("mp4")) || (asset.getExt().equals("webm"))))){
//                Resources res = getResources();
//                imageView.setImageDrawable(res.getDrawable(R.drawable.ic_action_video));
//            } else {
//                Resources res = getResources();
//                imageView.setImageDrawable(res.getDrawable(R.mipmap.no_image));
//            }

//            TextView textViewName = new TextView(this);
//
//            String status = "";
//            if (null == asset.getStatusid() || asset.getStatusid().equals(null)) {
//                status = "Unavailable";
//            } else {
//                switch (Integer.parseInt(asset.getStatusid())) {
//                    case 100:
//                        status = "Pending";
//                        break;
//                    case 101:
//                        status = "In Progress";
//                        break;
//                    case 102:
//                        status = "For Approval";
//                        break;
//                    case 103:
//                        status = "Approved";
//                        break;
//                    case 104:
//                        status = "Completed";
//                        break;
//                    case 105:
//                        status = "Rejected";
//                        break;
//                    default:
//                        status = "";
//                        break;
//                }
//            }
//
//            textViewName.setText(asset.getName() + " asset");
//            textViewName.setGravity(Gravity.CENTER_VERTICAL);
//            textViewName.setPadding(16, 16, 16, 16);
//            textViewName.setId(i);
//
//            textViewName.setOnClickListener(this);
//            textViewName.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
//
//            TextView textViewStatus = new TextView(this);
//            textViewStatus.setText("Status: " + status);
//            textViewStatus.setGravity(Gravity.RIGHT);
//            textViewStatus.setPadding(16, 16, 16, 16);
//            textViewStatus.setId(i);
            //textViewStatus.setOnClickListener(this); --todo
//            textViewStatus.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            //todo- preview
            //textViewName.setOnClickListener(this);

//            tr.addView(imageView, new TableRow.LayoutParams(0, 60,(float) 1));
//            tr.addView(textViewName, new TableRow.LayoutParams(0,
//                    TableRow.LayoutParams.WRAP_CONTENT, (float) 3));
//            tr.addView(textViewStatus, new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, (float) 2));
//            tl.addView(tr, new TableLayout.LayoutParams(
//                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
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
