package com.example.amps.fyp_amps_android_yishan;

import android.content.Context;
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
        TextView assetNameTV, revisionNo, updatedUserName, updatedDate, comment, fileSize;
        ImageView image;

        public DataObjectHolder(View itemView) {
            super(itemView);
            assetNameTV = (TextView) itemView.findViewById(R.id.assetNameTV);
            revisionNo = (TextView) itemView.findViewById(R.id.vertionNumberTV);
            updatedUserName = (TextView) itemView.findViewById(R.id.updatedUserNameTV);
            updatedDate = (TextView) itemView.findViewById(R.id.lastUpdateTimeTV);
            comment = (TextView) itemView.findViewById(R.id.commentTV);
            fileSize = (TextView) itemView.findViewById(R.id.filesizeTV);
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

        holder.assetNameTV.setText("");
        holder.revisionNo.setText("revision no not available");
        holder.updatedUserName.setText("name not available");
        holder.updatedDate.setText("date not available");
        holder.comment.setText("No comment yet");
        //--todo implement comment

        if (null != asset.getName()) {
            String fullname = asset.getName();
            if (null != asset.getExt()) {
                fullname = fullname + "." + asset.getExt();
            } else {
                //do nothing
            }
            holder.assetNameTV.setText(fullname);
        }
        if (null != asset.getLatest_revnum()) {
            holder.revisionNo.setText("#" + asset.getLatest_revnum());
        } else {
            Log.d(TAG, "revisionNo is null.");
        } if (null != asset.getUpdated_username()) {
            holder.updatedUserName.setText(asset.getUpdated_username());
        } else {
            Log.d(TAG, "updatedUserName is null.");
        } if (null != asset.getUpdated_datetime()) {
            holder.updatedDate.setText(asset.getUpdated_datetime());
        } else {
            Log.d(TAG, "updatedDate is null.");
        }

        String fileSizeUnit;
        if (null != asset && 0 != asset.getLatest_revsize()) {
            DecimalFormat df = new DecimalFormat("#.###");
            Double latestRevSize = asset.getLatest_revsize();
            if (latestRevSize < 1024) {
                fileSizeUnit = "B";
                holder.fileSize.setText(latestRevSize+ " " + fileSizeUnit);
            } else if (latestRevSize < 1024 * 1024) {
                double newsize = Double.parseDouble(df.format(latestRevSize / (1024.000)));
//                asset.setFile_size(Double.parseDouble(df.format(newsize)));
                fileSizeUnit = "kB";
                holder.fileSize.setText(newsize + " " + fileSizeUnit);
            } else if (asset.getLatest_revsize() >= 1024 * 1024) {
                double newsize = Double.parseDouble(df.format(latestRevSize / (1024.000 * 1024.000)));
//                asset.setFile_size(Double.parseDouble(df.format(newsize)));
                fileSizeUnit = "MB";
                holder.fileSize.setText(newsize + " " + fileSizeUnit);
            }


            //tvFileSize2.setText(String.valueOf(a.getFile_size()));

            if (null != asset.getExt() &&
                    ((asset.getExt().equals("jpg") || (asset.getExt().equals("png") || (asset.getExt().equals("jpeg")) || (asset.getExt().equals("gif")))))) {
                if (null != asset.getBase64_thumbnail() && (!asset.getBase64_thumbnail().isEmpty())) {
//                    Log.d(TAG, "get Base64_thumbnail");
                    byte[] decodedString = Base64.decode(
                            asset.getBase64_thumbnail(), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(
                            decodedString, 0, decodedString.length);
                    Log.d(TAG, "decodedByte: " + decodedByte.getHeight() + " " + decodedByte.getWidth());
                    holder.image.setImageBitmap(decodedByte);
                } else {
//                    Log.d(TAG, "don't get Base64_thumbnail");
                    Resources res = context.getResources();
                    holder.image.setImageDrawable(res.getDrawable(R.drawable.unknown_icon_large));
                }
            } else if (null != asset.getExt() &&
                    (asset.getExt().equals("avi") || (asset.getExt().equals("flv")
                    || (asset.getExt().equals("mp4")) || (asset.getExt().equals("wmv"))
                    || (asset.getExt().equals("mp3"))
                    || (asset.getExt().equals("webm"))))) {
                Resources res = context.getResources();
                holder.image.setImageDrawable(res.getDrawable(R.drawable.video_icon_large));
            } else if (null != asset.getExt() &&
                    (asset.getExt().equals("doc") || (asset.getExt().equals("txt")
                    || (asset.getExt().equals("pptx")) || (asset.getExt().equals("ppt"))
                    || (asset.getExt().equals("docx"))))) {
                Resources res = context.getResources();
                holder.image.setImageDrawable(res.getDrawable(R.drawable.document_icon_large));
            } else {
                Resources res = context.getResources();
                holder.image.setImageDrawable(res.getDrawable(R.drawable.unknown_icon_large));
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