    package com.example.palayan;

    import android.os.Bundle;
    import android.widget.Toast;

    import androidx.activity.EdgeToEdge;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.graphics.Insets;
    import androidx.core.view.ViewCompat;
    import androidx.core.view.WindowCompat;
    import androidx.core.view.WindowInsetsCompat;

    import com.bumptech.glide.Glide;
    import com.example.palayan.Helper.Pest;
    import com.example.palayan.databinding.ActivityPestDetailsBinding;
    import com.google.firebase.firestore.DocumentReference;
    import com.google.firebase.firestore.FirebaseFirestore;

    public class PestDetails extends AppCompatActivity {

        private ActivityPestDetailsBinding root;
        private String pestId;
        private FirebaseFirestore firestore;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            root = ActivityPestDetailsBinding.inflate(getLayoutInflater());
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
            setContentView(root.getRoot());

            firestore = FirebaseFirestore.getInstance();

            pestId = getIntent().getStringExtra("pest_id");
            if (pestId != null) {
                loadPestDetails(pestId);
            }

            root.ivBack.setOnClickListener(view -> onBackPressed());

        }

        private void loadPestDetails(String pestId) {
            DocumentReference docRef = firestore.collection("pests").document(pestId);

            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Pest pest = documentSnapshot.toObject(Pest.class);

                    if (pest != null) {
                        // Set texts
                        root.tvPestName.setText(pest.getPestName());
                        root.tvSciName.setText(pest.getScientificName());
                        root.tvDescription.setText(pest.getDescription());
                        root.tvSymtomps.setText(formatAsBulletList(pest.getSymptoms()));
                        root.tvCause.setText(formatAsBulletList(pest.getCause()));
                        root.tvTreatments.setText(formatAsBulletList(pest.getTreatments()));

                        // Load image
                        Glide.with(this)
                                .load(pest.getImageUrl())
                                .placeholder(R.drawable.loading_image)
                                .into(root.ivPestImage);
                    }

                } else {
                    Toast.makeText(this, "Pest not found.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to load data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }

        private String formatAsBulletList(String rawText) {
            if (rawText == null || rawText.trim().isEmpty()) {
                return "• No data available.";
            }

            String[] items = rawText.split("\\.");
            StringBuilder result = new StringBuilder();
            for (String item : items) {
                item = item.trim();
                if (!item.isEmpty()) {
                    result.append("• ").append(item).append("\n");
                }
            }
            return result.toString();
        }
    }