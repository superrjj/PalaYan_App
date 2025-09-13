package com.example.palayan;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.palayan.API.ApiClient;
import com.example.palayan.API.ApiService;
import com.example.palayan.API.PredictResponse;
import com.example.palayan.databinding.ActivityPredictResultBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PredictResult extends AppCompatActivity {

    private ActivityPredictResultBinding root;
    private Bitmap capturedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityPredictResultBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        // Get image path from intent
        String imagePath = getIntent().getStringExtra("imagePath");
        if (imagePath != null) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                capturedBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                root.ivDiseaseImage.setImageBitmap(capturedBitmap);

                // Call API instead of local prediction
                callPredictionAPI();
            } else {
                Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No image path passed", Toast.LENGTH_SHORT).show();
        }
    }

    private void callPredictionAPI() {
        if (capturedBitmap == null) {
            Toast.makeText(this, "No image to predict", Toast.LENGTH_SHORT).show();
            return;
        }

        // I-add mo yung retry mechanism
        callPredictionAPIWithRetry(0);
    }

    private void callPredictionAPIWithRetry(int retryCount) {
        if (capturedBitmap == null) {
            Toast.makeText(this, "No image to predict", Toast.LENGTH_SHORT).show();
            return;
        }

        final int MAX_RETRIES = 3;

        try {
            // Convert bitmap to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            capturedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            byte[] imageBytes = baos.toByteArray();

            Log.d("PredictResult", "Image size: " + imageBytes.length + " bytes");
            Log.d("PredictResult", "API URL: " + ApiClient.getBaseUrl() + "predict_disease");
            Log.d("PredictResult", "Retry attempt: " + (retryCount + 1));

            // Create multipart body
            okhttp3.RequestBody requestFile = okhttp3.RequestBody.create(
                    okhttp3.MediaType.parse("image/jpeg"), imageBytes);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                    "image", "image.jpg", requestFile);

            // Call API
            ApiService apiService = ApiClient.getApiService();
            Call<PredictResponse> call = apiService.predictDisease(imagePart);

            call.enqueue(new Callback<PredictResponse>() {
                @Override
                public void onResponse(Call<PredictResponse> call, Response<PredictResponse> response) {
                    Log.d("PredictResult", "Response code: " + response.code());

                    if (response.isSuccessful() && response.body() != null) {
                        PredictResponse result = response.body();
                        Log.d("PredictResult", "Success! Predicted: " + result.predicted_disease);
                        displayPredictionResult(result);
                    } else {
                        String errorBody = "";
                        try {
                            errorBody = response.errorBody().string();
                            Log.e("PredictResult", "Error body: " + errorBody);
                        } catch (Exception e) {
                            errorBody = "Could not read error body";
                        }

                        // I-try mo ulit kung may error
                        if (retryCount < MAX_RETRIES) {
                            Log.d("PredictResult", "Retrying... Attempt " + (retryCount + 2));
                            new android.os.Handler().postDelayed(() -> {
                                callPredictionAPIWithRetry(retryCount + 1);
                            }, 2000); // Wait 2 seconds before retry
                        } else {
                            Toast.makeText(PredictResult.this,
                                    "API Error: " + response.code() + " - " + errorBody,
                                    Toast.LENGTH_LONG).show();
                            Log.e("PredictResult", "API Error: " + response.code() + " - " + errorBody);
                        }
                    }
                }

                @Override
                public void onFailure(Call<PredictResponse> call, Throwable t) {
                    String errorMessage = "Network Error: " + t.getMessage();
                    Log.e("PredictResult", "Network Error", t);

                    // I-try mo ulit kung may network error
                    if (retryCount < MAX_RETRIES) {
                        Log.d("PredictResult", "Retrying due to network error... Attempt " + (retryCount + 2));
                        new android.os.Handler().postDelayed(() -> {
                            callPredictionAPIWithRetry(retryCount + 1);
                        }, 2000); // Wait 2 seconds before retry
                    } else {
                        Toast.makeText(PredictResult.this, errorMessage, Toast.LENGTH_LONG).show();

                        // I-add mo to para sa better error diagnosis
                        if (t instanceof java.net.UnknownHostException) {
                            Log.e("PredictResult", "Unknown host - check your URL");
                            Toast.makeText(PredictResult.this, "Cannot connect to server. Check your internet connection.", Toast.LENGTH_LONG).show();
                        } else if (t instanceof java.net.ConnectException) {
                            Log.e("PredictResult", "Connection refused - server might be down");
                            Toast.makeText(PredictResult.this, "Server is not responding. Please try again later.", Toast.LENGTH_LONG).show();
                        } else if (t instanceof javax.net.ssl.SSLException) {
                            Log.e("PredictResult", "SSL error - certificate problem");
                            Toast.makeText(PredictResult.this, "SSL connection error. Please check your network settings.", Toast.LENGTH_LONG).show();
                        } else if (t instanceof java.net.SocketTimeoutException) {
                            Log.e("PredictResult", "Request timeout");
                            Toast.makeText(PredictResult.this, "Request timeout. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error preparing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("PredictResult", "Error preparing image", e);
        }
    }

    private void displayPredictionResult(PredictResponse result) {
        try {
            // Display prediction results
            root.tvDiseaseName.setText(result.predicted_disease);
            root.tvSciName.setText(result.disease_info.scientific_name);

            // Format description (flexible - sentences or bullet points)
            if (result.disease_info.description != null) {
                String formattedDesc = formatText(result.disease_info.description);
                root.tvDiseaseDesc.setText(formattedDesc);
            }

            // Format symptoms - I-update mo to para ma-handle both String at List
            StringBuilder symptomsText = new StringBuilder();
            if (result.disease_info.symptoms != null) {
                if (result.disease_info.symptoms instanceof String) {
                    // If symptoms is a string, format it
                    String symptomsStr = (String) result.disease_info.symptoms;
                    symptomsText.append(formatText(symptomsStr));
                } else if (result.disease_info.symptoms instanceof List) {
                    // If symptoms is a list, format as bullet points
                    List<?> symptomsList = (List<?>) result.disease_info.symptoms;
                    for (Object symptom : symptomsList) {
                        symptomsText.append("• ").append(symptom.toString()).append("\n");
                    }
                }
            }
            root.tvSymptoms.setText(symptomsText.toString());

            // Format treatments - I-update mo din to
            StringBuilder treatmentsText = new StringBuilder();
            if (result.disease_info.treatments != null) {
                if (result.disease_info.treatments instanceof String) {
                    // If treatments is a string, format it
                    String treatmentsStr = (String) result.disease_info.treatments;
                    treatmentsText.append(formatText(treatmentsStr));
                } else if (result.disease_info.treatments instanceof List) {
                    // If treatments is a list, format as bullet points
                    List<?> treatmentsList = (List<?>) result.disease_info.treatments;
                    for (Object treatment : treatmentsList) {
                        treatmentsText.append("• ").append(treatment.toString()).append("\n");
                    }
                }
            }
            root.tvTreatments.setText(treatmentsText.toString());

            // Format cause (flexible - sentences or bullet points)
            if (result.disease_info.cause != null) {
                String formattedCause = formatText(result.disease_info.cause);
                root.tvCause.setText(formattedCause);
            }

            // Show confidence
            String confidenceText = "Confidence: " + String.format("%.1f%%", result.confidence * 100);
            Toast.makeText(this, confidenceText, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error displaying results: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("PredictResult", "Error displaying results", e);
        }
    }

    private String formatText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        // Check if text contains bullet points or list indicators
        if (text.contains("•") || text.contains("-") || text.contains("*") ||
                text.contains("1.") || text.contains("2.") || text.contains("3.")) {
            // Already formatted, return as is
            return text;
        }

        // Check if text has multiple sentences (contains periods)
        if (text.contains(".") && text.split("\\.").length > 2) {
            // Multiple sentences - format as bullet points
            String[] sentences = text.split("\\.");
            StringBuilder formatted = new StringBuilder();

            for (String sentence : sentences) {
                sentence = sentence.trim();
                if (!sentence.isEmpty()) {
                    formatted.append("• ").append(sentence).append(".\n");
                }
            }
            return formatted.toString();
        }

        // Single sentence or short text - return as is
        return text;
    }
}