package com.example.palayan.Helper;

import java.io.Serializable;

public class FertilizerScheduleEntry implements Serializable {
    private String stage; // "Punlaan", "Ika-1", "Ika-2", "Ika-3"
    private String fertilizer; // Fertilizer type and amount
    private String timing; // Timing (e.g., "7-10 DAS", "0-14 DAT")

    public FertilizerScheduleEntry() {
    }

    public FertilizerScheduleEntry(String stage, String fertilizer, String timing) {
        this.stage = stage;
        this.fertilizer = fertilizer;
        this.timing = timing;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getFertilizer() {
        return fertilizer;
    }

    public void setFertilizer(String fertilizer) {
        this.fertilizer = fertilizer;
    }

    public String getTiming() {
        return timing;
    }

    public void setTiming(String timing) {
        this.timing = timing;
    }
}

