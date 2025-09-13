package com.example.palayan.API;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://palayan-rice-disease-api-production.up.railway.app/";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // I-update mo yung timeout settings at connection pooling
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)  // I-increase mo to 60 seconds
                    .readTimeout(60, TimeUnit.SECONDS)     // I-increase mo to 60 seconds
                    .writeTimeout(60, TimeUnit.SECONDS)    // I-increase mo to 60 seconds
                    .retryOnConnectionFailure(true)        // I-add mo to para sa retry
                    .connectionPool(new okhttp3.ConnectionPool(5, 5, TimeUnit.MINUTES))  // I-add mo connection pooling
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }
}