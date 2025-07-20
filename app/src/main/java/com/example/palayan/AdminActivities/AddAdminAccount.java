package com.example.palayan.AdminActivities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.palayan.Dialog.CustomDialogFragment;
import com.example.palayan.Dialog.StatusDialogFragment;
import com.example.palayan.Helper.Validator.TextHelp;
import com.example.palayan.R;
import com.example.palayan.databinding.ActivityAddAdminAccountBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AddAdminAccount extends AppCompatActivity {

    private ActivityAddAdminAccountBinding root;
    private FirebaseFirestore firestore;
    private ArrayAdapter<String> roleAdapter;
    private boolean isEditMode = false;
    private int userId = -1;
    private String originalUsername = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityAddAdminAccountBinding.inflate(getLayoutInflater());
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(root.getRoot());

        //instatiate the database
        firestore = FirebaseFirestore.getInstance();

        //role
        roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.roles_array));
        root.spRole.setAdapter(roleAdapter);


        //default visibility
        root.btnCreate.setVisibility(View.VISIBLE);
        root.btnUpdateAccount.setVisibility(View.GONE);

        //for back button
        root.ivBack.setOnClickListener(v -> {
            if (hasInput()) {
                showDiscardDialog();
            } else {
                finish();
            }
        });

        //if update mode
        if (getIntent() != null && getIntent().hasExtra("userId")) {
            isEditMode = true;
            userId = getIntent().getIntExtra("userId", -1);
            if (userId != -1) {
                loadAccountDetails(userId);
                root.btnCreate.setVisibility(View.GONE);
                root.btnUpdateAccount.setVisibility(View.VISIBLE);
            }
        }

        //live validation for text change
        TextHelp.addValidation(root.layoutFullName, root.txtFullName, "Field required");
        TextHelp.addValidation(root.layoutUsername, root.txtUsername, "Field required");
        TextHelp.addValidation(root.layoutPassword, root.txtPassword, "Field required");
        TextHelp.addValidation(root.layoutSecOne, root.txtSecOne, "Field required");
        TextHelp.addValidation(root.layoutSecTwo, root.txtSecTwo, "Field required");
        TextHelp.addAutoCompleteValidation(root.layoutRole, root.spRole, "Selection required");

        //validation for password requirements
        TextHelp.addPasswordRequirementsValidation(
                root.txtPassword,
                root.txtUsername,
                root.cvOneReq, root.ivOneReq, root.tvOneReq,
                root.cvTwoReq, root.ivTwoReq, root.tvTwoReq,
                root.cvThreeReq, root.ivThreeReq, root.tvThreeReq,
                root.cvFourReq, root.ivFourReq, root.tvFourReq,
                root.cvFiveReq, root.ivFiveReq, root.tvFiveReq,
                getResources().getColor(R.color.green),
                getResources().getColor(R.color.light_gray),
                getResources().getColor(R.color.green),
                getResources().getColor(R.color.dark_gray),
                getResources().getColor(R.color.white),
                getResources().getColor(R.color.black)
        );

        //validation for not match
        TextHelp.addConfirmPasswordValidation(
                root.layoutConfirmPass,
                root.txtPassword,
                root.txtConfirmPassword,
                "Password does not match"
        );

        //live validation
        root.txtUsername.addTextChangedListener(
                TextHelp.createUsernameLiveChecker(firestore, root.layoutUsername, originalUsername, isEditMode)
        );


        root.btnCreate.setOnClickListener(v -> showAddConfirmationDialog());

        root.btnUpdateAccount.setOnClickListener(v -> showUpdateConfirmationDialog());
    }

    //retrieving the account
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
                            root.spRole.setText(role, false);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load account.", Toast.LENGTH_SHORT).show()
                );
    }

    //validation for all fields
    private boolean validateAllFields(){

        if (!TextHelp.isFilled(root.layoutFullName, root.txtFullName, "Please fill out this field.")) return false;
        TextHelp.addLetterOnly(root.layoutFullName, root.txtFullName, "Oops! That should only contain letters.");

        if (!TextHelp.isFilled(root.layoutUsername, root.txtUsername, "Please fill out this field.")) return false;

        if (root.spRole.getText().toString().trim().isEmpty()) {
            root.layoutRole.setError("Please select a role");
            return false;
        } else {
            root.layoutRole.setError(null);
        }


        if (!TextHelp.isFilled(root.layoutPassword, root.txtPassword, "Please fill out this field.")) return false;
        String password = root.txtPassword.getText().toString();
        String confirmPassword = root.txtConfirmPassword.getText().toString();
        if (!password.equals(confirmPassword)) {
            root.layoutConfirmPass.setError("Password does not match");
            return false;
        }

        if (!TextHelp.isFilled(root.layoutSecOne, root.txtSecOne, "Please fill out this field.")) return false;
        TextHelp.addLetterOnly(root.layoutSecOne, root.txtSecOne, "Oops! That should only contain letters.");

        if (!TextHelp.isFilled(root.layoutSecTwo, root.txtSecTwo, "Please fill out this field.")) return false;
        TextHelp.addLetterOnly(root.layoutSecTwo, root.txtSecTwo, "Oops! That should only contain letters.");

        return true;
    }

    //to show the confirm dialog
    private void showAddConfirmationDialog() {

        if (!validateAllFields()) return;
        String fullName = root.txtFullName.getText().toString().trim();


        CustomDialogFragment.newInstance(
                "Add Admin Account",
                "Are you sure you want to add \"" + fullName + "\"?",
                "This admin account will be added to the application.",
                R.drawable.ic_account_logo,
                "ADD",
                (dialog, which) -> addNewAccountToDatabase()
        ).show(getSupportFragmentManager(), "AddConfirmDialog");
    }

    //to show the update dialog
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

    //inserting data to the database
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

    //for update account
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

    //get the input
    private HashMap<String, Object> collectFormInput() {
        String fullName = root.txtFullName.getText().toString().trim();
        String username = root.txtUsername.getText().toString().trim();
        String password = root.txtPassword.getText().toString().trim();
        String secOne = root.txtSecOne.getText().toString().trim();
        String secTwo = root.txtSecTwo.getText().toString().trim();
        String selectedRole = root.spRole.getText().toString();


        HashMap<String, Object> account = new HashMap<>();
        account.put("fullName", fullName);
        account.put("username", username);
        account.put("password", password);
        account.put("role", selectedRole);
        account.put("security1", secOne);
        account.put("security2", secTwo);
        account.put("status", "Active");
        account.put("lastActive", null);
        account.put("archived", false);
        return account;
    }

    //to show the success dialog
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

    //when clicked the back
    @Override
    public void onBackPressed() {
        if (hasInput()) {
            showDiscardDialog();
        } else {
            super.onBackPressed();
        }
    }

    //when text field contains input
    private boolean hasInput() {
        return !root.txtFullName.getText().toString().trim().isEmpty()
                || !root.txtUsername.getText().toString().trim().isEmpty()
                || !root.txtPassword.getText().toString().trim().isEmpty()
                || !root.txtConfirmPassword.getText().toString().trim().isEmpty()
                || !root.txtSecOne.getText().toString().trim().isEmpty()
                || !root.txtSecTwo.getText().toString().trim().isEmpty()
                || !root.spRole.getText().toString().trim().isEmpty();
    }

    //to show discard dialog
    private void showDiscardDialog() {
        CustomDialogFragment.newInstance(
                "Discard Changes?",
                "Are you sure you want to discard the entered information?",
                "All unsaved changes will be lost.",
                R.drawable.ic_warning,
                "DISCARD",
                (dialog, which) -> finish()
        ).show(getSupportFragmentManager(), "DiscardDialog");
    }

}
