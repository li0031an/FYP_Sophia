package com.example.amps.fyp_amps_android_yishan;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class ProjectDetailsActivity extends BaseActivity implements Settings, View.OnClickListener
        , GetRootFolderIdListener, GetOneLevelChildListener, GetAssetListener
        , CreateDeleteProjectFolderListener, CanAccessFolderListener, GetSharedResourceListener {

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
    AsyncTaskGetSharedResource asyncTaskGetSharedResource;

    private static String header;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private int noFolderItem;
    private int noAssetItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_project_details);
        setTitle("Project folders");
        settings = getSharedPreferences(SETTINGS, 0);
        Intent intent = getIntent();
        projectId = intent.getStringExtra("project_id");
        String projectName = intent.getStringExtra("project_name");
        String folderName = intent.getStringExtra("folder_name");
        if (null != projectName) {
            header = projectName;
            setTitle(header);
        }
        if (null != folderName) {
            header = folderName;
            setTitle(header);
        }
        if (null != projectId) {
            String intentRootId = intent.getStringExtra("rootFolderId");
            if (null != intentRootId) {
                rootFolderId = intentRootId;
                Log.d(TAG, "intent - rootFolderId" + rootFolderId);
                getAsset = new GetAsset(this, ProjectDetailsActivity.this, settings, rootFolderId, projectId);
                getAsset.execute();
                getOneLevelChild = new GetOneLevelChild(this, ProjectDetailsActivity.this, settings, rootFolderId, projectId);
                getOneLevelChild.execute();
            } else {
                AsyncTaskCanAccessFolder asyncTaskCanAccessFolder = new AsyncTaskCanAccessFolder(this, ProjectDetailsActivity.this, settings, projectId);
                asyncTaskCanAccessFolder.execute();
            }
        } else {
            if (null == savedInstanceState) {
                Log.e(TAG, "savedInstanceState is null");
            } else {
                onRestoreInstanceState(savedInstanceState);
            }
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
//        listviewMenu = getResources().getStringArray(R.array.project_details_longclick_items);
//        ListView listview = (ListView)findViewById(R.id.project_details_layout_listview);
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.project_details_listview_items, listviewMenu);
//        listview.setAdapter(adapter);
//        registerForContextMenu(listview);
        //
        Button add_button = (Button) findViewById(R.id.add_button);
//        add_button.setOnClickListener(this);
        registerForContextMenu(add_button);

    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_folder_create_upload, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //get the context view item selected, e.g. original menu
        //AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        String environment = "";
        //get the action to do, e.g. upload images or videos
        switch (item.getItemId()) {
            case R.id.upload_item_image_or_video:
                environment = Environment.DIRECTORY_PICTURES;
                callUploadActivity(environment);
                break;
//            case R.id.upload_item_other_types:
//                environment = Environment.DIRECTORY_DOWNLOADS;
//                callUploadActivity(environment);
//                break;
            case R.id.create_new_folder:
//                showToast("create_new_folder.");
                askForNewFolderName();
                break;
            default: // do nothing
        }
//        if ("" != environment) {
//            callUploadActivity(environment);
//        } else {
//            //do create new folder
//        }
        return super.onContextItemSelected(item);
    }

    protected void askForNewFolderName() {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(ProjectDetailsActivity.this);
        View promptsView = li.inflate(R.layout.prompt_new_folder_name, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                new ContextThemeWrapper(this, R.style.dialogTheme));

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);
        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Confirm",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                // edit text
                                String name = userInput.getText().toString();
                                startCreateProjectFolderAsyncTask(name);
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    protected void confirmForDeleteFolder(final int position) {
        // get prompts.xml view
        if (position < folderList.size()) {
            LayoutInflater li = LayoutInflater.from(ProjectDetailsActivity.this);
            View promptsView = li.inflate(R.layout.prompt_confirmation_without_input, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    new ContextThemeWrapper(this, R.style.dialogTheme));

            // set prompts.xml to alertdialog builder
            alertDialogBuilder.setView(promptsView);

            // set dialog message
            alertDialogBuilder
                    .setMessage("Confirm to delete the folder " + ((Folder) folderList.get(position)).getName() + " ?")
                    .setCancelable(false)
                    .setPositiveButton("Confirm",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
//                                    showToast("should start delete folder " + ((Folder)folderList.get(position)).getName());
                                    startDeleteProjectFolderAsyncTask((Folder) folderList.get(position));
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
        } else {
            Log.e(TAG, "the position is out of range: position: " + position);
        }
    }

    public void startCreateProjectFolderAsyncTask(String name) {
        AsyncTaskCreateProjectFolder asyncTaskCreateProjectFolder =
                new AsyncTaskCreateProjectFolder(this, name, ProjectDetailsActivity.this,
                        rootFolderId, settings, projectId);
        asyncTaskCreateProjectFolder.execute();
    }

    public void startDeleteProjectFolderAsyncTask(Folder folder) {
        AsyncTaskDeleteProjectFolder asyncTaskDeleteProjectFolder =
                new AsyncTaskDeleteProjectFolder(this, folder.getFolder_id(), folder.getName(), ProjectDetailsActivity.this,
                        settings, projectId);
        asyncTaskDeleteProjectFolder.execute();
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart()");
        if (null != mAdapter) {
            ((RecyclerViewAdapter) mAdapter).clearData();
        }
        SharedPreferences details = getSharedPreferences(TAG, Context.MODE_PRIVATE);
        projectId = details.getString("projectId", null);
        Log.d(TAG, "projectId: " + projectId);
        int layerNo = details.getInt("NO", 0);
        Log.d(TAG, "layerNo: onRestart: " + layerNo);
        String intentRootId = null;
        if (layerNo > 1) {
            intentRootId = details.getString(String.valueOf(layerNo), null);
        } else if (layerNo == 1) {
            intentRootId = details.getString(String.valueOf(layerNo), null);
        }
        if (null != intentRootId) {
            details.edit().putInt("NO", layerNo - 1).commit();
            Log.d(TAG, "rootFolderId: " + intentRootId);
            rootFolderId = intentRootId;
            getAsset = new GetAsset(this, ProjectDetailsActivity.this, settings, rootFolderId, projectId);
            getAsset.execute();
            getOneLevelChild = new GetOneLevelChild(this, ProjectDetailsActivity.this, settings, rootFolderId, projectId);
            getOneLevelChild.execute();
        } else {
            AsyncTaskCanAccessFolder asyncTaskCanAccessFolder = new AsyncTaskCanAccessFolder(this, ProjectDetailsActivity.this, settings, projectId);
            asyncTaskCanAccessFolder.execute();
        }
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        if (null != mAdapter) {
            ((RecyclerViewAdapter) mAdapter).setOnItemClickListener(new RecyclerViewAdapter.MyClickListener() {
                @Override
                public void onItemClick(int position, View v) {
                    onItemClickCommon(position, v);
                }
            });
        }
    }

    protected void onItemClickCommon(int position, View v) {
        if (v.getId() == R.id.clickIcon) {
            confirmForDeleteFolder(position);
        } else {
            if (position < noFolderItem) {
                displayAssetList(position);
            } else {
                int newPosition = position - noFolderItem;
                Intent intent = new Intent(ProjectDetailsActivity.this, AssetDetailActivity.class);
                intent.putExtra("project_id", projectId);
                intent.putExtra("asset_id", ((Asset) assetList.get(newPosition)).getAsset_id());
                intent.putExtra("asset_name", ((Asset) assetList.get(newPosition)).getName());
                intent.putExtra("folderId", rootFolderId);
                startActivity(intent);
            }
        }
    }


    @Override
    protected void onPause() {
        SharedPreferences Details = getSharedPreferences(TAG, Context.MODE_PRIVATE);
        boolean isBackPressed = Details.getBoolean("IsBackPressed", false);
        Details.edit().putBoolean("IsBackPressed", false).commit();
        Log.d(TAG, "onPause IsBackPressed: " + String.valueOf(isBackPressed));
        if ((!isBackPressed) && (null != rootFolderId)) {
            int layerNo = Details.getInt("NO", 0);
            String stringNo = String.valueOf(layerNo + 1);
            Details.edit().putInt("NO", layerNo + 1)
                    .putString(stringNo, rootFolderId).commit();
            Log.d(TAG, "NO: onPause: " + String.valueOf(layerNo + 1));
            Log.d(TAG, "Layno, rootFolderId: " + String.valueOf(layerNo + 1) + " " + rootFolderId);
        }
        Details.edit().putString("projectId", projectId).commit();
        super.onPause();
        Log.d(TAG, "onPause()");
        if (null != mAdapter) {
            ((RecyclerViewAdapter) mAdapter).clearData();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
        if (null != mAdapter) {
            ((RecyclerViewAdapter) mAdapter).clearData();
        }
        SharedPreferences Details = getSharedPreferences(TAG, Context.MODE_PRIVATE);
        if (null != projectId) {
        }
//        Bundle savedInstanceState = new Bundle();
//        onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
//        Bundle savedInstanceState = new Bundle();
//        onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        SharedPreferences Details = getSharedPreferences(TAG, Context.MODE_PRIVATE);
        int layerNo = Details.getInt("NO", 0);
//        Details.edit().putInt("NO", layerNo - 1).commit();
        Details.edit().putBoolean("IsBackPressed", true).commit();
        Log.d(TAG, "layerno: onBackPressed: " + String.valueOf(layerNo));
        ProjectDetailsActivity.super.onBackPressed();
        finish();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (null != projectId) {
            savedInstanceState.putString("projectId", projectId);
        }
        if (null != rootFolderId) {
            savedInstanceState.putString("rootFolderId", rootFolderId);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        projectId = savedInstanceState.getString("projectId");
        rootFolderId = savedInstanceState.getString("rootFolderId");
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
        String intentProjectName = newIntent.getStringExtra("project_name");
        String intentFolderName = newIntent.getStringExtra("folder_name");
        if (null != intentRootId) {
            rootFolderId = intentRootId;
            Log.d(TAG, "new intent - rootFolderId: " + rootFolderId);
        }
        if (null != intentFolderName) {
            header = intentFolderName;
            Log.d(TAG, "new intent - header: intentFolderName: " + intentFolderName);
        }
        if (null != intentProjectName) {
            header = intentProjectName;
            Log.d(TAG, "new intent - header: intentProjectName: " + intentProjectName);
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


    private void callUploadActivity(String environmentVariable) {
        Intent uploadFile = new Intent(ProjectDetailsActivity.this, AssetUploadActivity.class);
//                    uploadImage.putExtra("asset_id", asset_id);
        uploadFile.putExtra("project_id", projectId);
        uploadFile.putExtra("folder_id", rootFolderId);
        uploadFile.putExtra("isNewRevision", false);
        uploadFile.putExtra("environment", environmentVariable);
//        Log.d("AssetUploadActivity", "pass to folder_id: " + rootFolderId);
//        Log.d(TAG, "environmentVariable pass to upload: " + environmentVariable);
//                    String assetFullName = asset.getName() + "." + asset.getExt();
//                    String assetFullName = "";
//                    uploadImage.putExtra("assetFullName", assetFullName);
//                    Log.d("AssetUploadActivity", "pass to assetFullName: "+assetFullName);
//                    uploadImage.putExtra("latest_revid", asset.getLatest_revid());
//                    Log.d("AssetUploadActivity", "pass to latest_revid: "+ asset.getLatest_revid());
        startActivity(uploadFile);
    }

    //
    @Override
    public void onClick(View view) {
        displayAssetList(view.getId());
    }

    protected void displayAssetList(int id) {
        Folder folder = (Folder) folderList.get(id);
        showToast("folder name: " + folder.getName() + " is opened.");
        Intent intent = new Intent(ProjectDetailsActivity.this, ProjectDetailsActivity.class);
        intent.putExtra("project_id", projectId);
        intent.putExtra("rootFolderId", folder.getFolder_id());
        intent.putExtra("folder_name", folder.getName());
        startActivity(intent);
    }

    protected void refreshRecyclerView() {
        mAdapter = new RecyclerViewAdapter(this, folderList, assetList);
        noAssetItem = 0;
        noFolderItem = 0;
        if (null != folderList) {
            noFolderItem = folderList.size();
        }
        if (null != assetList) {
            noAssetItem = assetList.size();
        }
        ((RecyclerViewAdapter) mAdapter).setOnItemClickListener(new RecyclerViewAdapter.MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                onItemClickCommon(position, v);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    protected void refreshFolderList(ArrayList<Folder> arrayfolderList) {
        if (null != arrayfolderList && 0 != arrayfolderList.size()) {
//            Log.d(TAG, "folderList.getFolder_id: " + arrayfolderList.get(0).getFolder_id());
            folderList.clear();
//            Log.d(TAG, "start to add folderlist");
            for (int i = 0; i < arrayfolderList.size(); i++) {
//                Log.d(TAG, "i, arrayfolder name " + i + " " + arrayfolderList.get(i).getName());
                folderList.add(arrayfolderList.get(i));
            }
        } else {
            folderList.clear();
        }
        refreshRecyclerView();
    }

    protected void refreshBasicAssetList(ArrayList<Asset> arrayAssetList) {
        if (null != arrayAssetList) {
//            Log.d(TAG, "assetList is gotten");
            Boolean needThumbNailUpdate = false;
            Boolean hasBasicInfo = false;
            assetList.clear();
            ArrayList<String> assetIdList = new ArrayList<String>();
            for (int i = 0; i < arrayAssetList.size(); i++) {
                Asset temp = arrayAssetList.get(i);
                assetIdList.add(temp.asset_id);
                if (null != temp.getExt()) {
                    hasBasicInfo = true;
                    if (temp.getExt().equals("jpg") || (temp.getExt().equals("png") || (temp.getExt().equals("jpeg")) || (temp.getExt().equals("gif")))) {
//                    Log.d(TAG, "call GetAssetDetail()");
                        needThumbNailUpdate = true;
                    }
                } else {
                    needThumbNailUpdate = true;
                }
                assetList.add(temp);
            }
            //Todo -- if several images are calling GetAssetDetail() at almost the same time
            if (needThumbNailUpdate) {
                String selectAttributes;
                if (hasBasicInfo) {
                    selectAttributes = "[asset_id],[base64_thumbnail],[ext]";
                } else {
                    selectAttributes = "[asset_id], [name], [ext], [file_size]" +
                            ", [latest_revid], [latest_revnum], [updated_userid], [updated_username]" +
                            ", [updated_datetime], [assigned_userid]" +
                            ", [base64_small_file], [latest_revsize]";
                }
                getAssetDetail = new GetAssetDetail(this, ProjectDetailsActivity.this, settings, assetIdList, projectId, selectAttributes);
                getAssetDetail.execute();
            }
            refreshRecyclerView();
        }
    }


    @Override
    public void onGetRootFolderIdReady() {
        rootFolder = getRootFolderId.getRootFolder();
        if (null != rootFolder) {
            rootFolderId = rootFolder.folder_id;
            if (null != rootFolderId) {
                getOneLevelChild = new GetOneLevelChild(this, ProjectDetailsActivity.this, settings, rootFolderId, projectId);
                getOneLevelChild.execute();
                getAsset = new GetAsset(this, ProjectDetailsActivity.this, settings, rootFolderId, projectId);
                getAsset.execute();
            }
        } else {
//            showToast("Sorry cannot get one level child because root id not found.");
//            Log.d(TAG, "rootId not found");
        }
    }

    @Override
    public void onOneLevelChildReady() {
        boolean valid = getOneLevelChild.valid;

        if (valid) {
            ArrayList<Folder> arrayfolderList = getOneLevelChild.getFolderList();
            refreshFolderList(arrayfolderList);
        }
    }

    @Override
    public void onAssetReady() {
        ArrayList<Asset> arrayAssetList = getAsset.getAssetList();
        refreshBasicAssetList(arrayAssetList);
    }

    public void onAssetDetailReady() {
        ArrayList<Asset> assetDetailList = getAssetDetail.getAssetDetail();
        boolean newAssetUsed = false;
        if (null != assetDetailList && assetDetailList.size() > 0) {
            for (int j = 0; j < assetDetailList.size(); j++) {
                Asset assetDetail = assetDetailList.get(j);
                if (null != assetDetail) {
//                    Log.d(TAG, "assetDetail is gotten");
                    for (int i = 0; i < assetList.size(); i++) {
                        Asset temp = (Asset) assetList.get(i);
                        if (temp.getAsset_id().equalsIgnoreCase(assetDetail.getAsset_id())) {
                            if (null == assetDetail.getName()) {
                                String base64_thumbnail = assetDetail.getBase64_thumbnail();
//                                if (null == base64_thumbnail) {
//                                Log.d(TAG, "base64_thumbnail is null");
//                                }
                                ((Asset) assetList.get(i)).setBase64_thumbnail(base64_thumbnail);
                            } else {
                                assetList.remove(i);
                                assetList.add(assetDetail);
                            }
                            newAssetUsed = true;
                            if (null != ((Asset) assetList.get(i)).getBase64_thumbnail()) {
//                                Log.d(TAG, "Base64_thumbnail is available");
                            }
                            break;
                        }
                    }
                    if (!newAssetUsed) {
                        Log.d(TAG, "new asset is not used, asset id: " + assetDetail.getAsset_id());
                        Log.d(TAG, "new asset is not used, asset type: " + assetDetail.getExt());
                    }
                }
            }
            refreshRecyclerView();
        }
    }

    @Override
    public void onCreateProjectFolderReady(String newFolderId) {
//        Log.d(TAG, "createDeleteProjectFolderListener: newFolderId, " + newFolderId);
        if (null != newFolderId) {
            if (null != rootFolderId) {
                getAsset = new GetAsset(this, ProjectDetailsActivity.this, settings, rootFolderId, projectId);
                getAsset.execute();
                getOneLevelChild = new GetOneLevelChild(this, ProjectDetailsActivity.this, settings, rootFolderId, projectId);
                getOneLevelChild.execute();
            } else {
                AsyncTaskCanAccessFolder asyncTaskCanAccessFolder = new AsyncTaskCanAccessFolder(this, ProjectDetailsActivity.this, settings, projectId);
                asyncTaskCanAccessFolder.execute();
            }
        } else {
            Log.e(TAG, "newFolderId is null in onCreateProjectFolderReady");
        }
    }

    @Override
    public void onDeleteProjectFolderReady(String folderId) {
//        Log.d(TAG, "createDeleteProjectFolderListener: newFolderId, " + newFolderId);
        if (null != folderId) {
            if (null != rootFolderId) {
                getAsset = new GetAsset(this, ProjectDetailsActivity.this, settings, rootFolderId, projectId);
                getAsset.execute();
                getOneLevelChild = new GetOneLevelChild(this, ProjectDetailsActivity.this, settings, rootFolderId, projectId);
                getOneLevelChild.execute();
            } else {
                AsyncTaskCanAccessFolder asyncTaskCanAccessFolder = new AsyncTaskCanAccessFolder(this, ProjectDetailsActivity.this, settings, projectId);
                asyncTaskCanAccessFolder.execute();
            }
        } else {
            Log.e(TAG, "folderId is null in onDeleteProjectFolderReady");
        }
    }

    @Override
    public void onCanAccessFolderReady(boolean isProjectAdmin) {
        if (isProjectAdmin) {
            getRootFolderId = new GetRootFolderId(this, ProjectDetailsActivity.this, settings, projectId);
            getRootFolderId.execute();
        } else {
            asyncTaskGetSharedResource = new AsyncTaskGetSharedResource(this, ProjectDetailsActivity.this, settings, projectId);
            asyncTaskGetSharedResource.execute();
        }
    }

    @Override
    public void onGetSharedResourceReady() {
        ArrayList<Folder> tempFolderList = asyncTaskGetSharedResource.getFolderList();
        refreshFolderList(tempFolderList);
        ArrayList<Asset> tempAssetList = asyncTaskGetSharedResource.getAssetList();
        refreshBasicAssetList(tempAssetList);
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
            imageView.setImageDrawable(res.getDrawable(R.mipmap.folder_icon));

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


    public void showToast(String info) {
        Toast toast = Toast.makeText(
                ProjectDetailsActivity.this,
                info,
                Toast.LENGTH_SHORT);
        toast.show();
    }

}
