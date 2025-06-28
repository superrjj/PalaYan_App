package com.example.palayan.Helper;

public class RiceVariety {
    public String rice_seed_id;
    public String varietyName;
    public String releaseName;
    public String breedingCode;
    public String yearRelease;
    public String breederOrigin;
    public String maturityDays;
    public String plantHeight;
    public String averageYield;
    public String maxYield;
    public String location;
    public String environment;
    public String season;
    public String plantingMethod;

    public RiceVariety() {

    }

    public RiceVariety(String rice_seed_id, String varietyName, String releaseName, String breedingCode, String yearRelease,
                       String breederOrigin, String maturityDays, String plantHeight, String averageYield, String maxYield,
                       String location, String environment, String season, String plantingMethod) {

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
    }
}
