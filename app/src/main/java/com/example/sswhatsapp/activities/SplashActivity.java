package com.example.sswhatsapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.sswhatsapp.R;
import com.example.sswhatsapp.databinding.ActivitySplashBinding;
import com.example.sswhatsapp.models.UserDetailsResponse;
import com.example.sswhatsapp.utils.Constants;
import com.example.sswhatsapp.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    //fields
    private ActivitySplashBinding binding;
    private final long SPLASH_ANIM_TIME_MILLIS = 1750;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        binding.txtAppName.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_right_twist));

        long tStart = System.currentTimeMillis();

        Intent intent;
        SessionManager.initSessionManager(getApplicationContext());
        if (SessionManager.isLoggedIn())
            intent = new Intent(this, HomeActivity.class);
        else
            intent = new Intent(this, SignUpActivity.class);

        long tEnd = System.currentTimeMillis();
        long elapsedTimeMillis = (tEnd - tStart);
        long delayTimeMillis;

        if (elapsedTimeMillis >= SPLASH_ANIM_TIME_MILLIS)
            delayTimeMillis = 0;
        else
            delayTimeMillis = SPLASH_ANIM_TIME_MILLIS - elapsedTimeMillis;


//        ALTERING CODE FLOW: 1
//        new Handler(Looper.getMainLooper()).postDelayed(() -> {
//            Intent alterIntent = new Intent(SplashActivity.this, ChatWithIndividualActivity.class);
//
//            UserDetailsResponse response = new UserDetailsResponse("Amit8287251828", "Bhrata Shree", "male", "amits6383@gmail.com", "8287251828", "32 23", "https://firebasestorage.googleapis.com/v0/b/ss-whatsapp-84666.appspot.com/o/User%20Profile%20Images%2FIMG-20210820-WA0022.jpg?alt=media&token=11bfd3b1-18b5-47c6-b048-82d7b81ae311");
//            alterIntent.putExtra(Constants.INTENT_USER_DETAILS_EXTRA, response);
//            alterIntent.putExtra(Constants.INTENT_CONNECTION_ID_EXTRA, "Shubham9818231612_&_Amit8287251828");
//            alterIntent.putExtra(Constants.INTENT_IS_ERADICATED, false);
//            startActivity(alterIntent);
//            finish();
//        }, delayTimeMillis);


//        ALTERING CODE FLOW: 2
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent alterIntent = new Intent(SplashActivity.this, ChatWithIndividualActivity.class);

            UserDetailsResponse response = new UserDetailsResponse("Shubham9818231612", "Shubham Sharma", "male", "sshubham5352@gmail.com", "9818231612", "32 23", "https://firebasestorage.googleapis.com/v0/b/ss-whatsapp-84666.appspot.com/o/User%20Profile%20Images%2F2023_12_29_00_58_16?alt=media&token=235cc553-359f-4039-847d-525b47b0a927");
            alterIntent.putExtra(Constants.INTENT_USER_DETAILS_EXTRA, response);
            alterIntent.putExtra(Constants.INTENT_CONNECTION_ID_EXTRA, "Shubham9818231612_&_Amit8287251828");
            alterIntent.putExtra(Constants.INTENT_IS_NEW_CONNECTION_EXTRA, false);
            startActivity(alterIntent);
            finish();
        }, delayTimeMillis);


//        new Handler(Looper.getMainLooper()).postDelayed(() -> {
//            startActivity(intent);
//            finish();
//        }, delayTimeMillis);
    }
}