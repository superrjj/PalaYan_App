package com.example.palayan.UserActivities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.MultipartBody;
import com.example.palayan.API.ApiService;
import com.example.palayan.API.PredictResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class PredictorRepository {
    private final ApiService apiService;
    private final LocalPredictor local;

    public PredictorRepository(ApiService apiService, LocalPredictor local) {
        this.apiService = apiService;
        this.local = local;
    }

    public interface ResultCallback {
        void onSuccess(String disease, float confidence, Map<String, Float> allPreds, PredictResponse raw);
        void onError(Throwable t);
    }

    public void predict(Context ctx, Bitmap bitmap, boolean forceLocal, ResultCallback cb) {
        boolean online = isOnline(ctx);
        boolean canLocal = (local != null && local.isReady());
        if (forceLocal || !online || !canLocal) {
            try {
                LocalPredictor.PredictionResult r = local.predict(bitmap);
                Map<String, Float> all = new LinkedHashMap<>();
                cb.onSuccess(r.label.replace("_"," "), r.confidence, all, null);
                return;
            } catch (Throwable t) {
                cb.onError(t);
                return;
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        okhttp3.RequestBody body = okhttp3.RequestBody.create(
                okhttp3.MediaType.parse("image/jpeg"), baos.toByteArray());
        MultipartBody.Part part = MultipartBody.Part.createFormData("image","image.jpg", body);

        apiService.predictDisease(part).enqueue(new Callback<PredictResponse>() {
            @Override public void onResponse(Call<PredictResponse> call, Response<PredictResponse> response) {
                if (response.isSuccessful() && response.body()!=null) {
                    PredictResponse r = response.body();
                    Map<String, Float> all = new LinkedHashMap<>();
                    if (r.all_predictions != null) {
                        for (Map.Entry<String, ?> e : r.all_predictions.entrySet()) {
                            Object val = e.getValue();
                            float f;
                            if (val instanceof Number) {
                                f = ((Number) val).floatValue();
                            } else {
                                try { f = Float.parseFloat(String.valueOf(val)); } catch (Exception ex) { f = 0f; }
                            }
                            all.put(e.getKey(), f);
                        }
                    }
                    cb.onSuccess(r.predicted_disease, (float) r.confidence, all, r);
                } else {
                    cb.onError(new Exception("API "+response.code()));
                }
            }
            @Override public void onFailure(Call<PredictResponse> call, Throwable t) { cb.onError(t); }
        });
    }

    private static boolean isOnline(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm != null ? cm.getActiveNetworkInfo() : null;
        return ni != null && ni.isConnected();
    }
}