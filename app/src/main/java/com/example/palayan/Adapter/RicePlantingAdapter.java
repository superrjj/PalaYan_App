package com.example.palayan.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.palayan.Helper.RicePlanting;
import com.example.palayan.R;

import java.util.List;

public class RicePlantingAdapter extends RecyclerView.Adapter<RicePlantingAdapter.PlantingViewHolder> {

    private List<RicePlanting> plantings;
    private OnPlantingClickListener listener;

    public interface OnPlantingClickListener {
        void onPlantingClick(RicePlanting planting);
    }

    public RicePlantingAdapter(List<RicePlanting> plantings, OnPlantingClickListener listener) {
        this.plantings = plantings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlantingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rice_planting, parent, false);
        return new PlantingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantingViewHolder holder, int position) {
        RicePlanting planting = plantings.get(position);
        holder.bind(planting, listener);
    }

    @Override
    public int getItemCount() {
        return plantings != null ? plantings.size() : 0;
    }

    public void updateList(List<RicePlanting> newList) {
        this.plantings = newList;
        notifyDataSetChanged();
    }

    static class PlantingViewHolder extends RecyclerView.ViewHolder {
        private TextView tvVarietyName, tvPlantingDate, tvPlantingDetails;

        public PlantingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVarietyName = itemView.findViewById(R.id.tvVarietyName);
            tvPlantingDate = itemView.findViewById(R.id.tvPlantingDate);
            tvPlantingDetails = itemView.findViewById(R.id.tvPlantingDetails);
        }

        public void bind(RicePlanting planting, OnPlantingClickListener listener) {
            tvVarietyName.setText(planting.getRiceVarietyName() != null ? planting.getRiceVarietyName() : "Unknown Variety");
            
            String plantingDate = planting.getPlantingDate();
            if (plantingDate != null && !plantingDate.isEmpty()) {
                tvPlantingDate.setText("Planting Date: " + plantingDate);
            } else {
                tvPlantingDate.setText("Planting Date: Not set");
            }

            String method = planting.getPlantingMethod() != null && !planting.getPlantingMethod().isEmpty()
                    ? planting.getPlantingMethod()
                    : "Hindi tukoy";
            String seedWeight = planting.getSeedWeight() != null && !planting.getSeedWeight().isEmpty()
                    ? planting.getSeedWeight() + " kg"
                    : "Walang datos";
            String fertilizer = planting.getFertilizerUsed() != null && !planting.getFertilizerUsed().isEmpty()
                    ? planting.getFertilizerUsed()
                    : "Walang abono";
            String fertilizerAmount = planting.getFertilizerAmount() != null && !planting.getFertilizerAmount().isEmpty()
                    ? planting.getFertilizerAmount() + " kg"
                    : "";
            String fertilizerInfo = fertilizerAmount.isEmpty() ? fertilizer : fertilizer + " (" + fertilizerAmount + ")";

            tvPlantingDetails.setText("Paraan: " + method + " | Binhi: " + seedWeight + " | Abono: " + fertilizerInfo);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlantingClick(planting);
                }
            });
        }
    }
}

