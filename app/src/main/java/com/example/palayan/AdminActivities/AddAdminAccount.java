package com.example.palayan.AdminActivities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.palayan.Dialog.CustomDialogFragment;
import com.example.palayan.Dialog.StatusDialogFragment;
import com.example.palayan.R;
import com.example.palayan.databinding.ActivityAddAdminAccountBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class AddAdminAccount extends AppCompatActivity {

    private ActivityAddAdminAccountBinding root;
    private FirebaseFirestore firestore;
    private ArrayAdapter<String> roleAdapter;
    private boolean isEditMode = false;
    private int userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityAddAdminAccountBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> onBackPressed());

        firestore = FirebaseFirestore.getInstance();

        // Spinner with placeholder
        String[] roles = {"Select Role", "Main Admin", "Data Manager"};
        roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles);
        root.spRole.setAdapter(roleAdapter);

        // Default visibility
        root.btnCreate.setVisibility(View.VISIBLE);
        root.btnUpdateAccount.setVisibility(View.GONE);

        // If edit mode
        if (getIntent() != null && getIntent().hasExtra("userId")) {
            isEditMode = true;
            userId = getIntent().getIntExtra("userId", -1);
            if (userId != -1) {
                loadAccountDetails(userId);
                root.btnCreate.setVisibility(View.GONE);
                root.btnUpdateAccount.setVisibility(View.VISIBLE);
            }
        }

        root.btnCreate.setOnClickListener(v -> showAddConfirmationDialog());
        root.btnUpdateAccount.setOnClickListener(v -> showUpdateConfirmationDialog());
    }

    private void loadAccountDetails(int userId) {
        firestore.collection("accounts")
                .document(String.valueOf(userId))
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        root.txtFullName.setText(doc.getString("fullName"));
                        root.txtUsername.setText(doc.getString("username"));
                        root.txtPassword.setText(doc.getString("password"));
                        root.txtSecOne.setText(doc.getString("security1"));
                        root.txtSecTwo.setText(doc.getString("security2"));

                        String role = doc.getString("role");
                        int spinnerPosition = roleAdapter.getPosition(role);
                        if (spinnerPosition >= 0) {
                            root.spRole.setSelection(spinnerPosition);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load account.", Toast.LENGTH_SHORT).show()
                );
    }

    private void showAddConfirmationDialog() {
        String fullName = root.txtFullName.getText().toString().trim();
        if (fullName.isEmpty()) {
            Toast.makeText(this, "Full Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        CustomDialogFragment.newInstance(
                "Add Admin Account",
                "Are you sure you want to add \"" + fullName + "\"?",
                "This admin account will be added to the application.",
                R.drawable.ic_account_logo,
                "ADD",
                (dialog, which) -> addNewAccountToDatabase()
        ).show(getSupportFragmentManager(), "AddConfirmDialog");
    }

    private void showUpdateConfirmationDialog() {
        String fullName = root.txtFullName.getText().toString().trim();
        if (fullName.isEmpty()) {
            Toast.makeText(this, "Full Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        CustomDialogFragment.newInstance(
                "Update Admin Account",
                "Are you sure you want to update \"" + fullName + "\"?",
                "The changes will take effect immediately.",
                R.drawable.ic_edit,
                "UPDATE",
                (dialog, which) -> updateAccount()
        ).show(getSupportFragmentManager(), "UpdateConfirmDialog");
    }

    private void addNewAccountToDatabase() {
        HashMap<String, Object> account = collectFormInput();
        if (account == null) return;

        firestore.collection("accounts")
                .orderBy("userId", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(docs -> {
                    int nextUserId = 1;
                    if (!docs.isEmpty()) {
                        DocumentSnapshot lastDoc = docs.getDocuments().get(0);
                        Long lastId = lastDoc.getLong("userId");
                        if (lastId != null) nextUserId = lastId.intValue() + 1;
                    }

                    account.put("userId", nextUserId);
                    firestore.collection("accounts")
                            .document(String.valueOf(nextUserId))
                            .set(account)
                            .addOnSuccessListener(unused -> showSuccessDialog("added"))
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to add account.", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch ID.", Toast.LENGTH_SHORT).show());
    }

    private void updateAccount() {
        if (userId == -1) return;

        HashMap<String, Object> account = collectFormInput();
        if (account == null) return;

        account.put("userId", userId);

        firestore.collection("accounts")
                .document(String.valueOf(userId))
                .set(account)
                .addOnSuccessListener(unused -> showSuccessDialog("updated"))
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update account.", Toast.LENGTH_SHORT).show());
    }

    private HashMap<String, Object> collectFormInput() {
        String fullName = root.txtFullName.getText().toString().trim();
        String username = root.txtUsername.getText().toString().trim();
        String password = root.txtPassword.getText().toString().trim();
        String selectedRole = root.spRole.getSelectedItem().toString();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return null;
        }

        if (selectedRole.equals("Select Role")) {
            Toast.makeText(this, "Please select a valid role", Toast.LENGTH_SHORT).show();
            return null;
        }

        HashMap<String, Object> account = new HashMap<>();
        account.put("fullName", fullName);
        account.put("username", username);
        account.put("password", password);
        account.put("role", selectedRole);
        account.put("security1", root.txtSecOne.getText().toString().trim());
        account.put("security2", root.txtSecTwo.getText().toString().trim());
        account.put("status", "Active");
        account.put("lastActive", null);
        account.put("archived", false);
        return account;
    }

    private void showSuccessDialog(String action) {
        String fullName = root.txtFullName.getText().toString().trim();
        String title = "Admin Account " + (action.equals("updated") ? "Updated" : "Added");
        String message = fullName + " has been successfully " + action + ".";

        StatusDialogFragment.newInstance(
                        title,
                        message,
                        R.drawable.ic_success,
                        R.color.green
                ).setOnDismissListener(() -> finish())
                .show(getSupportFragmentManager(), "SuccessDialog");
    }
}
