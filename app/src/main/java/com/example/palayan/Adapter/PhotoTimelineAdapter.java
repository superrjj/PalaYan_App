package com.example.palayan.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.palayan.Helper.PhotoTimelineModel;
import com.bumptech.glide.Glide;
import com.example.palayan.R;

import java.util.List;

public class PhotoTimelineAdapter extends RecyclerView.Adapter<PhotoTimelineAdapter.ViewHolder> {

    private final Context context;
    private final List<PhotoTimelineModel> photoList;
    private OnPhotoClickListener onPhotoClickListener;

    public PhotoTimelineAdapter(Context context, List<PhotoTimelineModel> photoList) {
        this.context = context;
        this.photoList = photoList;
    }

    public interface OnPhotoClickListener {
        void onPhotoClick(String imageUrl, String date);
    }

    public void setOnPhotoClickListener(OnPhotoClickListener listener) {
        this.onPhotoClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_image_rice_disease, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PhotoTimelineModel item = photoList.get(position);
        holder.tvDate.setText(item.getDate());

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.color.light_gray)
                .centerCrop()
                .into(holder.ivDiseaseImage);

        holder.itemView.setOnClickListener(v -> {
            if (onPhotoClickListener != null) {
                onPhotoClickListener.onPhotoClick(item.getImageUrl(), item.getDate());
            }
        });
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivDiseaseImage;
        TextView tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDiseaseImage = itemView.findViewById(R.id.ivDiseaseImage);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
