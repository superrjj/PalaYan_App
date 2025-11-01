package com.example.palayan.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        private TextView tvName, tvRiceVariety, tvSoilType, tvSize, tvPlantingDate, tvHistoryCount;

        public RiceFieldViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvRiceVariety = itemView.findViewById(R.id.tvRiceVariety);
            tvSoilType = itemView.findViewById(R.id.tvSoilType);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvPlantingDate = itemView.findViewById(R.id.tvPlantingDate);
            tvHistoryCount = itemView.findViewById(R.id.tvHistoryCount);
        }

        public void bind(RiceFieldProfile riceField, OnRiceFieldClickListener listener) {
            tvName.setText(riceField.getName());
            tvRiceVariety.setText(riceField.getRiceVariety());
            tvSoilType.setText(riceField.getSoilType());
            tvSize.setText(String.format(Locale.getDefault(), "%.2f hektarya", riceField.getSizeHectares()));
            tvPlantingDate.setText("Petsa: " + riceField.getPlantingDate());
            
            int historyCount = riceField.getHistory() != null ? riceField.getHistory().size() : 0;
            tvHistoryCount.setText(String.format(Locale.getDefault(), "%d naitalang kasaysayan", historyCount));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRiceFieldClick(riceField);
                }
            });
        }
    }
}

