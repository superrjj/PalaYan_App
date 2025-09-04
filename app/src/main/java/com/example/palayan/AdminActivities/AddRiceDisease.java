package com.example.palayan.AdminActivities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.palayan.Adapter.ImageUploadAdapter;
import com.example.palayan.Dialog.AllDiseaseRiceImagesSheet;
import com.example.palayan.Dialog.CustomDialogFragment;
import com.example.palayan.Helper.ImageUploadItem;
import com.example.palayan.R;
import com.example.palayan.databinding.ActivityAddRiceDiseaseBinding;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddRiceDisease extends AppCompatActivity {
    private ActivityAddRiceDiseaseBinding root;
    private ImageUploadAdapter adapter;
    private List<ImageUploadItem> imageList = new ArrayList<>();
    private FirebaseFirestore firestore;
    private StorageReference storageRef;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private Uri cameraImageUri;
    private AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityAddRiceDiseaseBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        firestore = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("rice_disease");

        setupRecyclerView();
        setupImagePickers();

        root.tvViewAll.setOnClickListener(v -> {
            ArrayList<Uri> uris = new ArrayList<>();
            for (ImageUploadItem item : imageList) {
                if (!item.isPlaceholder() && item.getImageUrl() != null) {
                    uris.add(Uri.parse(item.getImageUrl()));
                }
            }
            AllDiseaseRiceImagesSheet bottomSheet = new AllDiseaseRiceImagesSheet(uris);
            bottomSheet.show(getSupportFragmentManager(), "AllDiseaseRiceImagesSheet");
        });

        root.btnAddPest.setOnClickListener(v -> saveRiceDisease());
        root.ivBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new ImageUploadAdapter(imageList, uri -> showImageSourceDialog());
        root.rvImageRiceDiseases.setLayoutManager(new GridLayoutManager(this, 3));
        root.rvImageRiceDiseases.setAdapter(adapter);
        imageList.add(new ImageUploadItem(null, true)); // placeholder
        adapter.notifyDataSetChanged();
    }

    private void setupImagePickers() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        if (result.getData().getClipData() != null) {
                            int count = result.getData().getClipData().getItemCount();
                            for (int i = 0; i < count; i++) {
                                Uri selectedImage = result.getData().getClipData().getItemAt(i).getUri();
                                addImage(selectedImage);
                            }
                        } else if (result.getData().getData() != null) {
                            Uri selectedImage = result.getData().getData();
                            addImage(selectedImage);
                        }
                    }
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        addImage(cameraImageUri);
                    }
                });
    }

    private void showImageSourceDialog() {
        String[] options = {"Camera", "Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Choose Image Source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        File photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                                UUID.randomUUID().toString() + ".jpg");
                        cameraImageUri = FileProvider.getUriForFile(this,
                                getPackageName() + ".provider", photoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        cameraLauncher.launch(intent);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        galleryLauncher.launch(Intent.createChooser(intent, "Select Pictures"));
                    }
                }).show();
    }

    private void addImage(Uri imageUri) {
        if (!imageList.isEmpty() && imageList.get(imageList.size() - 1).isPlaceholder()) {
            imageList.remove(imageList.size() - 1);
        }
        imageList.add(new ImageUploadItem(imageUri.toString(), false));
        if (imageList.size() < 7) {
            imageList.add(new ImageUploadItem(null, true));
        }
        adapter.notifyDataSetChanged();
        updateViewAll();
    }

    private void updateViewAll() {
        int realCount = getRealImageCount();
        if (realCount > 6) {
            root.tvViewAll.setVisibility(View.VISIBLE);
            root.tvViewAll.setText("View All (" + realCount + ")");
        } else {
            root.tvViewAll.setVisibility(View.GONE);
        }
    }

    private int getRealImageCount() {
        int count = 0;
        for (ImageUploadItem item : imageList) {
            if (!item.isPlaceholder()) count++;
        }
        return count;
    }

    // Main save method - now shows confirmation dialog
    private void saveRiceDisease() {
        showAddConfirmationDialog();
    }

    // Show confirmation dialog before saving
    private void showAddConfirmationDialog() {
        if (!validateAllFields()) return;

        String diseaseName = root.txtDiseaseName.getText().toString().trim();
        CustomDialogFragment.newInstance(
                "Add Rice Disease",
                "Are you sure you want to add \"" + diseaseName + "\"?",
                "This rice disease will be added to the system and trigger model retraining.",
                R.drawable.ic_disease_logo,
                "ADD",
                (dialog, which) -> performSaveRiceDisease()
        ).show(getSupportFragmentManager(), "AddDiseaseConfirmDialog");
    }

    // Validation method
    private boolean validateAllFields() {
        String diseaseName = root.txtDiseaseName.getText().toString().trim();
        String scientificName = root.txtScientificName.getText().toString().trim();
        String description = root.txtDescription.getText().toString().trim();
        String symptoms = root.txtSymptoms.getText().toString().trim();
        String cause = root.txtCause.getText().toString().trim();
        String treatments = root.txtTreatments.getText().toString().trim();

        if (diseaseName.isEmpty() || scientificName.isEmpty() || description.isEmpty() ||
                symptoms.isEmpty() || cause.isEmpty() || treatments.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        int totalImagesToUpload = 0;
        for (ImageUploadItem item : imageList) {
            if (!item.isPlaceholder() && item.getImageUrl() != null) {
                totalImagesToUpload++;
            }
        }

        if (totalImagesToUpload == 0) {
            Toast.makeText(this, "Please upload at least 1 image", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    //Actual save implementation
    private void performSaveRiceDisease() {
        String diseaseName = root.txtDiseaseName.getText().toString().trim();
        String scientificName = root.txtScientificName.getText().toString().trim();
        String description = root.txtDescription.getText().toString().trim();
        String symptoms = root.txtSymptoms.getText().toString().trim();
        String cause = root.txtCause.getText().toString().trim();
        String treatments = root.txtTreatments.getText().toString().trim();

        List<String> imageUrls = new ArrayList<>();
        int totalImagesToUpload = 0;
        for (ImageUploadItem item : imageList) {
            if (!item.isPlaceholder() && item.getImageUrl() != null) {
                totalImagesToUpload++;
            }
        }

        final int finalTotal = totalImagesToUpload;
        final int[] uploadedCount = {0};

        //Show progress dialog
        showProgressDialog("Uploading images...");

        for (ImageUploadItem item : imageList) {
            if (!item.isPlaceholder() && item.getImageUrl() != null) {
                Uri fileUri = Uri.parse(item.getImageUrl());
                StorageReference fileRef = storageRef.child(diseaseName.replaceAll("\\s+", "_"))
                        .child(UUID.randomUUID().toString() + ".jpg");

                fileRef.putFile(fileUri)
                        .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    imageUrls.add(uri.toString());
                                    uploadedCount[0]++;
                                    if (uploadedCount[0] == finalTotal) {
                                        saveToFirestore(diseaseName, scientificName, description,
                                                symptoms, cause, treatments, imageUrls);
                                    }
                                }))
                        .addOnFailureListener(e -> {
                            uploadedCount[0]++;
                            Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            if (uploadedCount[0] == finalTotal) {
                                hideProgressDialog();
                                if (!imageUrls.isEmpty()) {
                                    saveToFirestore(diseaseName, scientificName, description,
                                            symptoms, cause, treatments, imageUrls);
                                } else {
                                    Toast.makeText(this, "No images uploaded successfully!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    }

    // Progress dialog methods
    private void showProgressDialog(String message) {
        if (progressDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(message);
            builder.setCancelable(false);
            progressDialog = builder.create();
        } else {
            progressDialog.setMessage(message);
        }
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void saveToFirestore(String diseaseName, String scientificName, String description,
                                 String symptoms, String cause, String treatments, List<String> imageUrls) {
        String docId = diseaseName.replaceAll("\\s+", "_");
        DocumentReference docRef = firestore.collection("rice_local_disease").document(docId);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                hideProgressDialog();
                Toast.makeText(this, "Disease already exists! Choose another name.", Toast.LENGTH_LONG).show();
            } else {
                docRef.set(new RiceDiseaseModel(diseaseName, scientificName, description,
                                symptoms, cause, treatments, imageUrls))
                        .addOnSuccessListener(unused -> {
                            hideProgressDialog();
                            Toast.makeText(this, "Rice disease added successfully! Training will start automatically.", Toast.LENGTH_LONG).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            hideProgressDialog();
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        });
            }
        }).addOnFailureListener(e -> {
            hideProgressDialog();
            Toast.makeText(this, "Error checking disease: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideProgressDialog();
    }

    // Rice Disease Model Class
    public static class RiceDiseaseModel {
        private String diseaseName;
        private String scientificName;
        private String description, symptoms, cause, treatments;
        private List<String> images;

        public RiceDiseaseModel() {}

        public RiceDiseaseModel(String diseaseName, String scientificName, String description,
                                String symptoms, String cause, String treatments, List<String> images) {
            this.diseaseName = diseaseName;
            this.scientificName = scientificName;
            this.description = description;
            this.symptoms = symptoms;
            this.cause = cause;
            this.treatments = treatments;
            this.images = images;
        }

        // All getters
        public String getDiseaseName() { return diseaseName; }
        public String getScientificName() { return scientificName; }
        public String getDescription() { return description; }
        public String getSymptoms() { return symptoms; }
        public String getCause() { return cause; }
        public String getTreatments() { return treatments; }
        public List<String> getImages() { return images; }

        // All setters
        public void setDiseaseName(String diseaseName) { this.diseaseName = diseaseName; }
        public void setScientificName(String scientificName) { this.scientificName = scientificName; }
        public void setDescription(String description) { this.description = description; }
        public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
        public void setCause(String cause) { this.cause = cause; }
        public void setTreatments(String treatments) { this.treatments = treatments; }
        public void setImages(List<String> images) { this.images = images; }
    }
}