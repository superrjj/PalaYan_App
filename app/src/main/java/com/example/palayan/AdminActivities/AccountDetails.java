package com.example.palayan.AdminActivities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.palayan.Helper.AdminModel;
import com.example.palayan.Helper.RiceVariety;
import com.example.palayan.databinding.ActivityAccountDetailsBinding;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class AccountDetails extends AppCompatActivity {

    private ActivityAccountDetailsBinding root;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityAccountDetailsBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        firestore = FirebaseFirestore.getInstance();


        int userId = getIntent().getIntExtra("userId", -1);
        if (userId != -1) {
            loadData(userId);
        }

        root.ivBack.setOnClickListener(view -> onBackPressed());

    }

    private void loadData(int userId) {
        firestore.collection("accounts")
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        AdminModel model = doc.toObject(AdminModel.class);

                        if (model != null) {
                            root.txtFullName.setText(model.fullName);
                            root.txtUsername.setText(model.username);
                            root.txtPassword.setText(model.password);
                            root.txtRole.setText(model.role);
                            root.txtSec1.setText(model.security1);
                            root.txtSec2.setText(model.security2);
                        }
                    } else {
                        Toast.makeText(this, "Account not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    }