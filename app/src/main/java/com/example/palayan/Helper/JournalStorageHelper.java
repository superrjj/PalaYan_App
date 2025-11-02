package com.example.palayan.Helper;

import android.content.Context;

import com.example.palayan.Helper.AppHelper.DeviceUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class JournalStorageHelper {
    private static final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private static final Gson gson = new Gson();

    public static void saveRiceField(Context context, RiceFieldProfile riceField, OnSaveListener listener) {
        String deviceId = DeviceUtils.getDeviceId(context);
        
        // Convert RiceFieldProfile to Map for Firestore
        Map<String, Object> data = new HashMap<>();
        data.put("id", riceField.getId());
        data.put("name", riceField.getName());
        data.put("imageUrl", riceField.getImageUrl() != null ? riceField.getImageUrl() : "");
        data.put("province", riceField.getProvince() != null ? riceField.getProvince() : "");
        data.put("city", riceField.getCity() != null ? riceField.getCity() : "");
        data.put("barangay", riceField.getBarangay() != null ? riceField.getBarangay() : "");
        data.put("sizeHectares", riceField.getSizeHectares());
        data.put("soilType", riceField.getSoilType());
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("deviceId", deviceId);
        
        // Convert history list to JSON string (Firestore doesn't support nested serializable objects directly)
        if (riceField.getHistory() != null && !riceField.getHistory().isEmpty()) {
            data.put("history", gson.toJson(riceField.getHistory()));
        } else {
            data.put("history", "[]");
        }

        firestore.collection("users")
                .document(deviceId)
                .collection("rice_fields")
                .document(riceField.getId())
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    public static void deleteRiceField(Context context, String riceFieldId, OnDeleteListener listener) {
        String deviceId = DeviceUtils.getDeviceId(context);
        
        firestore.collection("users")
                .document(deviceId)
                .collection("rice_fields")
                .document(riceFieldId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    public static void addHistoryEntry(Context context, String riceFieldId, RiceFieldProfile.HistoryEntry entry, OnSaveListener listener) {
        String deviceId = DeviceUtils.getDeviceId(context);
        
        // Get the rice field first
        firestore.collection("users")
                .document(deviceId)
                .collection("rice_fields")
                .document(riceFieldId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get existing history
                        String historyJson = documentSnapshot.getString("history");
                        if (historyJson == null || historyJson.isEmpty()) {
                            historyJson = "[]";
                        }
                        
                        // Parse, add new entry, and save back
                        try {
                            java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<java.util.List<RiceFieldProfile.HistoryEntry>>(){}.getType();
                            java.util.List<RiceFieldProfile.HistoryEntry> historyList = gson.fromJson(historyJson, listType);
                            if (historyList == null) {
                                historyList = new java.util.ArrayList<>();
                            }
                            historyList.add(entry);
                            
                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("history", gson.toJson(historyList));
                            
                            firestore.collection("users")
                                    .document(deviceId)
                                    .collection("rice_fields")
                                    .document(riceFieldId)
                                    .update(updateData)
                                    .addOnSuccessListener(aVoid -> {
                                        if (listener != null) {
                                            listener.onSuccess();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        if (listener != null) {
                                            listener.onFailure(e.getMessage());
                                        }
                                    });
                        } catch (Exception e) {
                            if (listener != null) {
                                listener.onFailure("Error parsing history: " + e.getMessage());
                            }
                        }
                    } else {
                        if (listener != null) {
                            listener.onFailure("Rice field not found");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    public interface OnSaveListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnDeleteListener {
        void onSuccess();
        void onFailure(String error);
    }
}

