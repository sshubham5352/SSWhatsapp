package com.example.sswhatsapp.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sswhatsapp.firebase.FirestoreManager;
import com.example.sswhatsapp.firebase.FirestoreNetworkCallListener;
import com.example.sswhatsapp.utils.SessionManager;

public class BaseActivity extends AppCompatActivity implements FirestoreNetworkCallListener {
    //Fields
    FirestoreManager firestoreManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestoreManager = new FirestoreManager(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        firestoreManager.updateMyOnlineStatus(true, SessionManager.getUserId());
    }

    @Override
    protected void onPause() {
        super.onPause();
        firestoreManager.updateMyOnlineStatus(false, SessionManager.getUserId());
    }

    @Override
    public void onFirestoreNetworkCallSuccess(Object response, int serviceCode) {

    }

    @Override
    public void onFirestoreNetworkCallFailure(Object response, int serviceCode) {

    }

    @Override
    public void onFirestoreNetworkCallFailure(String errorMessage) {

    }
}
