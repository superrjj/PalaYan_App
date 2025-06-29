package com.example.palayan.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.palayan.Helper.RiceVariety;
import com.example.palayan.R;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class RiceVarietyAdapter extends RecyclerView.Adapter<RiceVarietyAdapter.VarietyViewHolder> {

    private List<RiceVariety> list;
    private Context context;

    public RiceVarietyAdapter(List<RiceVariety> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public VarietyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_rice_seed_admin, parent, false);
        return new VarietyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VarietyViewHolder holder, int position) {
        RiceVariety variety = list.get(position);

        holder.variety_name.setText(variety.varietyName);
        holder.release_name.setText(variety.location);
        holder.variety_description.setText("Tap to see description");

        holder.btnDelete.setOnClickListener(v -> {
            FirebaseDatabase.getInstance()
                    .getReference("rice_seed_varieties")
                    .child(variety.rice_seed_id)
                    .child("archived")
                    .setValue(true)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(context, "Rice variety deleted.", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Failed to deleted.", Toast.LENGTH_SHORT).show()
                    );
        });

        holder.btnEdit.setOnClickListener(v ->
                Toast.makeText(context, "Edit clicked for " + variety.breedingCode, Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class VarietyViewHolder extends RecyclerView.ViewHolder {
        TextView variety_name, release_name, variety_description;
        ImageView btnDelete, btnEdit;

        public VarietyViewHolder(@NonNull View itemView) {
            super(itemView);
            variety_name = itemView.findViewById(R.id.tv_variety_name);
            release_name = itemView.findViewById(R.id.tv_location);
            variety_description = itemView.findViewById(R.id.tv_description);
            btnDelete = itemView.findViewById(R.id.iv_delete);
            btnEdit = itemView.findViewById(R.id.iv_edit);
        }
    }
}
