package com.example.palayan.UserActivities;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;

public final class ModelManager {
    private static final String PREFS = "ml_model_prefs";
    private static final String KEY_VERSION = "version";
    private static final String DOC = "rice_disease_classifier";

    public interface LoadCallback {
        void onReady(LocalPredictor predictor);
        void onError(Exception e);
    }

    public static void loadPredictor(Context ctx, LoadCallback cb) {
        try {
            File dir = new File(ctx.getFilesDir(), "ml"); dir.mkdirs();
            File modelFile = new File(dir, "rice_disease_model.tflite");
            File labelsFile = new File(dir, "rice_disease_labels.txt");

            if (modelFile.exists() && labelsFile.exists()) {
                try {
                    LocalPredictor lp = new LocalPredictor(modelFile, labelsFile);
                    cb.onReady(lp);
                    fetchAndUpdateIfNewer(ctx, modelFile, labelsFile, null);
                    return;
                } catch (Exception ignored) {}
            }
            try {
                LocalPredictor lp = new LocalPredictor(ctx);
                cb.onReady(lp);
            } catch (Exception e) {
                cb.onError(e);
                return;
            }
            fetchAndUpdateIfNewer(ctx, new File(ctx.getFilesDir(), "ml/rice_disease_model.tflite"),
                    new File(ctx.getFilesDir(), "ml/rice_disease_labels.txt"), null);
        } catch (Exception e) {
            cb.onError(e);
        }
    }

    private static void fetchAndUpdateIfNewer(Context ctx, File modelFile, File labelsFile, Runnable onDone) {
        FirebaseFirestore.getInstance().collection("model_info").document(DOC).get()
                .addOnSuccessListener(doc -> {
                    if (doc == null || !doc.exists()) { if (onDone!=null) onDone.run(); return; }
                    Long remoteVersion = doc.getLong("version");
                    String tfliteUrl = doc.getString("tflite_url");
                    String labelsUrl = doc.getString("labels_url");
                    if (remoteVersion == null || tfliteUrl == null || labelsUrl == null) { if (onDone!=null) onDone.run(); return; }
                    long localVersion = getLocalVersion(ctx);
                    if (remoteVersion <= localVersion) { if (onDone!=null) onDone.run(); return; }

                    FirebaseStorage.getInstance().getReferenceFromUrl(tfliteUrl).getFile(modelFile)
                            .addOnSuccessListener(a -> FirebaseStorage.getInstance().getReferenceFromUrl(labelsUrl).getFile(labelsFile)
                                    .addOnSuccessListener(b -> {
                                        saveLocalVersion(ctx, remoteVersion);
                                        if (onDone!=null) onDone.run();
                                    })
                                    .addOnFailureListener(e -> { if (onDone!=null) onDone.run(); }))
                            .addOnFailureListener(e -> { if (onDone!=null) onDone.run(); });
                })
                .addOnFailureListener(e -> { if (onDone!=null) onDone.run(); });
    }

    private static long getLocalVersion(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sp.getLong(KEY_VERSION, 0L);
    }

    private static void saveLocalVersion(Context ctx, long version) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().putLong(KEY_VERSION, version).apply();
    }
}