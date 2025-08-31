package com.example.sswhatsapp.retrofit;

import android.app.ProgressDialog;
import android.content.Context;

import com.example.sswhatsapp.firebase.FirebaseConstants;
import com.example.sswhatsapp.models.bodies.NotificationBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RetrofitManager {
    private Context mContext;
    private final RetrofitNetworkCallListener mListener;
    private final RetrofitCalls retrofitCalls;
    private ProgressDialog progressDialog;

    //CONSTRUCTOR
    public RetrofitManager(RetrofitNetworkCallListener listener) {
        mListener = listener;
        retrofitCalls = RetrofitClient.getClient().create(RetrofitCalls.class);
    }

    //CONSTRUCTOR
    public RetrofitManager(Context context, RetrofitNetworkCallListener listener) {
        mContext = context;
        mListener = listener;
        retrofitCalls = RetrofitClient.getClient().create(RetrofitCalls.class);

        progressDialog = new ProgressDialog(mContext);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Loading...");
    }

    public void sendChatNotificationCall(String receiverFCMToken, JSONObject dataMap) {
        JSONArray token = new JSONArray();
        token.put(receiverFCMToken);
        JSONObject chatBody = new JSONObject();
        try {
            chatBody.put(RetrofitConstants.REMOTE_MSG_REGISTRATION_IDS, token);
            chatBody.put(RetrofitConstants.REMOTE_MSG_DATA, dataMap);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        Call<String> call = retrofitCalls.sendMessage(RetrofitClient.getFMCRemoteMsgHeaders(), chatBody.toString());

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject responseJson = new JSONObject(response.body());
                        JSONArray results = responseJson.getJSONArray(RetrofitConstants.JSON_ARRAY_RESULTS);
                        if (responseJson.getInt(RetrofitConstants.JSON_FAILURE) == 0) {
                            mListener.onRetrofitNetworkCallSuccess(response.body(), RetrofitConstants.SEND_CHAT_NOTIFICATION_CALL);
                        } else {
                            mListener.onRetrofitNetworkCallFailure(RetrofitConstants.NETWORK_CALL_FAILURE + ((JSONObject) results.get(0)).getString(RetrofitConstants.JSON_ERROR));
                        }
                    } catch (JSONException e) {
                        mListener.onRetrofitNetworkCallFailure(RetrofitConstants.NETWORK_CALL_FAILURE + e.getMessage());
                    }
                } else if (response.code() == 401) {
                    mListener.onRetrofitNetworkCallFailure(RetrofitConstants.NETWORK_CALL_FAILURE + RetrofitConstants.UNAUTHORIZED_CALL_ERROR);
                } else if (response.code() == 404) {
                    mListener.onRetrofitNetworkCallFailure(RetrofitConstants.NETWORK_CALL_FAILURE + RetrofitConstants.INVALID_REQUEST_ERROR);
                } else {
                    mListener.onRetrofitNetworkCallFailure(RetrofitConstants.NETWORK_CALL_FAILURE + RetrofitConstants.GENERAL_ERROR);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                mListener.onRetrofitNetworkCallFailure(RetrofitConstants.NETWORK_CALL_FAILURE + RetrofitConstants.GENERAL_ERROR);
            }
        });
    }

    public void sendChatNotificationCallV1(String receiverFCMToken, HashMap<String, String> dataMap) {
        Thread thread = new Thread(() -> {
            AccessToken accessToken = new AccessToken();
            String token = accessToken.getAccessToken();

            NotificationBody notificationBody = new NotificationBody(new NotificationBody.NotificationData(receiverFCMToken, dataMap));
            Call<NotificationBody> call = retrofitCalls.sendNotificationV1("Bearer " + token, FirebaseConstants.PROJECT_ID, notificationBody);

            call.enqueue(new Callback<NotificationBody>() {
                @Override
                public void onResponse(Call<NotificationBody> call, Response<NotificationBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            JSONObject responseJson = new JSONObject(response.message());
                            JSONArray results = responseJson.getJSONArray(RetrofitConstants.JSON_ARRAY_RESULTS);
                            if (responseJson.getInt(RetrofitConstants.JSON_FAILURE) == 0) {
                                mListener.onRetrofitNetworkCallSuccess(response.body(), RetrofitConstants.SEND_CHAT_NOTIFICATION_CALL);
                            } else {
                                mListener.onRetrofitNetworkCallFailure(RetrofitConstants.NETWORK_CALL_FAILURE + ((JSONObject) results.get(0)).getString(RetrofitConstants.JSON_ERROR));
                            }
                        } catch (JSONException e) {
                            mListener.onRetrofitNetworkCallFailure(RetrofitConstants.NETWORK_CALL_FAILURE + e.getMessage());
                        }
                    } else if (response.code() == 401) {
                        mListener.onRetrofitNetworkCallFailure(RetrofitConstants.NETWORK_CALL_FAILURE + RetrofitConstants.UNAUTHORIZED_CALL_ERROR);
                    } else if (response.code() == 404) {
                        mListener.onRetrofitNetworkCallFailure(RetrofitConstants.NETWORK_CALL_FAILURE + RetrofitConstants.INVALID_REQUEST_ERROR);
                    } else {
                        mListener.onRetrofitNetworkCallFailure(RetrofitConstants.NETWORK_CALL_FAILURE + RetrofitConstants.GENERAL_ERROR);
                    }
                }

                @Override
                public void onFailure(Call<NotificationBody> call, Throwable t) {
                    mListener.onRetrofitNetworkCallFailure(RetrofitConstants.NETWORK_CALL_FAILURE + RetrofitConstants.GENERAL_ERROR);
                }
            });
        });

        thread.start();
    }
}
