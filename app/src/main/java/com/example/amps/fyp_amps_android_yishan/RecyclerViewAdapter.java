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

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView
        .Adapter<RecyclerViewAdapter
        .DataObjectHolder> {
    private static String TAG = "RecyclerViewAdapter";
    private ArrayList<Object> mDataset;
    private static MyClickListener myClickListener;
    private Context context;

    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        TextView label;
        ImageView image;

        public DataObjectHolder(View itemView) {
            super(itemView);
            label = (TextView) itemView.findViewById(R.id.cardText);
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

    public RecyclerViewAdapter(Context context, ArrayList<Object> myDataset) {
        this.context = context;
        mDataset = myDataset;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_cardview_layout, parent, false);

        DataObjectHolder dataObjectHolder = new DataObjectHolder(view);
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        if (mDataset.get(position) instanceof Folder) {
            holder.label.setText(((Folder)mDataset.get(position)).getName());
            Resources res = context.getResources();
            holder.image.setImageDrawable(res.getDrawable(R.mipmap.folder));
        } else if (mDataset.get(position) instanceof Project) {
            holder.label.setText(((Project) mDataset.get(position)).getName());
            Resources res = context.getResources();
            holder.image.setImageDrawable(res.getDrawable(R.mipmap.project_icon));
        } else if (mDataset.get(position) instanceof Asset) {
            Asset asset = (Asset) mDataset.get(position);
            holder.label.setText(asset.getName());

            if(((asset.getExt().equals("jpg") || (asset.getExt().equals("png") || (asset.getExt().equals("jpeg")) || (asset.getExt().equals("gif")))))) {
                if(null != asset.getBase64_thumbnail() && (!asset.getBase64_thumbnail().isEmpty())) {
                    Log.d(TAG, "get Base64_thumbnail");
                    byte[] decodedString = Base64.decode(
                            asset.getBase64_thumbnail(), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(
                            decodedString, 0, decodedString.length);
                    holder.image.setImageBitmap(decodedByte);
                } else{
                    Log.d(TAG, "don't get Base64_thumbnail");
                    Resources res = context.getResources();
                    holder.image.setImageDrawable(res.getDrawable(R.drawable.content_picture));
                }
            } else if((asset.getExt().equals("avi") || (asset.getExt().equals("flv") || (asset.getExt().equals("mp4")) || (asset.getExt().equals("webm"))))){
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