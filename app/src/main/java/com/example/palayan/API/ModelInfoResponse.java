package com.example.palayan.API;

import java.util.List;

public class ModelInfoResponse {
    public boolean model_loaded;
    public int num_classes;
    public List<String> classes;
    public String model_version;
    public String timestamp;
}