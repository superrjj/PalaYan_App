package com.example.palayan.UserActivities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.palayan.UserActivities.LoadingDialog;
import com.example.palayan.databinding.ActivityTreatmentNotesBinding;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.Map;

public class TreatmentNotes extends AppCompatActivity {

    private ActivityTreatmentNotesBinding root;

    private String diseaseName;
    private String deviceId;
    private String imagePathFromPredict; // optional preview only

    private Bitmap selectedBitmap;       // from Camera thumbnail
    private Uri selectedImageUri;        // from Gallery

    private LoadingDialog loadingDialog;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    private final ActivityResultLauncher<String> requestCameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) openCamera();
                else Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            });

    private final ActivityResultLauncher<String> requestReadImagesPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) openGallery();
                else Toast.makeText(this, "Photos permission denied", Toast.LENGTH_SHORT).show();
            });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap bmp = (Bitmap) result.getData().getExtras().get("data");
                    if (bmp != null) {
                        selectedBitmap = bmp;
                        selectedImageUri = null;
                        root.ivImageContainer.setPadding(0, 0, 0, 0);
                        root.ivImageContainer.setImageBitmap(bmp);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        selectedImageUri = uri;
                        selectedBitmap = null;
                        root.ivImageContainer.setPadding(0, 0, 0, 0);
                        root.ivImageContainer.setImageURI(uri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityTreatmentNotesBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        loadingDialog = new LoadingDialog(this);
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        Intent i = getIntent();
        diseaseName = i.getStringExtra("diseaseName");
        deviceId = i.getStringExtra("deviceId");
        imagePathFromPredict = i.getStringExtra("imagePath"); // optional

        root.ivBack.setOnClickListener(v -> onBackPressed());

        // Prefill disease and date
        root.tvDiseaseDetected.setText(TextUtils.isEmpty(diseaseName) ? "Unknown" : diseaseName);
        String today = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(new Date());
        root.tvDateApplied.setText(today);

        // Optional preview from previous image path (no upload yet)
        previewFromPredictPath();

        // Pick image (Camera or Gallery)
        root.cvUploadPhoto.setOnClickListener(v -> showImagePickerDialog());

        // Save note
        root.btnSaveNote.setOnClickListener(v -> onSaveNote());
    }

    private void showImagePickerDialog() {
        String[] options = new String[]{"Camera", "Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Select Photo")
                .setItems(options, (d, which) -> {
                    if (which == 0) ensureCameraAndOpen();
                    else ensureGalleryAndOpen();
                })
                .show();
    }

    private void ensureCameraAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission.launch(Manifest.permission.CAMERA);
        } else {
            openCamera();
        }
    }

    private void ensureGalleryAndOpen() {
        String readPerm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, readPerm)
                != PackageManager.PERMISSION_GRANTED) {
            requestReadImagesPermission.launch(readPerm);
        } else {
            openGallery();
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private void openGallery() {
        Intent pick = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pick.setType("image/*");
        galleryLauncher.launch(pick);
    }

    private void onSaveNote() {
        String description = root.txtDescription.getText() == null ? "" : root.txtDescription.getText().toString().trim();
        if (selectedBitmap == null && selectedImageUri == null) {
            Toast.makeText(this, "Please select a photo (camera or gallery).", Toast.LENGTH_SHORT).show();
            return;
        }
        if (description.isEmpty()) {
            Toast.makeText(this, "Please enter treatment applied.", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingDialog.show("Uploading photo...");
        uploadPhotoThenSaveNote(description);
    }

    private void uploadPhotoThenSaveNote(String description) {
        try {
            byte[] bytes;

            if (selectedBitmap != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                bytes = baos.toByteArray();
            } else {
                try (InputStream is = getContentResolver().openInputStream(selectedImageUri)) {
                    if (is == null) throw new IllegalStateException("Cannot open selected image.");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, read);
                    }
                    bytes = baos.toByteArray();
                }
            }

            String filename = "treatment_" + (deviceId == null ? "unknown" : deviceId) + "_" + UUID.randomUUID() + ".jpg";
            String path = "treatment_notes/" + (deviceId == null ? "unknown" : deviceId) + "/" + filename;

            StorageReference ref = storage.getReference().child(path);
            UploadTask task = ref.putBytes(bytes);
            task.addOnSuccessListener(snap ->
                    ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        String photoUrl = uri.toString();
                        loadingDialog.setMessage("Saving note...");
                        saveNoteToFirestore(description, photoUrl);
                    }).addOnFailureListener(e -> {
                        loadingDialog.dismiss();
                        Toast.makeText(this, "Failed to get photo URL: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    })
            ).addOnFailureListener(e -> {
                loadingDialog.dismiss();
                Toast.makeText(this, "Photo upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });

        } catch (Exception e) {
            loadingDialog.dismiss();
            Toast.makeText(this, "Error preparing photo: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveNoteToFirestore(String description, String photoUrl) {
        String docId = (deviceId == null ? "unknown" : deviceId) + "_" + UUID.randomUUID();

        // Friendly date string for display
        String dateApplied = root.tvDateApplied.getText() == null
                ? ""
                : root.tvDateApplied.getText().toString();

        Map<String, Object> data = new java.util.HashMap<>();
        data.put("deviceId", deviceId);
        data.put("diseaseName", diseaseName);
        data.put("dateApplied", dateApplied);
        data.put("description", description);
        data.put("photoUrl", photoUrl);
        data.put("timestamp", FieldValue.serverTimestamp());
        data.put("documentId", docId);

        firestore.collection("users")
                .document(deviceId == null ? "unknown" : deviceId)
                .collection("treatment_notes")
                .document(docId)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Treatment note saved.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Failed to save note: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void previewFromPredictPath() {
        try {
            if (!TextUtils.isEmpty(imagePathFromPredict)) {
                Bitmap bmp = BitmapFactory.decodeFile(imagePathFromPredict);
                if (bmp != null) {
                    root.ivImageContainer.setPadding(0, 0, 0, 0);
                    root.ivImageContainer.setImageBitmap(bmp);
                }
            }
        } catch (Exception ignored) {}
    }
}