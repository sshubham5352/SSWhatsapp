package com.example.sswhatsapp.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.sswhatsapp.R;
import com.example.sswhatsapp.databinding.ActivitySignUpBinding;
import com.example.sswhatsapp.firebase.FirebaseConstants;
import com.example.sswhatsapp.firebase.FirestoreManager;
import com.example.sswhatsapp.firebase.FirestoreNetworkCallListener;
import com.example.sswhatsapp.firebase.RealtimeDbManager;
import com.example.sswhatsapp.firebase.RealtimeDbNetworkCallListener;
import com.example.sswhatsapp.models.UserDetailsResponse;
import com.example.sswhatsapp.utils.Constants;
import com.example.sswhatsapp.utils.Helper;
import com.example.sswhatsapp.utils.SessionManager;
import com.example.sswhatsapp.utils.TimeHandler;
import com.google.firebase.firestore.QuerySnapshot;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener, FirestoreNetworkCallListener, RealtimeDbNetworkCallListener {

    //fields
    private ActivitySignUpBinding binding;
    private FirestoreManager firestoreManager;
    private RealtimeDbManager realtimeDbManager;
    private InputMethodManager imm;
    private Uri imgProfileUri;
    private String fcmToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up);
        firestoreManager = new FirestoreManager(this, this);
        realtimeDbManager = new RealtimeDbManager(this);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        //setting onClickListener
        binding.btnLogIn.setOnClickListener(this);
        binding.btnSignUp.setOnClickListener(this);
        binding.imgUserProfile.setOnClickListener(this);
        binding.radioGroupGender.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_log_in) {
            startLoginActivity();
        } else if (view.getId() == R.id.btn_sign_up) {
            if (areSignupFieldsValid()) {
                imm.hideSoftInputFromWindow(binding.rootLayout.getWindowToken(), 0); // closing soft keyboard
                binding.rootLayout.scrollTo(0, 0);
                getFCMToken();
            }
        } else if (view.getId() == R.id.img_user_profile) {
            importImgFromDevice();
        }
    }

    private void importImgFromDevice() {
        Intent intent = new Intent();
        intent.setType(Constants.INTENT_TYPE_IMAGE);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, Constants.INTENT_CODE_REQUEST_IMG);
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        if (radioGroup.getId() == R.id.radio_group_gender) {
            if (imgProfileUri != null)
                return;
            // i = id
            if (i == R.id.radio_btn_male)
                binding.imgUserProfile.setImageResource(R.drawable.img_male_icon);
            else if (i == R.id.radio_btn_female)
                binding.imgUserProfile.setImageResource(R.drawable.img_female_icon);
            else if (i == R.id.radio_btn_others)
                binding.imgUserProfile.setImageResource(R.drawable.img_others_gender_icon);
        }
    }

    private boolean areSignupFieldsValid() {
        if (Helper.isFieldEmpty(binding.name)) {
            binding.name.setError("Mandatory field");
            return false;
        } else
            binding.name.setError(null);

        if (Helper.isFieldEmpty(binding.emailId)) {
            binding.emailId.setError("Mandatory field");
            return false;
        } else
            binding.emailId.setError(null);

        if (Helper.isFieldEmpty(binding.mobileNumber)) {
            binding.mobileNumber.setError("Mandatory field");
            return false;
        } else
            binding.mobileNumber.setError(null);

        if (Helper.isFieldEmpty(binding.password)) {
            binding.password.setError("Mandatory field");
            return false;
        } else
            binding.password.setError(null);

        if (Helper.isFieldEmpty(binding.confirmPassword)) {
            binding.confirmPassword.setError("Mandatory field");
            return false;
        } else
            binding.confirmPassword.setError(null);

        if (!Helper.isEmailValid(binding.emailId.getText().toString())) {
            binding.emailId.setError("Please enter a valid email");
            return false;
        } else
            binding.emailId.setError(null);

        if (!Helper.isMobileNoValid(binding.mobileNumber.getText().toString())) {
            binding.mobileNumber.setError("Please enter a valid mobile no (without country code)");
            return false;
        } else
            binding.mobileNumber.setError(null);

        if (!Helper.isPasswordValid(binding.password.getText().toString())) {
            binding.password.setError(getResources().getString(R.string.password_constraints));
            return false;
        } else
            binding.password.setError(null);

        if (!binding.password.getText().toString().trim().equals(binding.confirmPassword.getText().toString().trim())) {
            binding.confirmPassword.setError("password doesn't match");
            return false;
        } else
            binding.confirmPassword.setError(null);

        return true;
    }

    private void clearFields() {
        imgProfileUri = null;
        binding.imgUserProfile.setImageResource(R.drawable.img_male_icon);
        binding.radioGroupGender.check(R.id.radio_btn_male);
        binding.name.getText().clear();
        binding.emailId.getText().clear();
        binding.mobileNumber.getText().clear();
        binding.password.getText().clear();
        binding.confirmPassword.getText().clear();
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
    private void uploadUserProfileImageToFirebase() {
        firestoreManager.uploadUserProfileImageToFirebase(imgProfileUri);
    }

    //NETWORK CALL
    private void signUpUser(String imgProfile) {
        String userId = (Helper.getFirstName(binding.name.getText().toString()) + binding.mobileNumber.getText().toString());
        String name = binding.name.getText().toString().trim();
        String gender = Helper.getGender(binding.radioGroupGender.getCheckedRadioButtonId());
        String emailId = binding.emailId.getText().toString().trim();
        String mobileNo = binding.mobileNumber.getText().toString().trim();
        String createdOn = TimeHandler.getCurrentTimeStamp();
        UserDetailsResponse userDetailsResponse = new UserDetailsResponse(userId, fcmToken, name, gender, emailId, mobileNo, createdOn, imgProfile);

        firestoreManager.SignUpUser(userDetailsResponse, binding.password.getText().toString().trim());
    }

    public void startLoginActivity() {
        Intent intent = new Intent(this, LogInActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_pop_in, R.anim.anim_pop_out);
    }

    public void startMainActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.INTENT_CODE_REQUEST_IMG:
                if (data != null && data.getData() != null) {
                    imgProfileUri = data.getData();
                    binding.imgUserProfile.setImageURI(imgProfileUri);
                } else
                    Toast.makeText(this, Constants.TOAST_SOMETHING_WENT_WRONG, Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onFirestoreNetworkCallSuccess(Object response, int serviceCode) {
        switch (serviceCode) {
            case FirebaseConstants.GET_FCM_TOKEN_CALL: {
                fcmToken = (String) response;
                searchUserByMobileNumber();
                break;
            }

            case FirebaseConstants.GET_USER_BY_MOBILE_NO_CALL: {
                QuerySnapshot snapshot = (QuerySnapshot) response;
                if (snapshot.getDocuments().size() == 0) {
                    searchUserByEmailId();
                } else {
                    Toast.makeText(this, "Mobile no already in use.\nPlease Log in...", Toast.LENGTH_LONG).show();
                }
                break;
            }

            case FirebaseConstants.GET_USER_BY_EMAIL_ID_CALL: {
                QuerySnapshot snapshot = (QuerySnapshot) response;
                if (snapshot.getDocuments().size() == 0) {
                    if (imgProfileUri != null)
                        uploadUserProfileImageToFirebase();
                    else
                        signUpUser("");
                } else {
                    Toast.makeText(this, "Email Id already in use.\nPlease Log in...", Toast.LENGTH_LONG).show();
                }
                break;
            }

            case FirebaseConstants.UPLOAD_USER_PROFILE_IMG_CALL: {
                Uri uri = (Uri) response;
                signUpUser(uri.toString());
                break;
            }

            case FirebaseConstants.SIGN_UP_USER_CALL:
                Toast.makeText(this, "User successfully signed in!", Toast.LENGTH_LONG).show();
                UserDetailsResponse userDetailsResponse = (UserDetailsResponse) response;
                realtimeDbManager.createNewUser(userDetailsResponse.getUserId());
                SessionManager.createUserSession(userDetailsResponse);
                startMainActivity();
                break;
        }
    }

    @Override
    public void onFirestoreNetworkCallFailure(Object errorMessage, int serviceCode) {

    }

    @Override
    public void onFirestoreNetworkCallFailure(String errorMessage) {
        Log.d(FirebaseConstants.NETWORK_CALL, errorMessage);
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRealtimeNetworkCallSuccess(Object response, int serviceCode) {

    }

    @Override
    public void onRealtimeNetworkCallFailure(String errorMessage) {

    }
}
