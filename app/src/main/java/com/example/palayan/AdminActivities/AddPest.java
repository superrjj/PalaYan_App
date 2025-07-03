package com.example.palayan.AdminActivities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.palayan.Helper.Pest;
import com.example.palayan.R;
import com.example.palayan.databinding.ActivityAddPestBinding;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class AddPest extends AppCompatActivity {

    private ActivityAddPestBinding root;
    private static final int REQUEST_IMAGE_PICK = 1;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       root = ActivityAddPestBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        root.ivBack.setOnClickListener(v -> onBackPressed());

        View.OnClickListener uploadClick = v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Image"), REQUEST_IMAGE_PICK);
        };

        root.ivUploadImage.setOnClickListener(uploadClick);
        root.tvTapToUpload.setOnClickListener(uploadClick);

        root.btnAddPest.setOnClickListener(v -> {
            String name = root.txtPestName.getText().toString().trim();
            String sciName = root.txtScientificName.getText().toString().trim();
            String desc = root.txtDescription.getText().toString().trim();
            String cause = root.txtCause.getText().toString().trim();
            String treat = root.txtTreatments.getText().toString().trim();

            if (name.isEmpty() || sciName.isEmpty() || desc.isEmpty() || cause.isEmpty() || treat.isEmpty() || imageUri == null) {
                Toast.makeText(this, "Please fill in all fields and select an image", Toast.LENGTH_SHORT).show();
                return;
            }

            uploadPestToFirebase(name, sciName, desc, cause, treat);
        });

    }

    private void uploadPestToFirebase(String name, String sciName, String desc, String cause, String treat) {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading...");
        dialog.setCancelable(false);
        dialog.show();

        // Convert name to Firebase-friendly ID
        String pestId = name.trim().toLowerCase().replaceAll("[^a-z0-9]+", "_");

        // Upload image first
        String imageName = UUID.randomUUID().toString();
        StorageReference imageRef = FirebaseStorage.getInstance().getReference("pest_images/" + imageName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Pest pest = new Pest(pestId, name, sciName, desc, cause, treat, uri.toString());

                    FirebaseDatabase.getInstance().getReference("pests")
                            .child(pestId)
                            .setValue(pest)
                            .addOnSuccessListener(unused -> {
                                dialog.dismiss();
                                Toast.makeText(this, "Pest added successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                dialog.dismiss();
                                Toast.makeText(this, "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }))
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            root.ivUploadImage.setImageURI(imageUri);
            root.tvTapToUpload.setVisibility(View.GONE); // Hide the text once image is set
        }
    }

}