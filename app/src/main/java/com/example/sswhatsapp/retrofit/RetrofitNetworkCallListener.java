package com.example.sswhatsapp.retrofit;

public interface RetrofitNetworkCallListener {
    void onRetrofitNetworkCallSuccess(Object response, int serviceCode);

    void onRetrofitNetworkCallFailure(String errorMessage);
}
