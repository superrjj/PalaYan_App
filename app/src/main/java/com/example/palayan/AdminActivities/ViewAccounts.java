package com.example.palayan.AdminActivities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.palayan.Adapter.AdminAccountAdapter;
import com.example.palayan.Helper.AdminModel;
import com.example.palayan.Helper.RiceVariety;
import com.example.palayan.databinding.ActivityViewAccountsBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewAccounts extends AppCompatActivity {

    private ActivityViewAccountsBinding root;
    private FirebaseFirestore firestore;
    private AdminAccountAdapter adapter;
    private ArrayList<AdminModel> accountList;
    private ArrayList<AdminModel> fullAccountList;
    private ListenerRegistration accountListener;
    private int currentUserId; //call the userId from the admin model


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityViewAccountsBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        firestore = FirebaseFirestore.getInstance();
        currentUserId = getIntent().getIntExtra("userId", -1);

        //initialize lists before loading accounts
        accountList = new ArrayList<>();
        fullAccountList = new ArrayList<>();

        adapter = new AdminAccountAdapter(accountList, this, currentUserId);
        root.rvAccounts.setLayoutManager(new LinearLayoutManager(this));
        root.rvAccounts.setAdapter(adapter);

        loadAccounts();  //now safe to attach snapshot listener

        root.ivBack.setOnClickListener(v -> onBackPressed());
        root.fabAdd.setOnClickListener(v -> startActivity(new Intent(this, AddAdminAccount.class)));

        root.svSearchBar.setQueryHint("Search for account...");
        root.svSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                filterList(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                filterList(s);
                return true;
            }
        });


    }

    private void loadAccounts() {

        if (accountListener != null) {
            accountListener.remove();  // Remove existing listener before attaching new one
        }

        accountListener = firestore.collection("accounts")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(ViewAccounts.this, "Failed to load accounts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    accountList.clear();
                    fullAccountList.clear();

                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            try {
                                AdminModel account = doc.toObject(AdminModel.class);
                                if (account != null && !account.isArchived()) {
                                    accountList.add(account);
                                    fullAccountList.add(account);
                                }
                            } catch (Exception ex) {
                                Log.e("Firestore Data", "Error parsing account", ex);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                    root.tvNoData.setVisibility(accountList.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }


    // Filter list based on query string
    private void filterList(String query) {
        List<AdminModel> filteredList = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (AdminModel item : fullAccountList) {
            if ((item.fullName != null && item.fullName.toLowerCase().contains(lowerQuery)) ||
                    (item.role != null && item.role.toLowerCase().contains(lowerQuery)) ||
                    (item.status != null && item.status.toLowerCase().contains(lowerQuery))) {
                filteredList.add(item);
            }
        }

        accountList.clear();
        accountList.addAll(filteredList);
        adapter.notifyDataSetChanged();

        root.tvNoData.setVisibility(accountList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (accountListener != null) {
            accountListener.remove();
            accountListener = null;
        }
    }

}
