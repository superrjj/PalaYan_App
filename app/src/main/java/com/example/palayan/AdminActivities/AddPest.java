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
import androidx.core.view.WindowCompat;

import com.bumptech.glide.Glide;
import com.example.palayan.Dialog.CustomDialogFragment;
import com.example.palayan.Dialog.StatusDialogFragment;
import com.example.palayan.Helper.Pest;
import com.example.palayan.Helper.Validator.TextHelp;
import com.example.palayan.R;
import com.example.palayan.databinding.ActivityAddPestBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
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
    private String existingImageUrl;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    root.ivUploadImage.setImageURI(imageUri);
                    root.tvTapToUpload.setVisibility(View.GONE);
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Broadcast to notify media scanner
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(imageUri);
                sendBroadcast(mediaScanIntent);

                // Load captured image
                root.ivUploadImage.setImageURI(imageUri);
                root.tvTapToUpload.setVisibility(View.GONE);
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
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(root.getRoot());

        root.ivBack.setOnClickListener(v -> {
            if (hasInput()) {
                showDiscardDialog();
            } else {
                finish();
            }
        });

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
            root.txtSymptoms.setText(getIntent().getStringExtra("symptoms"));
            root.txtCause.setText(getIntent().getStringExtra("cause"));
            root.txtTreatments.setText(getIntent().getStringExtra("treatments"));
            existingImageUrl = getIntent().getStringExtra("imageUrl");

            //display the existing image
            Glide.with(this)
                    .load(existingImageUrl)
                    .placeholder(R.drawable.ic_pest_logo)
                    .into(root.ivUploadImage);

            root.tvTapToUpload.setVisibility(View.GONE);

            root.btnAddPest.setVisibility(View.GONE);
            root.btnUpdatePest.setVisibility(View.VISIBLE);
            root.btnUpdatePest.setOnClickListener(view -> showUpdateConfirmationDialog(existingPestId));
        } else {
            root.btnAddPest.setVisibility(View.VISIBLE);
            root.btnUpdatePest.setVisibility(View.GONE);
            root.btnAddPest.setOnClickListener(view -> showAddConfirmationDialog());
        }

        TextHelp.addValidation(root.layoutPestName, root.txtPestName, "Field required");
        TextHelp.addValidation(root.layoutScientificName, root.txtScientificName, "Field required");
        TextHelp.addValidation(root.layoutDescription, root.txtDescription, "Field required");
        TextHelp.addValidation(root.layoutSymptoms, root.txtSymptoms, "Field required");
        TextHelp.addValidation(root.layoutCause, root.txtCause, "Field required");
        TextHelp.addValidation(root.layoutTreatments, root.txtTreatments, "Field required");

        TextHelp.enableClearIcon(root.layoutPestName, root.txtPestName);
        TextHelp.enableClearIcon(root.layoutScientificName, root.txtScientificName);
        TextHelp.enableClearIcon(root.layoutDescription, root.txtDescription);
        TextHelp.enableClearIcon(root.layoutCause, root.txtCause);
        TextHelp.enableClearIcon(root.layoutTreatments, root.txtTreatments);
        TextHelp.enableClearIcon(root.layoutSymptoms, root.txtSymptoms);

    }

    private boolean validateAllField()
    {

        boolean isValid = true;

        if(!TextHelp.isFilled(root.layoutPestName, root.txtPestName, "Please fill out this field."))return false;
        TextHelp.addLetterOnly(root.layoutPestName, root.txtPestName, "Oops! That should only contain letters.");

        if(!TextHelp.isFilled(root.layoutScientificName, root.txtScientificName, "Please fill out this field.")) return false;
        TextHelp.addLetterOnly(root.layoutScientificName, root.txtScientificName, "Oops! That should only contain letters.");

        if(!TextHelp.isFilled(root.layoutDescription, root.txtDescription, "Please fill out this field.")) return false;
        TextHelp.addLetterOnly(root.layoutDescription, root.txtDescription, "Oops! That should only contain letters.");

        if(!TextHelp.isFilled(root.layoutCause, root.txtCause, "Please fill out this field.")) return false;
        TextHelp.addLetterOnly(root.layoutCause, root.txtCause, "Oops! That should only contain letters.");

        if(!TextHelp.isFilled(root.layoutSymptoms, root.txtSymptoms, "Please fill out this field.")) return false;
        TextHelp.addLetterOnly(root.layoutSymptoms, root.txtSymptoms, "Oops! That should only contain letters.");

        if(!TextHelp.isFilled(root.layoutTreatments, root.txtTreatments, "Please fill out this field.")) return  false;
        TextHelp.addLetterOnly(root.layoutTreatments, root.txtTreatments, "Oops! That should only contain letters.");

        if (!isEditMode && imageUri == null) {
            Snackbar snackbar = Snackbar.make(root.imageUploadFrame, "Please upload an image!", Snackbar.LENGTH_SHORT);
            snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.dark_red));
            snackbar.setTextColor(ContextCompat.getColor(this, R.color.white));
            snackbar.show();
            return false;
        }


        return true;
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
        if (!validateAllField()) return;
        String name = root.txtPestName.getText().toString().trim();

        CustomDialogFragment.newInstance(
                "Add Pest",
                "Are you sure you want to add \"" + name + "\"?",
                "This pest will be added to the system.",
                R.drawable.ic_pest_logo,
                "ADD",
                (dialog, which) -> addPestToFirestore(name)
        ).show(getSupportFragmentManager(), "AddConfirmDialog");
    }

    private void showUpdateConfirmationDialog(String id) {
        if (!validateAllField()) return;
        String name = root.txtPestName.getText().toString().trim();

        CustomDialogFragment.newInstance(
                "Update Pest",
                "Are you sure you want to update \"" + name + "\"?",
                "The changes will be saved immediately.",
                R.drawable.ic_edit,
                "UPDATE",
                (dialog, which) -> updatePest(id)
        ).show(getSupportFragmentManager(), "UpdateConfirmDialog");
    }

    private void addPestToFirestore(String name) {

        String sciName = root.txtScientificName.getText().toString().trim();
        String desc = root.txtDescription.getText().toString().trim();
        String symp = root.txtSymptoms.getText().toString().trim();
        String cause = root.txtCause.getText().toString().trim();
        String treat = root.txtTreatments.getText().toString().trim();


        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading...");
        dialog.setCancelable(false);
        dialog.show();

        FirebaseFirestore.getInstance().collection("pests")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int maxId = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            int currentId = Integer.parseInt(doc.getId());
                            if (currentId > maxId) {
                                maxId = currentId;
                            }
                        } catch (NumberFormatException ignored) {
                            // skip non-numeric IDs
                        }
                    }

                    int newId = maxId + 1;
                    String pestId = String.valueOf(newId);

                    String imageName = UUID.randomUUID().toString();
                    StorageReference imageRef = FirebaseStorage.getInstance().getReference("pest_images/" + imageName);

                    imageRef.putFile(imageUri)
                            .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                Pest pest = new Pest(pestId, name, sciName, desc, symp, cause, treat, uri.toString(), false);

                                FirebaseFirestore.getInstance()
                                        .collection("pests")
                                        .document(pestId)
                                        .set(pest)
                                        .addOnSuccessListener(unused -> {
                                            dialog.dismiss();
                                            showSuccessDialog("added", name);
                                        })
                                        .addOnFailureListener(e -> {
                                            dialog.dismiss();
                                            Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }))
                            .addOnFailureListener(e -> {
                                dialog.dismiss();
                                Toast.makeText(this, "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Failed to fetch Pest IDs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void updatePest(String pestId) {
        if (!validateAllField()) return;

        String name = root.txtPestName.getText().toString().trim();
        String sciName = root.txtScientificName.getText().toString().trim();
        String desc = root.txtDescription.getText().toString().trim();
        String symp = root.txtSymptoms.getText().toString().trim();
        String cause = root.txtCause.getText().toString().trim();
        String treat = root.txtTreatments.getText().toString().trim();


        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Updating...");
        dialog.setCancelable(false);
        dialog.show();

        if (imageUri != null) {
            String imageName = UUID.randomUUID().toString();
            StorageReference imageRef = FirebaseStorage.getInstance().getReference("pest_images/" + imageName);

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Pest pest = new Pest(pestId, name, sciName, desc, symp, cause, treat, uri.toString(), false);
                        pest.archived = false;
                        updateToFirestore(pestId, pest, dialog);
                    }))
                    .addOnFailureListener(e -> {
                        dialog.dismiss();
                        Toast.makeText(this, "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Pest pest = new Pest(pestId, name, sciName, desc, symp, cause, treat, existingImageUrl, false);
            pest.archived = false;
            updateToFirestore(pestId, pest, dialog);
        }
    }

    private void updateToFirestore(String pestId, Pest pest, ProgressDialog dialog) {
        FirebaseFirestore.getInstance()
                .collection("pests")
                .document(pestId)
                .set(pest)
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

    @Override
    public void onBackPressed() {
        if (hasInput()) {
            showDiscardDialog();
        } else {
            super.onBackPressed();
        }
    }

    private boolean hasInput() {
        return !root.txtPestName.getText().toString().trim().isEmpty()
                || !root.txtScientificName.getText().toString().trim().isEmpty()
                || !root.txtDescription.getText().toString().trim().isEmpty()
                || !root.txtSymptoms.getText().toString().trim().isEmpty()
                || !root.txtCause.getText().toString().trim().isEmpty()
                || !root.txtTreatments.getText().toString().trim().isEmpty()
                || imageUri != null;
    }

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
