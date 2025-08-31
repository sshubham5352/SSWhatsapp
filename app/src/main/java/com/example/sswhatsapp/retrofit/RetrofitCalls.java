package com.example.sswhatsapp.retrofit;

import android.os.Message;

import com.example.sswhatsapp.models.bodies.NotificationBody;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RetrofitCalls {
    @POST("send")
    Call<String> sendMessage(@HeaderMap HashMap<String, String> headers, @Body String chatBody);

    @POST("v1/projects/{project_id}/messages:send")
    @Headers("Content-Type: application/json")
    Call<NotificationBody> sendNotificationV1(@Header("Authorization") String accessToken, @Path("project_id") String projectId, @Body NotificationBody chatBody);
}
