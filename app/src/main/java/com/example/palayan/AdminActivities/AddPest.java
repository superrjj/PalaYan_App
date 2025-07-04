package com.example.palayan.AdminActivities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.palayan.Dialog.CustomDialogFragment;
import com.example.palayan.Dialog.StatusDialogFragment;
import com.example.palayan.Helper.Pest;
import com.example.palayan.R;
import com.example.palayan.databinding.ActivityAddPestBinding;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class AddPest extends AppCompatActivity {

    private ActivityAddPestBinding root;
    private Uri imageUri;
    private File photoFile;
    private boolean isEditMode = false;
    private String existingPestId;

    // Choose from gallery
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    root.ivUploadImage.setImageURI(imageUri);
                    root.tvTapToUpload.setVisibility(View.GONE);
                }
            });

    // Take a photo
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && imageUri != null) {
                    root.ivUploadImage.setImageURI(imageUri);
                    root.tvTapToUpload.setVisibility(View.GONE);
                }
            });

    private final ActivityResultLauncher<String> requestCameraPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    launchCamera();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityAddPestBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        root.ivBack.setOnClickListener(v -> onBackPressed());

        View.OnClickListener uploadClick = v -> showImagePickerDialog();
        root.ivUploadImage.setOnClickListener(uploadClick);
        root.tvTapToUpload.setOnClickListener(uploadClick);

        if (getIntent().getBooleanExtra("isEdit", false)) {
            isEditMode = true;
            existingPestId = getIntent().getStringExtra("pest_id");

            root.txtPestName.setText(getIntent().getStringExtra("pestName"));
            root.txtPestName.setEnabled(false);
            root.txtScientificName.setText(getIntent().getStringExtra("scientificName"));
            root.txtDescription.setText(getIntent().getStringExtra("description"));
            root.txtCause.setText(getIntent().getStringExtra("cause"));
            root.txtTreatments.setText(getIntent().getStringExtra("treatments"));

            root.btnAddPest.setVisibility(View.GONE);
            root.btnUpdatePest.setVisibility(View.VISIBLE);
            root.btnUpdatePest.setOnClickListener(view -> showUpdateConfirmationDialog(existingPestId));
        } else {
            root.btnAddPest.setVisibility(View.VISIBLE);
            root.btnUpdatePest.setVisibility(View.GONE);
            root.btnAddPest.setOnClickListener(view -> showAddConfirmationDialog());
        }
    }

    private void showImagePickerDialog() {
        String[] options = {"Take a Photo", "Choose from Gallery"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                == PackageManager.PERMISSION_GRANTED) {
                            launchCamera();
                        } else {
                            requestCameraPermission.launch(Manifest.permission.CAMERA);
                        }
                    } else {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        galleryLauncher.launch(intent);
                    }
                }).show();
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show();
            return;
        }

        if (photoFile != null) {
            imageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraLauncher.launch(intent);
        }
    }

    private File createImageFile() throws IOException {
        String fileName = "IMG_" + System.currentTimeMillis();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(fileName, ".jpg", storageDir);
    }

    private void showAddConfirmationDialog() {
        String name = root.txtPestName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Pest name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        CustomDialogFragment.newInstance(
                "Add Pest",
                "Are you sure you want to add \"" + name + "\"?",
                "This pest will be added to the system.",
                R.drawable.ic_rice_logo,
                "ADD",
                (dialog, which) -> addPestToDatabase(name)
        ).show(getSupportFragmentManager(), "AddConfirmDialog");
    }

    private void showUpdateConfirmationDialog(String id) {
        CustomDialogFragment.newInstance(
                "Update Pest",
                "Are you sure you want to update \"" + id + "\"?",
                "The changes will be saved and take effect immediately.",
                R.drawable.ic_edit,
                "UPDATE",
                (dialog, which) -> updatePest(id)
        ).show(getSupportFragmentManager(), "UpdateConfirmDialog");
    }

    private void addPestToDatabase(String name) {
        String sciName = root.txtScientificName.getText().toString().trim();
        String desc = root.txtDescription.getText().toString().trim();
        String cause = root.txtCause.getText().toString().trim();
        String treat = root.txtTreatments.getText().toString().trim();

        if (sciName.isEmpty() || desc.isEmpty() || cause.isEmpty() || treat.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Please complete all fields and select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading...");
        dialog.setCancelable(false);
        dialog.show();

        String pestId = name.toLowerCase().replaceAll("[^a-z0-9]+", "_");
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
                                showSuccessDialog("added", name);
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

    private void updatePest(String pestId) {
        String name = root.txtPestName.getText().toString().trim();
        String sciName = root.txtScientificName.getText().toString().trim();
        String desc = root.txtDescription.getText().toString().trim();
        String cause = root.txtCause.getText().toString().trim();
        String treat = root.txtTreatments.getText().toString().trim();

        if (sciName.isEmpty() || desc.isEmpty() || cause.isEmpty() || treat.isEmpty()) {
            Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Updating...");
        dialog.setCancelable(false);
        dialog.show();

        if (imageUri != null) {
            String imageName = UUID.randomUUID().toString();
            StorageReference imageRef = FirebaseStorage.getInstance().getReference("pest_images/" + imageName);

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Pest pest = new Pest(pestId, name, sciName, desc, cause, treat, uri.toString());
                        updateDatabase(pestId, pest, dialog);
                    }))
                    .addOnFailureListener(e -> {
                        dialog.dismiss();
                        Toast.makeText(this, "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            String imageUrl = getIntent().getStringExtra("imageUrl");
            Pest pest = new Pest(pestId, name, sciName, desc, cause, treat, imageUrl);
            updateDatabase(pestId, pest, dialog);
        }
    }

    private void updateDatabase(String pestId, Pest pest, ProgressDialog dialog) {
        FirebaseDatabase.getInstance().getReference("pests")
                .child(pestId)
                .setValue(pest)
                .addOnSuccessListener(unused -> {
                    dialog.dismiss();
                    showSuccessDialog("updated", pest.getPestName());
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Update Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showSuccessDialog(String action, String name) {
        StatusDialogFragment.newInstance(
                        "Pest " + action,
                        name + " has been successfully " + action + ".",
                        R.drawable.ic_success,
                        R.color.green
                ).setOnDismissListener(this::finish)
                .show(getSupportFragmentManager(), "SuccessDialog");
    }
}
