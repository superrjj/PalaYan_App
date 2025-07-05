package com.example.palayan.AdminActivities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.palayan.Adapter.AdminAccountAdapter;
import com.example.palayan.Helper.AdminModel;
import com.example.palayan.databinding.ActivityViewAccountsBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ViewAccounts extends AppCompatActivity {

    private ActivityViewAccountsBinding root;
    private FirebaseFirestore firestore;
    private AdminAccountAdapter adapter;
    private ArrayList<AdminModel> accountList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityViewAccountsBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        firestore = FirebaseFirestore.getInstance();
        accountList = new ArrayList<>();
        adapter = new AdminAccountAdapter(accountList, this);

        root.rvAccounts.setLayoutManager(new LinearLayoutManager(this));
        root.rvAccounts.setAdapter(adapter);

        root.ivBack.setOnClickListener(v -> onBackPressed());
        root.fabAdd.setOnClickListener(v -> startActivity(new Intent(this, AddAdminAccount.class)));

        loadAccounts();
    }

    private void loadAccounts() {
        firestore.collection("accounts")
                .whereEqualTo("archived", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    accountList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        AdminModel account = doc.toObject(AdminModel.class);
                        accountList.add(account);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load accounts.", Toast.LENGTH_SHORT).show();
                });
    }
}
