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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.palayan.AdminActivities.AddAdminAccount;
import com.example.palayan.Dialog.CustomDialogFragment;
import com.example.palayan.Helper.AdminModel;
import com.example.palayan.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AdminAccountAdapter extends RecyclerView.Adapter<AdminAccountAdapter.AccountHolder> {

    private List<AdminModel> accountList;
    private Context context;
    private FirebaseFirestore firestore;
    private int currentUserId;

    public AdminAccountAdapter(List<AdminModel> accountList, Context context, int currentUserId) {
        this.accountList = accountList;
        this.context = context;
        this.currentUserId = currentUserId;
        firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public AccountHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_accounts, parent, false);
        return new AccountHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountHolder holder, int position) {

        AdminModel model = accountList.get(position);

        holder.tvFullName.setText(model.getFullName());
        holder.tvRole.setText(model.getRole());

        // Generate initials
        String[] nameParts = model.getFullName().split(" ");
        String initials = "";
        if (nameParts.length >= 2) {
            initials = nameParts[0].substring(0, 1).toUpperCase() + nameParts[1].substring(0, 1).toUpperCase();
        } else if (nameParts.length == 1) {
            initials = nameParts[0].substring(0, 1).toUpperCase();
        }
        holder.tvInitialName.setText(initials);

        //status based on lastActive
        if (model.getLastActive() != null) {
            long lastActiveMillis = model.getLastActive().getTime();
            long currentMillis = System.currentTimeMillis();
            long diffInDays = (currentMillis - lastActiveMillis) / (1000 * 60 * 60 * 24);

            if (diffInDays <= 7) {
                holder.tvStatus.setText("Active");
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.green));
            } else {
                holder.tvStatus.setText("Inactive");
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.dark_orange));
            }
        } else {
            holder.tvStatus.setText("Inactive");
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.dark_orange));
        }

        //cannot delete the accounts who currently logged
        if(model.getUserId() == currentUserId){
            holder.ivDelete.setVisibility(View.GONE);
        }else{
            holder.ivDelete.setVisibility(View.VISIBLE);
        }

        // Archive button
        holder.ivDelete.setOnClickListener(v -> {
            if (context instanceof FragmentActivity) {
                CustomDialogFragment.newInstance(
                        "Archive Account",
                        "Are you sure you want to archive \"" + model.getFullName() + "\"?",
                        "This account will be archived and no longer visible.",
                        R.drawable.ic_warning,
                        "ARCHIVE",
                        (dialog, which) -> {
                            int currentPosition = holder.getBindingAdapterPosition();
                            if (currentPosition != RecyclerView.NO_POSITION) {
                                holder.ivDelete.setEnabled(false);

                                firestore.collection("accounts")
                                        .document(String.valueOf(model.getUserId()))
                                        .update("archived", true)
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(context, "Archived " + model.getFullName(), Toast.LENGTH_SHORT).show();

                                            int updatedPosition = holder.getBindingAdapterPosition();
                                            if (updatedPosition != RecyclerView.NO_POSITION && updatedPosition < accountList.size()) {
                                                accountList.remove(updatedPosition);
                                                notifyItemRemoved(updatedPosition);
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(context, "Failed to archive: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            holder.ivDelete.setEnabled(true);
                                        });
                            }
                        }
                ).show(((FragmentActivity) context).getSupportFragmentManager(), "ArchiveConfirmDialog");
            }
        });

        // Update button
        holder.ivUpdate.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddAdminAccount.class);
            intent.putExtra("userId", model.getUserId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }

    public static class AccountHolder extends RecyclerView.ViewHolder {
        TextView tvFullName, tvRole, tvStatus, tvInitialName;
        ImageView ivUpdate, ivDelete;

        public AccountHolder(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvInitialName = itemView.findViewById(R.id.tvInitialName);
            ivUpdate = itemView.findViewById(R.id.ivUpdate);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }
}
