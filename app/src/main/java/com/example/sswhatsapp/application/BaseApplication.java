package com.example.sswhatsapp.application;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.example.sswhatsapp.firebase.FirestoreNetworkCallListener;
import com.example.sswhatsapp.firebase.RealtimeDbManager;
import com.example.sswhatsapp.utils.SessionManager;

public class BaseApplication extends Application implements DefaultLifecycleObserver, FirestoreNetworkCallListener {
    //Fields
    private RealtimeDbManager realtimeDbManager;

    @Override

    public void onCreate() {
        super.onCreate();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        realtimeDbManager = new RealtimeDbManager();
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStart(owner);

        String myUserId = SessionManager.getUserId();
        if (myUserId != null) {
            realtimeDbManager.updateMyOnlineStatusOnDisconnect(myUserId);
            realtimeDbManager.updateMyOnlineStatus(true, myUserId);
        }
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStop(owner);

        String myUserId = SessionManager.getUserId();
        if (myUserId != null) {
            realtimeDbManager.updateMyOnlineStatus(false, myUserId);
        }
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
