package com.example.palayan.Helper;

public class RiceVariety {
    public String rice_seed_id;
    public String varietyName;
    public String releaseName;
    public String breedingCode;
    public String yearRelease;
    public String breederOrigin;
    public int maturityDays;
    public int plantHeight;
    public double averageYield;
    public double maxYield;
    public String location;
    public String environment;
    public String season;
    public String plantingMethod;
    public Boolean archived;

    public RiceVariety() {

    }

    public RiceVariety(String rice_seed_id, String varietyName, String releaseName, String breedingCode, String yearRelease,
                       String breederOrigin, int maturityDays, int plantHeight, double averageYield, double maxYield,
                       String location, String environment, String season, String plantingMethod, Boolean archived) {
        this.rice_seed_id = rice_seed_id;
        this.varietyName = varietyName;
        this.releaseName = releaseName;
        this.breedingCode = breedingCode;
        this.yearRelease = yearRelease;
        this.breederOrigin = breederOrigin;
        this.maturityDays = maturityDays;
        this.plantHeight = plantHeight;
        this.averageYield = averageYield;
        this.maxYield = maxYield;
        this.location = location;
        this.environment = environment;
        this.season = season;
        this.plantingMethod = plantingMethod;
        this.archived = archived;
    }
}
