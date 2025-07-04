package com.example.palayan.AdminActivities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.palayan.databinding.ActivityAddAdminAccountBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class AddAdminAccount extends AppCompatActivity {

    private ActivityAddAdminAccountBinding root;
    private FirebaseFirestore firestore;
    private ArrayAdapter<String> roleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityAddAdminAccountBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        firestore = FirebaseFirestore.getInstance();

        // Spinner for role
        String[] roles = {"Main Admin", "Data Manager"};
        roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles);
        root.spRole.setAdapter(roleAdapter);

        // Create button
        root.btnCreate.setOnClickListener(v -> addAdminAccount());
    }

    private void addAdminAccount() {
        String fullName = root.txtFullName.getText().toString().trim();
        String username = root.txtUsername.getText().toString().trim();
        String password = root.txtPassword.getText().toString().trim();
        String role = root.spRole.getSelectedItem().toString();
        String security1 = root.txtSecOne.getText().toString().trim();
        String security2 = root.txtSecTwo.getText().toString().trim();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()
                || security1.isEmpty() || security2.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare data
        HashMap<String, Object> account = new HashMap<>();
        account.put("fullName", fullName);
        account.put("username", username);
        account.put("password", password);
        account.put("role", role);
        account.put("security1", security1);
        account.put("security2", security2);
        account.put("status", "Active");

        // Get the last used userId
        firestore.collection("accounts")
                .orderBy("userId", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int nextUserId = 1;
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot lastDoc = queryDocumentSnapshots.getDocuments().get(0);
                        Long lastId = lastDoc.getLong("userId");
                        if (lastId != null) {
                            nextUserId = lastId.intValue() + 1;
                        }
                    }


                    account.put("userId", nextUserId);

                    // Add to Firestore with custom doc ID
                    firestore.collection("accounts")
                            .document(String.valueOf(nextUserId))
                            .set(account)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Admin account created.", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to create account.", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch user ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
