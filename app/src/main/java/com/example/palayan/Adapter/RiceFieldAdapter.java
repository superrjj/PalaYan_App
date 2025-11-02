package com.example.palayan.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.palayan.Helper.RiceFieldProfile;
import com.example.palayan.R;

import java.util.List;
import java.util.Locale;

public class RiceFieldAdapter extends RecyclerView.Adapter<RiceFieldAdapter.RiceFieldViewHolder> {

    private List<RiceFieldProfile> riceFields;
    private OnRiceFieldClickListener listener;

    public interface OnRiceFieldClickListener {
        void onRiceFieldClick(RiceFieldProfile riceField);
    }

    public RiceFieldAdapter(List<RiceFieldProfile> riceFields, OnRiceFieldClickListener listener) {
        this.riceFields = riceFields;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RiceFieldViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rice_field, parent, false);
        return new RiceFieldViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RiceFieldViewHolder holder, int position) {
        RiceFieldProfile riceField = riceFields.get(position);
        holder.bind(riceField, listener);
    }

    @Override
    public int getItemCount() {
        return riceFields != null ? riceFields.size() : 0;
    }

    public void updateList(List<RiceFieldProfile> newList) {
        this.riceFields = newList;
        notifyDataSetChanged();
    }

    static class RiceFieldViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivRiceFieldImage;
        private TextView tvName, tvLocation, tvSoilType, tvSize;

        public RiceFieldViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRiceFieldImage = itemView.findViewById(R.id.ivRiceFieldImage);
            tvName = itemView.findViewById(R.id.tvName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvSoilType = itemView.findViewById(R.id.tvSoilType);
            tvSize = itemView.findViewById(R.id.tvSize);
        }

        public void bind(RiceFieldProfile riceField, OnRiceFieldClickListener listener) {
            tvName.setText(riceField.getName());
            
            // Set location (Barangay, City, Province)
            String location = "";
            if (riceField.getBarangay() != null && !riceField.getBarangay().isEmpty()) {
                location = riceField.getBarangay();
            }
            if (riceField.getCity() != null && !riceField.getCity().isEmpty()) {
                if (!location.isEmpty()) location += ", ";
                location += riceField.getCity();
            }
            if (riceField.getProvince() != null && !riceField.getProvince().isEmpty()) {
                if (!location.isEmpty()) location += ", ";
                location += riceField.getProvince();
            }
            tvLocation.setText(location.isEmpty() ? "Walang lokasyon" : location);
            
            tvSoilType.setText(riceField.getSoilType());
            tvSize.setText(String.format(Locale.getDefault(), "%.2f hektarya", riceField.getSizeHectares()));
            
            // Load image using Glide
            if (riceField.getImageUrl() != null && !riceField.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(riceField.getImageUrl())
                        .placeholder(R.color.light_gray)
                        .error(R.color.light_gray)
                        .into(ivRiceFieldImage);
            } else {
                ivRiceFieldImage.setImageResource(android.R.color.transparent);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRiceFieldClick(riceField);
                }
            });
        }
    }
}

