package com.example.sswhatsapp.retrofit;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

public interface RetrofitCalls {
    @POST("send")
    Call<String> sendMessage(@HeaderMap HashMap<String, String> headers, @Body String chatBody);
}
