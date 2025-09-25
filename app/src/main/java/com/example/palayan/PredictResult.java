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
import com.example.palayan.UserActivities.LoadingDialog;

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
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityPredictResultBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        loadingDialog = new LoadingDialog(this);

        String imagePath = getIntent().getStringExtra("imagePath");
        if (imagePath != null) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                capturedBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                root.ivDiseaseImage.setImageBitmap(capturedBitmap);
                callPredictionAPI();
            } else {
                Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No image path passed", Toast.LENGTH_SHORT).show();
        }

        root.btnBack.setOnClickListener(view -> onBackPressed());

    }

    private void callPredictionAPI() {
        if (capturedBitmap == null) {
            Toast.makeText(this, "No image to predict", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isFinishing()) loadingDialog.show("Loading result...");
        callPredictionAPIWithRetry(0);
    }

    private void callPredictionAPIWithRetry(int retryCount) {
        if (capturedBitmap == null) {
            Toast.makeText(this, "No image to predict", Toast.LENGTH_SHORT).show();
            return;
        }

        final int MAX_RETRIES = 100;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            capturedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            byte[] imageBytes = baos.toByteArray();


            if (loadingDialog != null) {
                String msg = retryCount == 0
                        ? "Loading result..."
                        : "Loading result... Attempt " + (retryCount + 1) + "/100";
                loadingDialog.setMessage(msg);
            }

            okhttp3.RequestBody requestFile = okhttp3.RequestBody.create(
                    okhttp3.MediaType.parse("image/jpeg"), imageBytes);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                    "image", "image.jpg", requestFile);

            ApiService apiService = ApiClient.getApiService();
            Call<PredictResponse> call = apiService.predictDisease(imagePart);

            call.enqueue(new Callback<PredictResponse>() {
                @Override
                public void onResponse(Call<PredictResponse> call, Response<PredictResponse> response) {

                    if (response.isSuccessful() && response.body() != null) {
                        if (loadingDialog != null) loadingDialog.dismiss();
                        PredictResponse result = response.body();
                        displayPredictionResult(result);
                    } else {
                        String errorBody = "";
                        try {
                            errorBody = response.errorBody() != null ? response.errorBody().string() : "";
                        } catch (Exception e) {
                            errorBody = "Could not read error body";
                        }

                        if (retryCount < MAX_RETRIES) {
                            new android.os.Handler().postDelayed(() -> {
                                callPredictionAPIWithRetry(retryCount + 1);
                            }, 2000);
                        } else {
                            if (loadingDialog != null) loadingDialog.dismiss();
                            Toast.makeText(PredictResult.this,
                                    "API Error: " + response.code() + " - " + errorBody,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<PredictResponse> call, Throwable t) {
                    String errorMessage = "Network Error: " + t.getMessage();

                    if (retryCount < MAX_RETRIES) {
                        if (loadingDialog != null) {
                            String msg = "Network error, retrying... Attempt " + (retryCount + 2) + "/3";
                            loadingDialog.setMessage(msg);
                        }
                        new android.os.Handler().postDelayed(() -> {
                            callPredictionAPIWithRetry(retryCount + 1);
                        }, 2000);
                    } else {
                        if (loadingDialog != null) loadingDialog.dismiss();
                        Toast.makeText(PredictResult.this, errorMessage, Toast.LENGTH_LONG).show();

                        if (t instanceof java.net.UnknownHostException) {
                            Toast.makeText(PredictResult.this, "Cannot connect to server. Check your internet connection.", Toast.LENGTH_LONG).show();
                        } else if (t instanceof java.net.ConnectException) {
                            Toast.makeText(PredictResult.this, "Server is not responding. Please try again later.", Toast.LENGTH_LONG).show();
                        } else if (t instanceof javax.net.ssl.SSLException) {
                            Toast.makeText(PredictResult.this, "SSL connection error. Please check your network settings.", Toast.LENGTH_LONG).show();
                        } else if (t instanceof java.net.SocketTimeoutException) {
                            Toast.makeText(PredictResult.this, "Request timeout. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });

        } catch (Exception e) {
            if (loadingDialog != null) loadingDialog.dismiss();
            Toast.makeText(this, "Error preparing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void displayPredictionResult(PredictResponse result) {
        try {
            root.tvDiseaseName.setText(result.predicted_disease);
            root.tvSciName.setText(result.disease_info.scientific_name);

            if (result.disease_info.description != null) {
                String formattedDesc = formatText(result.disease_info.description);
                root.tvDiseaseDesc.setText(formattedDesc);
            }

            StringBuilder symptomsText = new StringBuilder();
            if (result.disease_info.symptoms != null) {
                if (result.disease_info.symptoms instanceof String) {
                    String symptomsStr = (String) result.disease_info.symptoms;
                    symptomsText.append(formatText(symptomsStr));
                } else if (result.disease_info.symptoms instanceof List) {
                    List<?> symptomsList = (List<?>) result.disease_info.symptoms;
                    for (Object symptom : symptomsList) {
                        symptomsText.append("• ").append(symptom.toString()).append("\n");
                    }
                }
            }
            root.tvSymptoms.setText(symptomsText.toString());

            StringBuilder treatmentsText = new StringBuilder();
            if (result.disease_info.treatments != null) {
                if (result.disease_info.treatments instanceof String) {
                    String treatmentsStr = (String) result.disease_info.treatments;
                    treatmentsText.append(formatText(treatmentsStr));
                } else if (result.disease_info.treatments instanceof List) {
                    List<?> treatmentsList = (List<?>) result.disease_info.treatments;
                    for (Object treatment : treatmentsList) {
                        treatmentsText.append("• ").append(treatment.toString()).append("\n");
                    }
                }
            }
            root.tvTreatments.setText(treatmentsText.toString());

            if (result.disease_info.cause != null) {
                String formattedCause = formatText(result.disease_info.cause);
                root.tvCause.setText(formattedCause);
            }

            String confidenceText = "Confidence: " + String.format("%.1f%%", result.confidence * 100);
            Toast.makeText(this, confidenceText, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error displaying results: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String formatText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        if (text.contains("•") || text.contains("-") || text.contains("*") ||
                text.contains("1.") || text.contains("2.") || text.contains("3.")) {
            return text;
        }
        if (text.contains(".") && text.split("\\.").length > 2) {
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
        return text;
    }

    @Override
    protected void onDestroy() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        super.onDestroy();
    }
}