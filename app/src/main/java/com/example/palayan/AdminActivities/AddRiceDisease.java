package com.example.palayan.AdminActivities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.palayan.Adapter.ImageUploadAdapter;
import com.example.palayan.Helper.ImageUploadItem;
import com.example.palayan.databinding.ActivityAddRiceDiseaseBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    private Uri cameraImageUri;


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
            Toast.makeText(this, "View all images clicked", Toast.LENGTH_SHORT).show();
            // TODO: pwede mo i-launch ibang activity para makita lahat ng images
        });

        root.btnAddPest.setOnClickListener(v -> saveRiceDisease());
        root.ivBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new ImageUploadAdapter(imageList, uri -> {
            // when tap upload frame → choose camera or gallery
            showImageSourceDialog();
        });

        root.rvImageRiceDiseases.setLayoutManager(new GridLayoutManager(this, 3));
        root.rvImageRiceDiseases.setAdapter(adapter);

        // add initial empty slot
        imageList.add(new ImageUploadItem(null, true));
        adapter.notifyDataSetChanged();
    }

    private void setupImagePickers() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        if (result.getData().getClipData() != null) {
                            // Multiple images
                            int count = result.getData().getClipData().getItemCount();
                            for (int i = 0; i < count; i++) {
                                Uri selectedImage = result.getData().getClipData().getItemAt(i).getUri();
                                addImage(selectedImage);
                            }
                        } else if (result.getData().getData() != null) {
                            // Single image
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
                    if (which == 0) { // Camera
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                        File photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                                UUID.randomUUID().toString() + ".jpg");
                        cameraImageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);

                        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        cameraLauncher.launch(intent);
                    } else { // Gallery
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        galleryLauncher.launch(Intent.createChooser(intent, "Select Pictures"));
                    }

                }).show();
    }

    private void addImage(Uri imageUri) {
        // remove old placeholder
        if (!imageList.isEmpty() && imageList.get(imageList.size() - 1).isPlaceholder()) {
            imageList.remove(imageList.size() - 1);
        }

        imageList.add(new ImageUploadItem(imageUri.toString(), false));

        // add back placeholder if less than 6
        if (imageList.size() < 6) {
            imageList.add(new ImageUploadItem(null, true));
        }

        adapter.notifyDataSetChanged();

        // show "View All" if more than 6
        if (imageList.size() > 6) {
            root.tvViewAll.setVisibility(View.VISIBLE);
            root.tvViewAll.setText("View All (" + imageList.size() + ")");
        } else {
            root.tvViewAll.setVisibility(View.GONE);
        }
    }

    private void saveRiceDisease() {
        String description = root.txtDescription.getText().toString().trim();
        String symptoms = root.txtSymptoms.getText().toString().trim();
        String cause = root.txtCause.getText().toString().trim();
        String treatments = root.txtTreatments.getText().toString().trim();

        if (description.isEmpty() || symptoms.isEmpty() || cause.isEmpty() || treatments.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> imageUrls = new ArrayList<>();

        // Upload each image to Firebase Storage
        for (ImageUploadItem item : imageList) {
            if (!item.isPlaceholder() && item.getImageUrl() != null) {
                Uri fileUri = Uri.parse(item.getImageUrl());
                StorageReference fileRef = storageRef.child(UUID.randomUUID().toString());

                fileRef.putFile(fileUri)
                        .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    imageUrls.add(uri.toString());

                                    // kapag lahat uploaded → save sa Firestore
                                    if (imageUrls.size() == imageList.size() - 1) {
                                        saveToFirestore(description, symptoms, cause, treatments, imageUrls);
                                    }
                                }))
                        .addOnFailureListener(e -> Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }
    }

    private void saveToFirestore(String description, String symptoms, String cause, String treatments, List<String> imageUrls) {
        DocumentReference docRef = firestore.collection("rice_disease").document();

        docRef.set(new RiceDiseaseModel(description, symptoms, cause, treatments, imageUrls))
                .addOnSuccessListener(unused -> Toast.makeText(this, "Rice disease added", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // simple POJO model for Firestore
    public static class RiceDiseaseModel {
        String description, symptoms, cause, treatments;
        List<String> images;

        public RiceDiseaseModel() {}

        public RiceDiseaseModel(String description, String symptoms, String cause, String treatments, List<String> images) {
            this.description = description;
            this.symptoms = symptoms;
            this.cause = cause;
            this.treatments = treatments;
            this.images = images;
        }
    }
}
