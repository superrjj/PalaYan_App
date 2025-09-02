package com.example.palayan.Adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.palayan.Helper.ImageUploadItem;
import com.example.palayan.R;

import java.util.List;

public class ImageUploadAdapter extends RecyclerView.Adapter<ImageUploadAdapter.ViewHolder> {

    private List<ImageUploadItem> imageList;
    private OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClick(Uri uri);
    }

    public ImageUploadAdapter(List<ImageUploadItem> imageList, OnImageClickListener listener) {
        this.imageList = imageList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_image_rice_disease, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageUploadItem item = imageList.get(position);

        if (item.isPlaceholder()) {
            holder.ivTapToUpload.setVisibility(View.VISIBLE);
            holder.ivRemove.setVisibility(View.GONE);

            holder.ivTapToUpload.setOnClickListener(v -> listener.onImageClick(null));

        } else {
            holder.ivTapToUpload.setVisibility(View.GONE);
            holder.ivRemove.setVisibility(View.VISIBLE);

            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUrl())
                    .into(holder.ivUploadImage);

            // Remove image kapag X clicked
            holder.ivRemove.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    imageList.remove(pos);

                    // add ulit placeholder kung wala na
                    if (!hasPlaceholder()) {
                        imageList.add(new ImageUploadItem(null, true));
                    }

                    notifyItemRemoved(pos);
                    notifyItemRangeChanged(pos, imageList.size());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    private boolean hasPlaceholder() {
        if (imageList.isEmpty()) return false;
        return imageList.get(imageList.size() - 1).isPlaceholder();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUploadImage, ivTapToUpload, ivRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUploadImage = itemView.findViewById(R.id.ivUploadImage);
            ivTapToUpload = itemView.findViewById(R.id.ivTapToUpload);
            ivRemove = itemView.findViewById(R.id.ivRemove);
        }
    }
}
