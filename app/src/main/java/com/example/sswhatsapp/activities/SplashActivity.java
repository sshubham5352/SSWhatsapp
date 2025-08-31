package com.example.sswhatsapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.sswhatsapp.R;
import com.example.sswhatsapp.databinding.ActivitySplashBinding;
import com.example.sswhatsapp.firebase.FirebaseConstants;
import com.example.sswhatsapp.firebase.FirestoreManager;
import com.example.sswhatsapp.firebase.FirestoreNetworkCallListener;
import com.example.sswhatsapp.models.responses.InterConnectionResponse;
import com.example.sswhatsapp.utils.Constants;
import com.example.sswhatsapp.utils.SessionManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class SplashActivity extends AppCompatActivity implements FirestoreNetworkCallListener {

    //fields
    private ActivitySplashBinding binding;
    private final long SPLASH_ANIM_TIME_MILLIS = 1350;

    private FirestoreManager firestoreManager;
    long tStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
//        binding.txtAppName.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_right_twist));
        tStart = System.currentTimeMillis();

        SessionManager.initSessionManager(getApplicationContext());

        //ALTERNATE CODE FLOW(TO TEMP ACTIVITY)
//        Intent intent = new Intent(this, TempActivity.class);
//        startActivity(intent);
//        finish();

        if (SessionManager.isLoggedIn()) {
            firestoreManager = new FirestoreManager(this);
            fetchMyInterconnectionsList(SessionManager.getMyInterconnectionsDocId());
        } else {
            startSignupActivity();
        }
    }

    private void startSignupActivity() {
        Intent intent = new Intent(this, SignUpActivity.class);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(intent);
            finish();
        }, getDelayTimeForStartActivity());
    }

    private void startHomeActivity(ArrayList<InterConnectionResponse> myInterconnectionsList) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(Constants.INTENT_MY_INTERCONNECTIONS_LIST_EXTRA, myInterconnectionsList);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(intent);
            finish();
        }, getDelayTimeForStartActivity());
    }

    private long getDelayTimeForStartActivity() {
        long tEnd = System.currentTimeMillis();
        long elapsedTimeMillis = (tEnd - tStart);

        if (elapsedTimeMillis >= SPLASH_ANIM_TIME_MILLIS)
            return 50;      //to avoid lagging animation
        else
            return (SPLASH_ANIM_TIME_MILLIS - elapsedTimeMillis);
    }

    //INTERNET CALL
    public void fetchMyInterconnectionsList(String myInterconnectionsDocId) {
        firestoreManager.getMyInterconnectionsList(myInterconnectionsDocId, false);
    }


    @Override
    public void onFirestoreNetworkCallSuccess(Object response, int serviceCode) {
        switch (serviceCode) {
            case (FirebaseConstants.GET_MY_INTERCONNECTIONS_LIST_CALL): {
                QuerySnapshot querySnapshot = (QuerySnapshot) response;
                ArrayList<InterConnectionResponse> myInterconnectionsList = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    myInterconnectionsList.add(doc.toObject(InterConnectionResponse.class));
                }

                startHomeActivity(myInterconnectionsList);
                break;
            }
        }
    }

    @Override
    public void onFirestoreNetworkCallFailure(Object response, int serviceCode) {
        switch (serviceCode) {
            case (FirebaseConstants.GET_MY_INTERCONNECTIONS_LIST_CALL): {
                Toast.makeText(this, FirebaseConstants.GENERAL_ERROR, Toast.LENGTH_SHORT).show();
                startHomeActivity(new ArrayList<>());
                break;
            }
        }
    }

    @Override
    public void onFirestoreNetworkCallFailure(String errorMessage) {

    }
}