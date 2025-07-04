package com.example.palayan.AdminActivities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.palayan.databinding.ActivityAddAdminAccountBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class AddAdminAccount extends AppCompatActivity {

    private ActivityAddAdminAccountBinding root;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityAddAdminAccountBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());
        EdgeToEdge.enable(this);

        firestore = FirebaseFirestore.getInstance();

        // Load role items to spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                com.example.palayan.R.array.roles_array,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        root.spRole.setAdapter(adapter);

        // Create Button click listener
        root.btnCreate.setOnClickListener(v -> createAdminAccount());
    }

    private void createAdminAccount() {
        String fullName = root.txtFullName.getText().toString().trim();
        String username = root.txtUsername.getText().toString().trim();
        String password = root.txtPassword.getText().toString().trim();
        String role = root.spRole.getSelectedItem().toString();
        String secOne = root.txtSecOne.getText().toString().trim();
        String secTwo = root.txtSecTwo.getText().toString().trim();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()
                || role.equals("Select Role") || secOne.isEmpty() || secTwo.isEmpty()) {
            Toast.makeText(this, "Please complete all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch latest userId, then add 1
        firestore.collection("accounts")
                .orderBy("userId", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int nextUserId = 1;
                    if (!queryDocumentSnapshots.isEmpty()) {
                        int lastUserId = queryDocumentSnapshots.getDocuments().get(0).getLong("userId").intValue();
                        nextUserId = lastUserId + 1;
                    }

                    Map<String, Object> account = new HashMap<>();
                    account.put("userId", nextUserId);
                    account.put("fullName", fullName);
                    account.put("username", username);
                    account.put("password", password);
                    account.put("role", role);
                    account.put("securityOne", secOne);
                    account.put("securityTwo", secTwo);
                    account.put("status", "Active");

                    firestore.collection("accounts")
                            .add(account)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(this, "Account created successfully.", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to create account.", Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch latest userId.", Toast.LENGTH_SHORT).show();
                });
    }
}
