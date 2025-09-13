package com.example.palayan.API;

import java.util.List;

public class DiseasesResponse {
    public String status;
    public int count;
    public List<Disease> diseases;

    public static class Disease {
        public String id;
        public String name;
        public String scientific_name;
        public String description;
        public Object symptoms;  // I-change mo to Object para ma-handle both String at List
        public String cause;
        public Object treatments; // I-change mo din to Object
    }
}