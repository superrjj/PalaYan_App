package com.example.palayan.Adapter;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.AlertDialog;
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

import com.bumptech.glide.Glide;
import com.example.palayan.AdminActivities.AddPest;
import com.example.palayan.Dialog.CustomDialogFragment;
import com.example.palayan.Helper.Pest;
import com.example.palayan.PestDetails;
import com.example.palayan.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AdminPestAdapter extends RecyclerView.Adapter<AdminPestAdapter.PestHolder> {
    private List<Pest> pestList;
    private Context context;

    public AdminPestAdapter(List<Pest> pestList, Context context) {
        this.pestList = pestList;
        this.context = context;
    }

    @NonNull
    @Override
    public PestHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_view_pest_admin, viewGroup, false);
        return new PestHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PestHolder holder, int position) {
        Pest pest = pestList.get(position);
        holder.tvPestName.setText(pest.getPestName());
        holder.tvSciName.setText(pest.getScientificName());

        Glide.with(context)
                .load(pest.getImageUrl())
                .placeholder(R.drawable.loading_image)
                .into(holder.pestImage);

        // Edit button
        holder.imgUpdate.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddPest.class);
            intent.putExtra("isEdit", true);
            intent.putExtra("pest_id", pest.getPest_id());
            intent.putExtra("pestName", pest.getPestName());
            intent.putExtra("scientificName", pest.getScientificName());
            intent.putExtra("description", pest.getDescription());
            intent.putExtra("symptoms", pest.getSymptoms());
            intent.putExtra("cause", pest.getCause());
            intent.putExtra("treatments", pest.getTreatments());
            intent.putExtra("imageUrl", pest.getImageUrl());
            context.startActivity(intent);
        });

        holder.imgDelete.setOnClickListener(v -> {
            if (context instanceof FragmentActivity) {
                CustomDialogFragment.newInstance(
                        "Delete Pest",
                        "Are you sure you want to delete \"" + pest.getPestName() + "\"?",
                        "This pest will be removed from the application.",
                        R.drawable.ic_warning,
                        "DELETE",
                        (dialog, which) -> {
                            FirebaseFirestore.getInstance()
                                    .collection("pests")
                                    .document(pest.getPest_id())
                                    .update("archived", true)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(context, "Deleted \"" + pest.getPestName() + "\"", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Failed to archive: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                ).show(((FragmentActivity) context).getSupportFragmentManager(), "ArchiveConfirmDialog");
            } else {
                Toast.makeText(context, "Cannot show dialog. Invalid context.", Toast.LENGTH_SHORT).show();
            }
        });

        holder.cvPest.setOnClickListener(v -> {
            Intent intent = new Intent(context, PestDetails.class);
            intent.putExtra("pest_id", pest.getPest_id());
            context.startActivity(intent);
        });


    }

    @Override
    public int getItemCount() {
        return pestList.size();
    }

    public static class PestHolder extends RecyclerView.ViewHolder {

        TextView tvPestName, tvSciName;
        ImageView pestImage, imgDelete, imgUpdate;
        CardView cvPest;

        public PestHolder(@NonNull View itemView) {
            super(itemView);
            tvPestName = itemView.findViewById(R.id.tvPestName);
            tvSciName = itemView.findViewById(R.id.tvScientificName);
            pestImage = itemView.findViewById(R.id.ivPestImage);
            imgDelete = itemView.findViewById(R.id.iv_delete);
            imgUpdate = itemView.findViewById(R.id.iv_edit);
            cvPest = itemView.findViewById(R.id.cvPestView);
        }
    }
}
