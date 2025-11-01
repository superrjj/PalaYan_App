package com.example.palayan.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.palayan.Helper.TreatmentTimelineModel;
import com.example.palayan.R;

import java.util.List;

public class TreatmentTimelineAdapter extends RecyclerView.Adapter<TreatmentTimelineAdapter.ViewHolder> {

    private final Context context;
    private final List<TreatmentTimelineModel> timelineList;
    private OnPhotoClickListener onPhotoClickListener;

    public TreatmentTimelineAdapter(Context context, List<TreatmentTimelineModel> timelineList) {
        this.context = context;
        this.timelineList = timelineList;
    }

    public interface OnPhotoClickListener {
        void onPhotoClick(String imageUrl, String date, String description);
    }

    public void setOnPhotoClickListener(OnPhotoClickListener listener) {
        this.onPhotoClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_treatment_timeline, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TreatmentTimelineModel item = timelineList.get(position);

        // Set date
        holder.tvDate.setText(item.getDate());

        // Set description
        if (item.getDescription() != null && !item.getDescription().isEmpty()) {
            holder.tvDescription.setText(item.getDescription());
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        // Load photo if available
        if (item.getPhotoUrl() != null && !item.getPhotoUrl().isEmpty()) {
            holder.ivPhoto.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(item.getPhotoUrl())
                    .placeholder(R.color.light_gray)
                    .centerCrop()
                    .into(holder.ivPhoto);

            // Set click listener for photo
            holder.ivPhoto.setOnClickListener(v -> {
                if (onPhotoClickListener != null) {
                    onPhotoClickListener.onPhotoClick(
                        item.getPhotoUrl(),
                        item.getDate(),
                        item.getDescription() != null ? item.getDescription() : "Walang paglalarawan"
                    );
                }
            });
        } else {
            holder.ivPhoto.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return timelineList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        TextView tvDate;
        TextView tvDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }
}

