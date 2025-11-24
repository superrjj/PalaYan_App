package com.example.palayan.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.palayan.Helper.RicePlanting;
import com.example.palayan.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlantingHistoryAdapter extends RecyclerView.Adapter<PlantingHistoryAdapter.HistoryViewHolder> {

    private List<RicePlanting> historyPlantings;

    public PlantingHistoryAdapter(List<RicePlanting> historyPlantings) {
        this.historyPlantings = historyPlantings;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_planting_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        RicePlanting planting = historyPlantings.get(position);
        holder.bind(planting);
    }

    @Override
    public int getItemCount() {
        return historyPlantings != null ? historyPlantings.size() : 0;
    }

    public void updateList(List<RicePlanting> newList) {
        this.historyPlantings = newList;
        notifyDataSetChanged();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvVarietyName, tvPlantingDate, tvPlantingDetails, tvStatus, tvCompletedDate;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVarietyName = itemView.findViewById(R.id.tvVarietyName);
            tvPlantingDate = itemView.findViewById(R.id.tvPlantingDate);
            tvPlantingDetails = itemView.findViewById(R.id.tvPlantingDetails);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCompletedDate = itemView.findViewById(R.id.tvCompletedDate);
        }

        public void bind(RicePlanting planting) {
            tvVarietyName.setText(planting.getRiceVarietyName() != null ? planting.getRiceVarietyName() : "Unknown Variety");
            
            String plantingDate = planting.getPlantingDate();
            if (plantingDate != null && !plantingDate.isEmpty()) {
                tvPlantingDate.setText("Pagpupunla: " + plantingDate);
            } else {
                tvPlantingDate.setText("Pagpupunla: Hindi nakalista");
            }

            String method = planting.getPlantingMethod() != null && !planting.getPlantingMethod().isEmpty()
                    ? planting.getPlantingMethod()
                    : "Hindi tukoy";
            String seedWeight = planting.getSeedWeight() != null && !planting.getSeedWeight().isEmpty()
                    ? planting.getSeedWeight() + " kg"
                    : "Walang datos";
            
            tvPlantingDetails.setText("Paraan: " + method + " | Binhi: " + seedWeight);

            // Set status based on completedAt or deletedAt
            Date completedAt = planting.getCompletedAt();
            Date deletedAt = planting.getDeletedAt();
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            
            if (completedAt != null) {
                tvStatus.setText("Nakumpleto ang anihan");
                tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    itemView.getContext().getResources().getColor(R.color.green, null)));
                tvStatus.setVisibility(View.VISIBLE);
                tvCompletedDate.setText("Natapos: " + dateFormat.format(completedAt));
                tvCompletedDate.setVisibility(View.VISIBLE);
            } else if (deletedAt != null) {
                tvStatus.setText("Inalis ang pananim");
                tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    itemView.getContext().getResources().getColor(R.color.dark_red, null)));
                tvStatus.setVisibility(View.VISIBLE);
                tvCompletedDate.setText("Inalis: " + dateFormat.format(deletedAt));
                tvCompletedDate.setVisibility(View.VISIBLE);
            } else {
                tvStatus.setVisibility(View.GONE);
                tvCompletedDate.setVisibility(View.GONE);
            }
        }
    }
}

