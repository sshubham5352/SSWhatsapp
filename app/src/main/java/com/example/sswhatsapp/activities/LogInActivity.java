package com.example.sswhatsapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.sswhatsapp.R;
import com.example.sswhatsapp.databinding.ActivityLogInBinding;
import com.example.sswhatsapp.firebase.FirebaseConstants;
import com.example.sswhatsapp.firebase.FirestoreManager;
import com.example.sswhatsapp.firebase.FirestoreNetworkCallListener;
import com.example.sswhatsapp.firebase.RealtimeDbManager;
import com.example.sswhatsapp.models.responses.InterConnectionResponse;
import com.example.sswhatsapp.models.responses.UserDetailsResponse;
import com.example.sswhatsapp.utils.Constants;
import com.example.sswhatsapp.utils.Helper;
import com.example.sswhatsapp.utils.SessionManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class LogInActivity extends AppCompatActivity implements View.OnClickListener, FirestoreNetworkCallListener {

    //fields
    private ActivityLogInBinding binding;
    private FirestoreManager firestoreManager;
    private RealtimeDbManager realtimeDbManager;
    private UserDetailsResponse myUserDetailsResponse;
    private InputMethodManager imm;
    private String fcmToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_log_in);
        firestoreManager = new FirestoreManager(this, this);
        realtimeDbManager = new RealtimeDbManager();
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        setOnBackPressedListener();

        //setting onClickListener
        binding.btnLogIn.setOnClickListener(this);
        binding.btnSignUp.setOnClickListener(this);
        //setting onEditorAction
        setMobileNoTextChangeListener();
        setEmailIdTextChangeListener();
    }

    private void setOnBackPressedListener() {
        //onBack pressed
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.anim_pop_in, R.anim.anim_pop_out);
            }
        });
    }

    private void setMobileNoTextChangeListener() {
        TextWatcher listener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == 0) {
                    // field got empty
                    Helper.makeViewActive(binding.emailId);
                } else {
                    Helper.makeViewInactive(binding.emailId);
                    binding.emailId.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
        binding.mobileNumber.addTextChangedListener(listener);
    }

    private void setEmailIdTextChangeListener() {
        TextWatcher listener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == 0) {
                    // field got empty
                    Helper.makeViewActive(binding.mobileNumber);
                } else {
                    Helper.makeViewInactive(binding.mobileNumber);
                    binding.mobileNumber.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
        binding.emailId.addTextChangedListener(listener);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_sign_up) {
            getOnBackPressedDispatcher().onBackPressed();
        } else if (view.getId() == R.id.btn_log_in) {
            if (areLoginFieldsValid()) {
                imm.hideSoftInputFromWindow(binding.rootLayout.getWindowToken(), 0); // closing soft keyboard
                binding.rootLayout.scrollTo(0, 0);
                getFCMToken();
            }
        }
    }

    private boolean areLoginFieldsValid() {
        if (Helper.isFieldEmpty(binding.mobileNumber) && Helper.isFieldEmpty(binding.emailId)) {
            binding.mobileNumber.setError("Please fill either mobile no or email");
            binding.emailId.setError("Please fill either mobile no or email");
            return false;
        } else {
            binding.mobileNumber.setError(null);
            binding.emailId.setError(null);
        }

        if (!Helper.isFieldEmpty(binding.mobileNumber) && !Helper.isMobileNoValid(binding.mobileNumber.getText().toString())) {
            binding.mobileNumber.setError("Please enter a valid mobile no (without country code)");
            return false;
        } else
            binding.mobileNumber.setError(null);

        if (!Helper.isFieldEmpty(binding.emailId) && !Helper.isEmailValid(binding.emailId.getText().toString())) {
            binding.emailId.setError("Please enter a valid email");
            return false;
        } else
            binding.emailId.setError(null);


        if (!Helper.isPasswordValid(binding.password.getText().toString())) {
            binding.password.setError(getResources().getString(R.string.password_constraints));
            return false;
        } else
            binding.password.setError(null);

        return true;
    }

    private boolean isPasswordCorrect(String correctPassword) {
        return binding.password.getText().toString().trim().matches(correctPassword);
    }

    //NETWORK CALL
    private void getFCMToken() {
        firestoreManager.getFCMToken();
    }

    //NETWORK CALL
    private void searchUserByMobileNumber() {
        firestoreManager.getUserByMobileNo(binding.mobileNumber.getText().toString().trim());
    }

    //NETWORK CALL
    private void searchUserByEmailId() {
        firestoreManager.getUserByEmailId(binding.emailId.getText().toString().trim());
    }

    //NETWORK CALL
    private void updateFcmToken() {
        firestoreManager.updateFcmTokenField(myUserDetailsResponse.getUserId(), fcmToken);
    }

    //INTERNET CALL
    public void getMyInterconnectionsList(String myInterconnectionsDocId) {
        firestoreManager.getMyInterconnectionsList(myInterconnectionsDocId, false);
    }

    //INTERNET CALL
    public void updateMyOnlineStatus(boolean isOnline) {
        if (myUserDetailsResponse.userId != null) {
            realtimeDbManager.updateMyOnlineStatusOnDisconnect(myUserDetailsResponse.userId);
            realtimeDbManager.updateMyOnlineStatus(isOnline, myUserDetailsResponse.userId);
        }
    }

    private void createUserSession() {
        SessionManager.createUserSession(myUserDetailsResponse);
    }

    //STATING ACTIVITY
    private void startHomeActivity(ArrayList<InterConnectionResponse> myInterconnectionsList) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(Constants.INTENT_MY_INTERCONNECTIONS_LIST_EXTRA, myInterconnectionsList);
        startActivity(intent);
        finishAffinity();
    }

    @Override
    public void onFirestoreNetworkCallSuccess(Object response, int serviceCode) {
        switch (serviceCode) {
            case FirebaseConstants.GET_FCM_TOKEN_CALL: {
                fcmToken = (String) response;

                if (binding.mobileNumber.getText().length() != 0)
                    searchUserByMobileNumber();
                else
                    searchUserByEmailId();
                break;
            }
            case FirebaseConstants.GET_USER_BY_MOBILE_NO_CALL: {
                QuerySnapshot snapshot = (QuerySnapshot) response;
                if (snapshot.getDocuments().size() == 0) {
                    Toast.makeText(this, "Mobile no does not exist.\n if new user please sign up", Toast.LENGTH_LONG).show();
                    return;
                }

                String correctPassword = (String) snapshot.getDocuments().get(0).get(FirebaseConstants.KEY_USER_PASSWORD);
                if (isPasswordCorrect(correctPassword)) {
                    myUserDetailsResponse = snapshot.getDocuments().get(0).toObject(UserDetailsResponse.class);
                    updateFcmToken();
                } else {
                    Toast.makeText(this, "Incorrect password", Toast.LENGTH_LONG).show();
                }
                break;
            }

            case FirebaseConstants.GET_USER_BY_EMAIL_ID_CALL: {
                QuerySnapshot snapshot = (QuerySnapshot) response;
                if (snapshot.getDocuments().size() == 0) {
                    Toast.makeText(this, "Email id does not exist.\n if new user please sign up", Toast.LENGTH_LONG).show();
                    return;
                }

                String correctPassword = (String) snapshot.getDocuments().get(0).get(FirebaseConstants.KEY_USER_PASSWORD);
                if (isPasswordCorrect(correctPassword)) {
                    myUserDetailsResponse = snapshot.getDocuments().get(0).toObject(UserDetailsResponse.class);
                    updateFcmToken();
                } else {
                    Toast.makeText(this, "Incorrect password", Toast.LENGTH_LONG).show();
                }
                break;
            }

            case FirebaseConstants.UPDATE_FIELD_FCM_TOKEN_CALL: {
                myUserDetailsResponse.setFcmToken(fcmToken);
                createUserSession();
                getMyInterconnectionsList(myUserDetailsResponse.getMyInterconnectionsDocId());
                break;
            }

            case (FirebaseConstants.GET_MY_INTERCONNECTIONS_LIST_CALL): {
                QuerySnapshot querySnapshot = (QuerySnapshot) response;
                ArrayList<InterConnectionResponse> myInterconnectionsList = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    myInterconnectionsList.add(doc.toObject(InterConnectionResponse.class));
                }

                updateMyOnlineStatus(true);
                startHomeActivity(myInterconnectionsList);
                break;
            }
        }
    }

    @Override
    public void onFirestoreNetworkCallFailure(Object errorMessage, int serviceCode) {
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
        myUserDetailsResponse = null;
        Log.d(FirebaseConstants.NETWORK_CALL, errorMessage);
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }
}