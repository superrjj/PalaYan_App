package com.example.palayan.API;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    @Multipart
    @POST("predict_disease") // full URL will be in Retrofit builder
    Call<PredictResponse> predictDisease(
            @Part MultipartBody.Part image
    );
}
