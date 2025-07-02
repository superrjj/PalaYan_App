package com.example.palayan.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.palayan.AdminActivities.AddRiceVariety;
import com.example.palayan.Dialog.CustomDialogFragment;
import com.example.palayan.Helper.RiceVariety;
import com.example.palayan.R;
import com.example.palayan.RiceVarietyInformation;
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
        holder.location.setText(variety.location);
        holder.release_year.setText(variety.yearRelease);

        // Show variety info
        holder.cvRiceVariety.setOnClickListener(v -> {
            Intent intent = new Intent(context, RiceVarietyInformation.class);
            intent.putExtra("rice_seed_id", variety.rice_seed_id);
            context.startActivity(intent);
        });

        // Delete
        holder.btnDelete.setOnClickListener(v -> {
            if (context instanceof FragmentActivity) {
                CustomDialogFragment.newInstance(
                        "Delete Rice Variety",
                        "Are you sure you want to delete \"" + variety.varietyName + "\"?",
                        "All data associated with this rice may be permanently removed or become inaccessible.",
                        R.drawable.ic_warning,
                        "DELETE",
                        (dialog, which) -> {
                            FirebaseDatabase.getInstance()
                                    .getReference("rice_seed_varieties")
                                    .child(variety.rice_seed_id)
                                    .child("archived")
                                    .setValue(true)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, "Deleted \"" + variety.varietyName + "\"", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                ).show(((FragmentActivity) context).getSupportFragmentManager(), "DeleteConfirmDialog");
            } else {
                Toast.makeText(context, "Cannot show dialog. Invalid context.", Toast.LENGTH_SHORT).show();
            }
        });

        // Edit
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddRiceVariety.class);
            intent.putExtra("isEdit", true);
            intent.putExtra("rice_seed_id", variety.rice_seed_id);
            intent.putExtra("varietyName", variety.varietyName);
            intent.putExtra("releaseName", variety.releaseName);
            intent.putExtra("breedingCode", variety.breedingCode);
            intent.putExtra("yearRelease", variety.yearRelease);
            intent.putExtra("breederOrigin", variety.breederOrigin);
            intent.putExtra("maturityDays", variety.maturityDays);
            intent.putExtra("plantHeight", variety.plantHeight);
            intent.putExtra("averageYield", variety.averageYield);
            intent.putExtra("tillers", variety.tillers);
            intent.putExtra("maxYield", variety.maxYield);
            intent.putExtra("location", variety.location);
            intent.putExtra("environment", variety.environment);
            intent.putExtra("season", variety.season);
            intent.putExtra("plantingMethod", variety.plantingMethod);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class VarietyViewHolder extends RecyclerView.ViewHolder {
        TextView variety_name, location, release_year;
        ImageView btnDelete, btnEdit;
        CardView cvRiceVariety;

        public VarietyViewHolder(@NonNull View itemView) {
            super(itemView);
            variety_name = itemView.findViewById(R.id.tv_variety_name);
            location = itemView.findViewById(R.id.tv_location);
            release_year = itemView.findViewById(R.id.tv_release_year);
            cvRiceVariety = itemView.findViewById(R.id.cvRiceVarieties);
            btnDelete = itemView.findViewById(R.id.iv_delete);
            btnEdit = itemView.findViewById(R.id.iv_edit);
        }
    }
}
