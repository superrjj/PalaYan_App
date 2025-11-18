package com.example.palayan.Helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.palayan.Helper.AppHelper.DeviceUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JournalStorageHelper {
    private static final String PREFS_NAME = "JournalStorage";
    private static final String KEY_RICE_FIELDS = "rice_fields";
    private static final String KEY_PLANTINGS_PREFIX = "plantings_";
    private static final Gson gson = new Gson();

    // Rice Field methods
    public static void saveRiceField(Context context, RiceFieldProfile riceField, OnSaveListener listener) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String fieldsJson = prefs.getString(KEY_RICE_FIELDS, "[]");
            
            Type listType = new TypeToken<List<RiceFieldProfile>>(){}.getType();
            List<RiceFieldProfile> fieldsList = gson.fromJson(fieldsJson, listType);
            if (fieldsList == null) {
                fieldsList = new ArrayList<>();
            }
            
            // Check if updating existing field
            boolean found = false;
            for (int i = 0; i < fieldsList.size(); i++) {
                if (fieldsList.get(i).getId().equals(riceField.getId())) {
                    fieldsList.set(i, riceField);
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                fieldsList.add(riceField);
            }
            
            // Save back to SharedPreferences
            prefs.edit()
                    .putString(KEY_RICE_FIELDS, gson.toJson(fieldsList))
                    .apply();
            
            if (listener != null) {
                listener.onSuccess();
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.onFailure(e.getMessage());
            }
        }
    }

    public static void loadRiceFields(Context context, OnFieldsLoadedListener listener) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String fieldsJson = prefs.getString(KEY_RICE_FIELDS, "[]");
            
            Type listType = new TypeToken<List<RiceFieldProfile>>(){}.getType();
            List<RiceFieldProfile> fieldsList = gson.fromJson(fieldsJson, listType);
            
            if (fieldsList == null) {
                fieldsList = new ArrayList<>();
            }
            
            if (listener != null) {
                listener.onSuccess(fieldsList);
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.onFailure(e.getMessage());
            }
        }
    }

    public static void deleteRiceField(Context context, String riceFieldId, OnDeleteListener listener) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String fieldsJson = prefs.getString(KEY_RICE_FIELDS, "[]");
            
            Type listType = new TypeToken<List<RiceFieldProfile>>(){}.getType();
            List<RiceFieldProfile> fieldsList = gson.fromJson(fieldsJson, listType);
            
            if (fieldsList == null) {
                fieldsList = new ArrayList<>();
            }
            
            // Remove the field
            fieldsList.removeIf(field -> field.getId().equals(riceFieldId));
            
            // Save back
            prefs.edit()
                    .putString(KEY_RICE_FIELDS, gson.toJson(fieldsList))
                    .apply();
            
            // Also delete all plantings for this rice field
            String plantingsKey = KEY_PLANTINGS_PREFIX + riceFieldId;
            prefs.edit().remove(plantingsKey).apply();
            
            if (listener != null) {
                listener.onSuccess();
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.onFailure(e.getMessage());
            }
        }
    }

    public static void addHistoryEntry(Context context, String riceFieldId, RiceFieldProfile.HistoryEntry entry, OnSaveListener listener) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String fieldsJson = prefs.getString(KEY_RICE_FIELDS, "[]");
            
            Type listType = new TypeToken<List<RiceFieldProfile>>(){}.getType();
            List<RiceFieldProfile> fieldsList = gson.fromJson(fieldsJson, listType);
            
            if (fieldsList == null) {
                fieldsList = new ArrayList<>();
            }
            
            // Find the rice field and update its history
            RiceFieldProfile targetField = null;
            for (RiceFieldProfile field : fieldsList) {
                if (field.getId().equals(riceFieldId)) {
                    targetField = field;
                    break;
                }
            }
            
            if (targetField == null) {
                if (listener != null) {
                    listener.onFailure("Rice field not found");
                }
                return;
            }
            
            // Get or initialize history
            List<RiceFieldProfile.HistoryEntry> historyList = targetField.getHistory();
            if (historyList == null) {
                historyList = new ArrayList<>();
            }
            
            // Check if updating existing entry
            boolean found = false;
            if (entry.getId() != null && !entry.getId().isEmpty()) {
                for (int i = 0; i < historyList.size(); i++) {
                    if (historyList.get(i).getId().equals(entry.getId())) {
                        historyList.set(i, entry);
                        found = true;
                        break;
                    }
                }
            }
            
            if (!found) {
                historyList.add(entry);
            }
            
            targetField.setHistory(historyList);
            
            // Save back
            prefs.edit()
                    .putString(KEY_RICE_FIELDS, gson.toJson(fieldsList))
                    .apply();
            
            if (listener != null) {
                listener.onSuccess();
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.onFailure("Error: " + e.getMessage());
            }
        }
    }

    // Rice Planting methods
    public static void saveRicePlanting(Context context, String riceFieldId, RicePlanting planting, OnSaveListener listener) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String plantingsKey = KEY_PLANTINGS_PREFIX + riceFieldId;
            String plantingsJson = prefs.getString(plantingsKey, "[]");
            
            Type listType = new TypeToken<List<RicePlanting>>(){}.getType();
            List<RicePlanting> plantingsList = gson.fromJson(plantingsJson, listType);
            
            if (plantingsList == null) {
                plantingsList = new ArrayList<>();
            }
            
            // Check if updating existing planting
            boolean found = false;
            for (int i = 0; i < plantingsList.size(); i++) {
                if (plantingsList.get(i).getId().equals(planting.getId())) {
                    plantingsList.set(i, planting);
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                plantingsList.add(planting);
            }
            
            // Save back
            prefs.edit()
                    .putString(plantingsKey, gson.toJson(plantingsList))
                    .apply();
            
            if (listener != null) {
                listener.onSuccess();
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.onFailure(e.getMessage());
            }
        }
    }

    public static void loadRicePlantings(Context context, String riceFieldId, OnPlantingsLoadedListener listener) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String plantingsKey = KEY_PLANTINGS_PREFIX + riceFieldId;
            String plantingsJson = prefs.getString(plantingsKey, "[]");
            
            Type listType = new TypeToken<List<RicePlanting>>(){}.getType();
            List<RicePlanting> plantingsList = gson.fromJson(plantingsJson, listType);
            
            if (plantingsList == null) {
                plantingsList = new ArrayList<>();
            }
            
            // Filter by riceFieldId to ensure data integrity
            List<RicePlanting> filteredList = new ArrayList<>();
            for (RicePlanting planting : plantingsList) {
                if (planting.getRiceFieldId() != null && planting.getRiceFieldId().equals(riceFieldId)) {
                    filteredList.add(planting);
                }
            }
            
            if (listener != null) {
                listener.onSuccess(filteredList);
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.onFailure(e.getMessage());
            }
        }
    }

    public static void deleteRicePlanting(Context context, String riceFieldId, String plantingId, OnDeleteListener listener) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String plantingsKey = KEY_PLANTINGS_PREFIX + riceFieldId;
            String plantingsJson = prefs.getString(plantingsKey, "[]");
            
            Type listType = new TypeToken<List<RicePlanting>>(){}.getType();
            List<RicePlanting> plantingsList = gson.fromJson(plantingsJson, listType);
            
            if (plantingsList == null) {
                plantingsList = new ArrayList<>();
            }
            
            // Remove the planting
            plantingsList.removeIf(planting -> planting.getId().equals(plantingId));
            
            // Save back
            prefs.edit()
                    .putString(plantingsKey, gson.toJson(plantingsList))
                    .apply();
            
            if (listener != null) {
                listener.onSuccess();
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.onFailure(e.getMessage());
            }
        }
    }

    // Interfaces
    public interface OnSaveListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnDeleteListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnFieldsLoadedListener {
        void onSuccess(List<RiceFieldProfile> fields);
        void onFailure(String error);
    }

    public interface OnPlantingsLoadedListener {
        void onSuccess(List<RicePlanting> plantings);
        void onFailure(String error);
    }
}
