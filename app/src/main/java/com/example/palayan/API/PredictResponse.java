package com.example.palayan.API;

import java.util.List;
import java.util.Map;

public class PredictResponse {
    public String status;
    public String predicted_disease;
    public float confidence;
    public Map<String, Float> all_predictions;
    public String model_version;  // I-change mo to String instead of ModelVersion object
    public DiseaseInfo disease_info;
    public String message;

    public static class DiseaseInfo {
        public String scientific_name;
        public String description;
        public Object symptoms;  // I-change mo to Object para ma-handle both String at List
        public String cause;
        public Object treatments; // I-change mo din to Object
    }
}