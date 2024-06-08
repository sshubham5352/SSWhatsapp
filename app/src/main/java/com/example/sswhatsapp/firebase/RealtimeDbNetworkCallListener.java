package com.example.sswhatsapp.firebase;

public interface RealtimeDbNetworkCallListener {
    void onRealtimeNetworkCallSuccess(Object response, int serviceCode);


    void onRealtimeNetworkCallFailure(String errorMessage);
}
