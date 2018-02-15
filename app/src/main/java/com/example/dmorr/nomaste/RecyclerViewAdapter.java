package com.example.dmorr.nomaste;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    Context context;
    ArrayList<DashboardData> MainDashboardImageList;

    public RecyclerViewAdapter(Context context, ArrayList<DashboardData> tempList){
        this.MainDashboardImageList = tempList;
        this.context = context;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_images, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        // Getting the image (select_image data) from the list of all images
        DashboardData data = MainDashboardImageList.get(position);

        // Setting the image name
        holder.mTextView.setText(data.getImageName());

        // Load image from Glide library into our ViewHolder
        Glide.with(context).load(data.getImageURL()).into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return MainDashboardImageList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView mImageView;
        public TextView mTextView;

        public ViewHolder(View view){
            super(view);

            mImageView = (ImageView) view.findViewById(R.id.imageView);
            mTextView = (TextView) view.findViewById(R.id.ImageNameTextView);
        }
    }
}
