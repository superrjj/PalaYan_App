package com.example.palayan.API;

import java.util.List;
import java.util.Map;

public class PredictResponse {
    public String status;
    public String predicted_disease;
    public float confidence;
    public Map<String, Float> all_predictions;
    public ModelVersion model_version;
    public DiseaseInfo disease_info;

    public static class ModelVersion {
        public String version;
    }

    public static class DiseaseInfo {
        public String scientific_name;
        public String description;
        public List<String> symptoms;
        public String cause;
        public List<String> treatments;
    }
}