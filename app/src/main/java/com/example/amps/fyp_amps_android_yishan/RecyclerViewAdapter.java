package com.example.amps.fyp_amps_android_yishan;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView
        .Adapter<RecyclerViewAdapter
        .DataObjectHolder> {
    private static String TAG = "RecyclerViewAdapter";
    private ArrayList<Object> mDataset;
    private static MyClickListener myClickListener;
    private Context context;
    private int folderItemNo;
    private int assetItemNo;

    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        TextView label;
        ImageView image, clickIcon;

        public DataObjectHolder(View itemView) {
            super(itemView);
            label = (TextView) itemView.findViewById(R.id.cardText);
            image = (ImageView) itemView.findViewById(R.id.cardImage);
            clickIcon = (ImageView) itemView.findViewById(R.id.clickIcon);
//            Log.i(TAG, "Adding Listener");
            itemView.setOnClickListener(this);
            clickIcon.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            myClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    public RecyclerViewAdapter(Context context, ArrayList<Object> folderDataset, ArrayList<Object> assetDataset) {
        this.context = context;
        folderItemNo = 0;
        assetItemNo = 0;
        mDataset = new ArrayList<Object>();
        mDataset.clear();
//        this.clearData();
        if (null != folderDataset && 0 != folderDataset.size()) {
            for (int index = 0; index < folderDataset.size(); index++) {
                mDataset.add(folderDataset.get(index));
            }
            folderItemNo = folderDataset.size();
            if (null != assetDataset && 0 != assetDataset.size()) {
                assetItemNo = assetDataset.size();
                for (int index = 0; index < assetDataset.size(); index++) {
                    mDataset.add(assetDataset.get(index));
                }
            } else {
                Log.d(TAG, "assetDataset is empty");
            }
        } else {
            Log.d(TAG, "folderDataset is empty");
            if (null != assetDataset && 0 != assetDataset.size()) {
                assetItemNo = assetDataset.size();
                mDataset = assetDataset;
            } else {
                Log.e(TAG, "both folderDataset and assetDataset are empty");
            }
        }

        if (mDataset == null || mDataset.size() == 0) {
            showToast("the folder is empty.");
        }
//        for (int i = 0; i < mDataset.size(); i++) {
//            Log.d(TAG, "mDataset i " + i + " " + mDataset.get(i).getClass().getName());
//        }
//        Log.d(TAG, "folderItemNo, assetItemNo" + folderItemNo + " " + assetItemNo);
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_cardview_folder_layout, parent, false);

        DataObjectHolder dataObjectHolder = new DataObjectHolder(view);
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        if (mDataset.get(position) instanceof Folder) {
            holder.label.setText(((Folder) mDataset.get(position)).getName());
            Resources res = context.getResources();
            holder.image.setImageDrawable(res.getDrawable(R.mipmap.folder_icon));
        } else if (mDataset.get(position) instanceof Project) {
            holder.label.setText(((Project) mDataset.get(position)).getName());
            Resources res = context.getResources();
            holder.image.setImageDrawable(res.getDrawable(R.mipmap.project_icon));
        } else if (mDataset.get(position) instanceof Asset) {
            Asset asset = (Asset) mDataset.get(position);
            holder.label.setText(asset.getName());

            if (null != asset.getExt() && ((asset.getExt().equals("jpg") || (asset.getExt().equals("png") || (asset.getExt().equals("jpeg")) || (asset.getExt().equals("gif")))))) {
                if (null != asset.getBase64_thumbnail() && (!asset.getBase64_thumbnail().isEmpty())) {
//                    Log.d(TAG, "get Base64_thumbnail");
                    byte[] decodedString = Base64.decode(
                            asset.getBase64_thumbnail(), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(
                            decodedString, 0, decodedString.length);
                    holder.image.setImageBitmap(decodedByte);
                } else {
//                    Log.d(TAG, "don't get Base64_thumbnail");
                    Resources res = context.getResources();
                    holder.image.setImageDrawable(res.getDrawable(R.mipmap.unknown_icon));
                }
            } else if (null != asset.getExt() &&
                    ((asset.getExt().equals("avi") || (asset.getExt().equals("flv")
                    || (asset.getExt().equals("mp4")) || (asset.getExt().equals("webm"))
                    || (asset.getExt().equals("mp3"))
                    || (asset.getExt().equals("wmv")))))) {
                Resources res = context.getResources();
                holder.image.setImageDrawable(res.getDrawable(R.mipmap.video_icon));
            } else if (null != asset.getExt() &&
                    ((asset.getExt().equals("doc") || (asset.getExt().equals("txt")
                    || (asset.getExt().equals("pptx")) || (asset.getExt().equals("ppt"))
                    || (asset.getExt().equals("docx")))))) {
                Resources res = context.getResources();
                holder.image.setImageDrawable(res.getDrawable(R.mipmap.doc_icon));
            } else {
                Resources res = context.getResources();
                holder.image.setImageDrawable(res.getDrawable(R.mipmap.unknown_icon));
            }
        }
    }

    public void addItem(Folder dataObj, int index) {
        mDataset.add(index, dataObj);
        notifyItemInserted(index);
    }

    public void deleteItem(int index) {
        mDataset.remove(index);
        notifyItemRemoved(index);
    }

    public void clearData() {
        if (null != mDataset && 0 != mDataset.size()) {
            for (int i = mDataset.size() - 1; i >= 0; i--) {
                deleteItem(i);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public interface MyClickListener {
        void onItemClick(int position, View v);
    }

    public void showToast(String info) {
        Toast toast = Toast.makeText(
                context,
                info,
                Toast.LENGTH_SHORT);
        toast.show();
    }
}