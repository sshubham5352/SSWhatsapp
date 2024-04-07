package com.example.sswhatsapp.retrofit;

import static com.example.sswhatsapp.retrofit.RetrofitConstants.FCM_SERVER_KEY;
import static com.example.sswhatsapp.retrofit.RetrofitConstants.REMOTE_CHAT_AUTHORIZATION;
import static com.example.sswhatsapp.retrofit.RetrofitConstants.REMOTE_CHAT_CONTENT_TYPE;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {
    //static fields
    public static final String baseURL = "https://fcm.googleapis.com/fcm/";
    public static Retrofit retrofit = null;

    private RetrofitClient() {
        //Empty private constructor to implement Singleton pattern
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .client(getRequestHeader())
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static OkHttpClient getRequestHeader() {
        return new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS).build();
    }

    public static HashMap<String, String> remoteMsgHeaders = null;

    public static HashMap<String, String> getFMCRemoteMsgHeaders() {
        if (remoteMsgHeaders == null) {
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(REMOTE_CHAT_AUTHORIZATION, "key=" + FCM_SERVER_KEY);
            remoteMsgHeaders.put(REMOTE_CHAT_CONTENT_TYPE, "application/json");
        }
        return remoteMsgHeaders;
    }
}
