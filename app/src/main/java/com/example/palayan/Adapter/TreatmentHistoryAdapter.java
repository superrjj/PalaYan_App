package com.example.palayan.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.palayan.Helper.TreatmentHistoryModel;
import com.example.palayan.R;

import java.util.List;

public class TreatmentHistoryAdapter extends RecyclerView.Adapter<TreatmentHistoryAdapter.ViewHolder> {

    private final Context context;
    private final List<TreatmentHistoryModel> treatmentList;

    public TreatmentHistoryAdapter(Context context, List<TreatmentHistoryModel> treatmentList) {
        this.context = context;
        this.treatmentList = treatmentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_treatment_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TreatmentHistoryModel item = treatmentList.get(position);

        holder.tvDate.setText(item.getDate());
        holder.tvDescription.setText(item.getDescription());
    }

    @Override
    public int getItemCount() {
        return treatmentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }
}
