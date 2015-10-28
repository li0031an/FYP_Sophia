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

import java.text.DecimalFormat;
import java.util.ArrayList;

public class PreviewRecyclerViewAdapter extends RecyclerView
        .Adapter<PreviewRecyclerViewAdapter
        .DataObjectHolder> {
    private static String TAG = "PreviewRecyclerViewAdapter";
    private ArrayList<Object> mDataset;
    private static MyClickListener myClickListener;
    private Context context;

    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        TextView revisionNo, uploadedUser, uploadedDate, comment, fileSize;
        ImageView image;

        public DataObjectHolder(View itemView) {
            super(itemView);
            revisionNo = (TextView) itemView.findViewById(R.id.cardText1);
            uploadedUser = (TextView) itemView.findViewById(R.id.cardText2);
            uploadedDate = (TextView) itemView.findViewById(R.id.cardText3);
            comment = (TextView) itemView.findViewById(R.id.cardText4);
            fileSize = (TextView) itemView.findViewById(R.id.cardText5);
            image = (ImageView) itemView.findViewById(R.id.cardImage);
            Log.i(TAG, "Adding Listener");
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            myClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    public PreviewRecyclerViewAdapter(Context context, ArrayList<Object> myDataset) {
        this.context = context;
        mDataset = myDataset;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_cardview_layout_for_preview, parent, false);

        DataObjectHolder dataObjectHolder = new DataObjectHolder(view);
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        Asset asset = (Asset) mDataset.get(position);
        holder.revisionNo.setText("#" + asset.getRevNum());
        holder.uploadedUser.setText(asset.getUpdated_userid());
        holder.uploadedDate.setText(asset.getUpdated_datetime());
        holder.comment.setText("Comment ...");

        String fileSizeUnit;
        if (null != asset) {
            DecimalFormat df = new DecimalFormat("#.###");
            if (asset.getFile_size() < 1024) {
                fileSizeUnit = "B";
                holder.fileSize.setText(asset.getFile_size() + fileSizeUnit);
            } else if (asset.getFile_size() < 1024 * 1024) {
                asset.setFile_size(Double.parseDouble(df.format(asset.getFile_size() / (1024.000))));
                fileSizeUnit = "kB";
                holder.fileSize.setText(asset.getFile_size() + fileSizeUnit);
            } else if (asset.getFile_size() >= 1024 * 1024) {
                asset.setFile_size(Double.parseDouble(df.format(asset.getFile_size() / (1024.000 * 1024.000))));
                fileSizeUnit = "MB";
                holder.fileSize.setText(asset.getFile_size() + fileSizeUnit);
            }


            //tvFileSize2.setText(String.valueOf(a.getFile_size()));

            if (((asset.getExt().equals("jpg") || (asset.getExt().equals("png") || (asset.getExt().equals("jpeg")) || (asset.getExt().equals("gif")))))) {
                if (null != asset.getBase64_thumbnail() && (!asset.getBase64_thumbnail().isEmpty())) {
                    Log.d(TAG, "get Base64_thumbnail");
                    byte[] decodedString = Base64.decode(
                            asset.getBase64_thumbnail(), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(
                            decodedString, 0, decodedString.length);
                    holder.image.setImageBitmap(decodedByte);
                } else {
                    Log.d(TAG, "don't get Base64_thumbnail");
                    Resources res = context.getResources();
                    holder.image.setImageDrawable(res.getDrawable(R.drawable.content_picture));
                }
            } else if ((asset.getExt().equals("avi") || (asset.getExt().equals("flv") || (asset.getExt().equals("mp4")) || (asset.getExt().equals("webm"))))) {
                Resources res = context.getResources();
                holder.image.setImageDrawable(res.getDrawable(R.drawable.ic_action_video));
            } else {
                Resources res = context.getResources();
                holder.image.setImageDrawable(res.getDrawable(R.mipmap.no_image));
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

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public interface MyClickListener {
        public void onItemClick(int position, View v);
    }
}