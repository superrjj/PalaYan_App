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
import androidx.recyclerview.widget.RecyclerView;

import com.example.palayan.AdminActivities.AddAdminAccount;
import com.example.palayan.Helper.AdminModel;
import com.example.palayan.R;
import com.example.palayan.databinding.ListAccountsBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AdminAccountAdapter extends RecyclerView.Adapter<AdminAccountAdapter.AccountHolder> {

    private List<AdminModel> accountList;
    private Context context;
    private FirebaseFirestore firestore;


    public AdminAccountAdapter(List<AdminModel> accountList, Context context) {
        this.accountList = accountList;
        this.context = context;
        firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public AdminAccountAdapter.AccountHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_accounts, parent, false);
        return new AccountHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminAccountAdapter.AccountHolder accountHolder, int position) {

        AdminModel model = accountList.get(position);
        accountHolder.tvFullName.setText(model.getFullName());
        accountHolder.tvRole.setText(model.getRole());
        accountHolder.tvStatus.setText(model.getStatus());

        String[] nameParts = model.getFullName().split(" ");
        String initials = "";
        if (nameParts.length >= 2) {
            initials = nameParts[0].substring(0, 1).toUpperCase() + nameParts[1].substring(0, 1).toUpperCase();
        } else if (nameParts.length == 1) {
            initials = nameParts[0].substring(0, 1).toUpperCase();
        }
        accountHolder.tvInitialName.setText(initials);

        if (model.getLastActive() != null) {
            long lastActiveMillis = model.getLastActive().getTime();
            long currentMillis = System.currentTimeMillis();
            long diffInDays = (currentMillis - lastActiveMillis) / (1000 * 60 * 60 * 24);

            if (diffInDays <= 7) {
                accountHolder.tvStatus.setText("Active");
                accountHolder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.green));
            } else {
                accountHolder.tvStatus.setText("Inactive");
                accountHolder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.dark_orange));
            }
        } else {
            // If no lastActive date, treat as Inactive
            accountHolder.tvStatus.setText("Inactive");
            accountHolder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.dark_orange));
        }



        accountHolder.ivDelete.setOnClickListener(v ->{
            String userId = String.valueOf(model.getUserId());
            firestore.collection("accounts")
                    .document(userId)
                    .update("archived", true)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(context, "Archived " + model.getFullName(), Toast.LENGTH_SHORT).show();
                        accountList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, accountList.size());
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to archive: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        accountHolder.ivUpdate.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddAdminAccount.class);
            intent.putExtra("userId", model.getUserId());
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }

    public class AccountHolder extends RecyclerView.ViewHolder{
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
