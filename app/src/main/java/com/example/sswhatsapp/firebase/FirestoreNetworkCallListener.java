package com.example.sswhatsapp.firebase;

public interface FirestoreNetworkCallListener {
    void onFirestoreNetworkCallSuccess(Object response, int serviceCode);

    void onFirestoreNetworkCallFailure(Object response, int serviceCode);

    void onFirestoreNetworkCallFailure(String errorMessage);
}
