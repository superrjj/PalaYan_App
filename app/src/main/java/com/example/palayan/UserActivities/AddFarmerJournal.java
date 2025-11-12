package com.example.palayan.UserActivities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.palayan.Helper.JournalStorageHelper;
import com.example.palayan.Helper.RiceFieldProfile;
import com.example.palayan.R;
import com.example.palayan.UserActivities.LoadingDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;

public class AddFarmerJournal extends AppCompatActivity {

    private static final String[] PROVINCES = new String[] { "Tarlac" };
    private static final String[] TARLAC_MUNICIPALITIES = new String[] {
            "Anao", "Bamban", "Camiling", "Capas", "Concepcion", "Gerona", "La Paz",
            "Mayantoc", "Moncada", "Paniqui", "Pura", "Ramos", "San Clemente", "San Jose",
            "San Manuel", "Santa Ignacia", "Tarlac City", "Victoria"
    };

    private TextInputEditText etName, etSize;
    private TextInputLayout layoutName, layoutSize, layoutSoilType, layoutBarangay;
    private AutoCompleteTextView actvSoilType, actProvince, actCity, actBarangay;
    private Button btnSave;
    private ImageView ivBack, ivRiceFieldImage;
    private View ivTapToUpload;
    private Button ivRemoveImage;
    private TextView tvRemove;
    
    private Bitmap selectedBitmap;
    private Uri selectedImageUri;
    private FirebaseStorage storage;
    private LoadingDialog loadingDialog;
    private String deviceId;
    private String[] soilTypes;
    private String riceFieldId; // For editing/deleting existing rice field

    private final ActivityResultLauncher<String> requestCameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) openCamera();
                else showSnackBar("Camera permission denied", false);
            });

    private final ActivityResultLauncher<String> requestReadImagesPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) openGallery();
                else showSnackBar("Photos permission denied", false);
            });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap bmp = (Bitmap) result.getData().getExtras().get("data");
                    if (bmp != null) {
                        selectedBitmap = bmp;
                        selectedImageUri = null;
                        ivRiceFieldImage.setImageBitmap(bmp);
                        ivTapToUpload.setVisibility(View.GONE);
                        ivRemoveImage.setVisibility(View.VISIBLE);
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
                        ivRiceFieldImage.setImageURI(uri);
                        ivTapToUpload.setVisibility(View.GONE);
                        ivRemoveImage.setVisibility(View.VISIBLE);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_farmer_journal);

        storage = FirebaseStorage.getInstance();
        loadingDialog = new LoadingDialog(this);
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        
        // Check if editing existing rice field
        Intent intent = getIntent();
        if (intent != null) {
            riceFieldId = intent.getStringExtra("riceFieldId");
        }

        initViews();
        setupLocationDropdowns();
        setupSoilTypeDropdown();
        setupImagePicker();
        setupListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        etName = findViewById(R.id.etName);
        etSize = findViewById(R.id.etSize);
        actvSoilType = findViewById(R.id.actvSoilType);
        actProvince = findViewById(R.id.actProvince);
        actCity = findViewById(R.id.actCity);
        actBarangay = findViewById(R.id.actBarangay);
        
        layoutName = findViewById(R.id.layoutName);
        layoutSize = findViewById(R.id.layoutSize);
        layoutSoilType = findViewById(R.id.layoutSoilType);
        layoutBarangay = findViewById(R.id.layoutBarangay);
        
        ivRiceFieldImage = findViewById(R.id.ivRiceFieldImage);
        ivTapToUpload = findViewById(R.id.ivTapToUpload);
        ivRemoveImage = findViewById(R.id.ivRemoveImage);
        
        btnSave = findViewById(R.id.btnSave);
        tvRemove = findViewById(R.id.tvRemove);
        
        soilTypes = getResources().getStringArray(R.array.environment_array);
        
        // Show/hide delete button based on whether editing or adding new
        if (riceFieldId != null && !riceFieldId.isEmpty()) {
            tvRemove.setVisibility(View.VISIBLE);
        } else {
            tvRemove.setVisibility(View.GONE);
        }
    }

    private void setupLocationDropdowns() {
        // Province adapter (Tarlac only)
        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, PROVINCES);
        actProvince.setAdapter(provinceAdapter);
        actProvince.setOnItemClickListener((parent, v, position, id) -> {
            actCity.setText("");
            actBarangay.setText("");
            setupMunicipalities();
        });

        // Initialize municipalities
        setupMunicipalities();
    }

    private void setupMunicipalities() {
        ArrayAdapter<String> municipalitiesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, TARLAC_MUNICIPALITIES);
        actCity.setAdapter(municipalitiesAdapter);
        actCity.setOnItemClickListener((parent, v, position, id) -> {
            String selectedMunicipality = TARLAC_MUNICIPALITIES[position];
            actBarangay.setText("");
            setupBarangays(selectedMunicipality);
        });
    }

    private void setupBarangays(String municipality) {
        String[] barangays;
        switch (municipality) {
            case "Tarlac City":
                barangays = getResources().getStringArray(R.array.tarlac_city_barangays);
                break;
            case "Gerona":
                barangays = getResources().getStringArray(R.array.gerona_barangays);
                break;
            case "Concepcion":
                barangays = getResources().getStringArray(R.array.concepcion_barangays);
                break;
            case "La Paz":
                barangays = getResources().getStringArray(R.array.la_paz_barangays);
                break;
            case "San Jose":
                barangays = getResources().getStringArray(R.array.san_jose_barangays);
                break;
            case "Victoria":
                barangays = getResources().getStringArray(R.array.victoria_barangays);
                break;
            case "Santa Ignacia":
                barangays = getResources().getStringArray(R.array.santa_ignacia_barangays);
                break;
            case "Mayantoc":
                barangays = getResources().getStringArray(R.array.mayantoc_barangays);
                break;
            case "Camiling":
                barangays = getResources().getStringArray(R.array.camiling_barangays);
                break;
            case "San Clemente":
                barangays = getResources().getStringArray(R.array.san_clemente_barangays);
                break;
            case "San Manuel":
                barangays = getResources().getStringArray(R.array.san_manuel_barangays);
                break;
            case "Moncada":
                barangays = getResources().getStringArray(R.array.moncada_barangays);
                break;
            case "Paniqui":
                barangays = getResources().getStringArray(R.array.paniqui_barangays);
                break;
            case "Pura":
                barangays = getResources().getStringArray(R.array.pura_barangays);
                break;
            case "Anao":
                barangays = getResources().getStringArray(R.array.anao_barangays);
                break;
            case "Ramos":
                barangays = getResources().getStringArray(R.array.ramos_barangays);
                break;
            case "Capas":
                barangays = getResources().getStringArray(R.array.capas_barangays);
                break;
            case "Bamban":
                barangays = getResources().getStringArray(R.array.bamban_barangays);
                break;
            default:
                barangays = new String[] { };
        }

        ArrayAdapter<String> barangayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, barangays);
        actBarangay.setAdapter(barangayAdapter);
    }

    private void setupSoilTypeDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_list_item_1, soilTypes);
        actvSoilType.setAdapter(adapter);
    }

    private void setupImagePicker() {
        ivRiceFieldImage.setOnClickListener(v -> showImagePickerDialog());
        ivTapToUpload.setOnClickListener(v -> showImagePickerDialog());
        ivRemoveImage.setOnClickListener(v -> clearSelectedImage());
    }

    private void showImagePickerDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirmation, null);
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvDialogMessage = dialogView.findViewById(R.id.tvDialogMessage);
        MaterialButton btnDialogCancel = dialogView.findViewById(R.id.btnDialogCancel);
        MaterialButton btnDialogConfirm = dialogView.findViewById(R.id.btnDialogConfirm);

        tvDialogTitle.setText("Pumili ng Larawan");
        tvDialogMessage.setText("Pumili kung gagamit ng camera o gallery para sa larawan.");
        btnDialogCancel.setText("Camera");
        btnDialogConfirm.setText("Gallery");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnDialogCancel.setOnClickListener(v -> {
            dialog.dismiss();
            ensureCameraAndOpen();
        });
        btnDialogConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            ensureGalleryAndOpen();
        });

        dialog.show();
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

    private void clearSelectedImage() {
        selectedBitmap = null;
        selectedImageUri = null;
        ivRiceFieldImage.setImageBitmap(null);
        ivRiceFieldImage.setImageURI(null);
        ivRiceFieldImage.setBackgroundColor(Color.parseColor("#F5F5F5"));
        ivTapToUpload.setVisibility(View.VISIBLE);
        ivRemoveImage.setVisibility(View.GONE);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveRiceField());
        
        tvRemove.setOnClickListener(v -> {
            if (riceFieldId != null && !riceFieldId.isEmpty()) {
                showDeleteConfirmationDialog();
            }
        });
    }
    
    private void showDeleteConfirmationDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirmation, null);
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvDialogMessage = dialogView.findViewById(R.id.tvDialogMessage);
        MaterialButton btnDialogCancel = dialogView.findViewById(R.id.btnDialogCancel);
        MaterialButton btnDialogConfirm = dialogView.findViewById(R.id.btnDialogConfirm);

        tvDialogTitle.setText("Tanggalin ang Palayan");
        tvDialogMessage.setText("Sigurado ka bang gusto mong tanggalin ang palayan na ito?");
        btnDialogConfirm.setText("Tanggalin");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnDialogCancel.setOnClickListener(v -> dialog.dismiss());
        btnDialogConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            deleteRiceField();
        });

        dialog.show();
    }
    
    private void deleteRiceField() {
        loadingDialog.show("Ini-tanggal ang palayan...");
        JournalStorageHelper.deleteRiceField(this, riceFieldId, new JournalStorageHelper.OnDeleteListener() {
            @Override
            public void onSuccess() {
                loadingDialog.dismiss();
                showSnackBar("Matagumpay na natanggal ang palayan!", true, AddFarmerJournal.this::finish);
            }

            @Override
            public void onFailure(String error) {
                loadingDialog.dismiss();
                showSnackBar("Hindi natanggal: " + error, false, null);
            }
        });
    }

    private void saveRiceField() {
        // Clear previous errors
        layoutName.setError(null);
        layoutSize.setError(null);
        layoutSoilType.setError(null);
        layoutBarangay.setError(null);

        // Get values
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String sizeStr = etSize.getText() != null ? etSize.getText().toString().trim() : "";
        String soilType = actvSoilType.getText() != null ? actvSoilType.getText().toString().trim() : "";
        String province = actProvince.getText() != null ? actProvince.getText().toString().trim() : "";
        String city = actCity.getText() != null ? actCity.getText().toString().trim() : "";
        String barangay = actBarangay.getText() != null ? actBarangay.getText().toString().trim() : "";

        // Validate
        boolean isValid = true;

        if (name.isEmpty()) {
            layoutName.setError("Kailangan ang pangalan ng palayan");
            isValid = false;
        }

        if (province.isEmpty()) {
            showSnackBar("Kailangan ang probinsya", false, null);
            isValid = false;
        }

        if (city.isEmpty()) {
            showSnackBar("Kailangan ang lungsod/munisipalidad", false, null);
            isValid = false;
        }

        if (barangay.isEmpty()) {
            layoutBarangay.setError("Kailangan ang barangay");
            isValid = false;
        }

        if (sizeStr.isEmpty()) {
            layoutSize.setError("Kailangan ang sukat ng palayan");
            isValid = false;
        } else {
            try {
                double size = Double.parseDouble(sizeStr);
                if (size <= 0) {
                    layoutSize.setError("Ang sukat ay dapat mas malaki sa 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                layoutSize.setError("Hindi wasto ang sukat");
                isValid = false;
            }
        }

        if (soilType.isEmpty()) {
            layoutSoilType.setError("Kailangan ang klase ng lupa");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Check if image is selected, if not, save directly without uploading
        if (selectedBitmap == null && selectedImageUri == null) {
            // No image selected, save directly with empty imageUrl
            loadingDialog.show("Ini-save ang palayan...");
            saveRiceFieldToFirestore(name, "", province, city, barangay, Double.parseDouble(sizeStr), soilType);
        } else {
            // Upload image first, then save
            loadingDialog.show("Ini-upload ang larawan...");
            uploadImageThenSave(name, province, city, barangay, Double.parseDouble(sizeStr), soilType);
        }
    }

    private void uploadImageThenSave(String name, String province, String city, String barangay, double size, String soilType) {
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

            String filename = "rice_field_" + deviceId + "_" + UUID.randomUUID() + ".jpg";
            String path = "rice_fields/" + deviceId + "/" + filename;

            StorageReference ref = storage.getReference().child(path);
            UploadTask task = ref.putBytes(bytes);
            task.addOnSuccessListener(snap ->
                    ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        loadingDialog.setMessage("Ini-save ang palayan...");
                        saveRiceFieldToFirestore(name, imageUrl, province, city, barangay, size, soilType);
                    }).addOnFailureListener(e -> {
                        loadingDialog.dismiss();
                        showSnackBar("Hindi ma-upload ang larawan: " + e.getMessage(), false);
                    })
            ).addOnFailureListener(e -> {
                loadingDialog.dismiss();
                showSnackBar("Hindi ma-upload ang larawan: " + e.getMessage(), false);
            });

        } catch (Exception e) {
            loadingDialog.dismiss();
            showSnackBar("Error sa larawan: " + e.getMessage(), false);
        }
    }

    private void saveRiceFieldToFirestore(String name, String imageUrl, String province, String city, String barangay, double size, String soilType) {
        RiceFieldProfile riceField = new RiceFieldProfile(name, imageUrl, province, city, barangay, size, soilType);
        
        JournalStorageHelper.saveRiceField(this, riceField, new JournalStorageHelper.OnSaveListener() {
            @Override
            public void onSuccess() {
                loadingDialog.dismiss();
                // Show success message
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), 
                    "Matagumpay na na-save ang palayan!", Snackbar.LENGTH_SHORT);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(ContextCompat.getColor(AddFarmerJournal.this, R.color.green));
                android.widget.TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                textView.setTypeface(ResourcesCompat.getFont(AddFarmerJournal.this, R.font.poppins__regular));
                textView.setTextColor(ContextCompat.getColor(AddFarmerJournal.this, R.color.white));
                
                snackbar.addCallback(new com.google.android.material.snackbar.Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        Intent intent = new Intent(AddFarmerJournal.this, FarmerJournal.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                });
                
                snackbar.show();
            }

            @Override
            public void onFailure(String error) {
                loadingDialog.dismiss();
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), 
                    "Hindi na-save: " + error, Snackbar.LENGTH_LONG);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(ContextCompat.getColor(AddFarmerJournal.this, R.color.dark_red));
                android.widget.TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                textView.setTypeface(ResourcesCompat.getFont(AddFarmerJournal.this, R.font.poppins__regular));
                textView.setTextColor(ContextCompat.getColor(AddFarmerJournal.this, R.color.white));
                snackbar.show();
            }
        });
    }

    private void showSnackBar(String message, boolean isSuccess) {
        showSnackBar(message, isSuccess, null);
    }

    private void showSnackBar(String message, boolean isSuccess, @Nullable Runnable onDismiss) {
        View root = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(root, message, Snackbar.LENGTH_LONG);
        int backgroundColor = ContextCompat.getColor(this, isSuccess ? R.color.green : R.color.dark_red);
        snackbar.setBackgroundTint(backgroundColor);
        snackbar.setTextColor(ContextCompat.getColor(this, android.R.color.white));

        android.widget.TextView textView = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        if (textView != null) {
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTypeface(ResourcesCompat.getFont(this, R.font.poppins__regular));
        }

        if (onDismiss != null) {
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    onDismiss.run();
                }
            });
        }

        snackbar.show();
    }
}
