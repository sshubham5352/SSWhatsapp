package com.example.sswhatsapp.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.sswhatsapp.activities.SignUpActivity;
import com.example.sswhatsapp.firebase.FirebaseClients;
import com.example.sswhatsapp.models.UserDetailsResponse;

public class SessionManager {

    //Class Variables
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static final String PREF_NAME = "com.example.sswhatsapp";

    // All Shared Preferences Keys
    private static final String KEY_IS_LOGGED_IN = "01";
    private static final String KEY_USER_ID = "02";
    private static final String KEY_USER_FIRST_NAME = "03";
    private static final String KEY_USER_LAST_NAME = "04";
    private static final String KEY_USER_EMAIL = "05";
    private static final String KEY_USER_MOBILE_NO = "06";
    private static final String KEY_GENDER = "07";
    private static final String KEY_PROFILE_IMG_URL = "08";
    private static final String KEY_MY_INTERCONNECTIONS_DOC_ID = "09";
    private static final String KEY_FCM_TOKEN = "10";
    private static final String KEY_CREATED_ON = "11";

    // Constructor
    private SessionManager() {
        //private empty constructor for singleton approach
    }

    public static void initSessionManager(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
            editor.apply();
        }
    }

    public static boolean isInitiated() {
        return (sharedPreferences != null);
    }

    public static void createUserSession(UserDetailsResponse user) {
        String[] userNameSubParts = user.getName().split(" ");
        String firstName = userNameSubParts[0];
        String lastName = null;
        if (userNameSubParts.length > 1)
            lastName = userNameSubParts[userNameSubParts.length - 1];

        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_CREATED_ON, user.getCreatedOn());
        editor.putString(KEY_USER_ID, user.getUserId());
        editor.putString(KEY_FCM_TOKEN, user.getFcmToken());
        editor.putString(KEY_USER_FIRST_NAME, firstName);
        editor.putString(KEY_USER_LAST_NAME, lastName);
        editor.putString(KEY_GENDER, user.getGender());
        editor.putString(KEY_USER_EMAIL, user.getEmailId());
        editor.putString(KEY_USER_MOBILE_NO, user.getMobileNo());
        editor.putString(KEY_PROFILE_IMG_URL, user.getProfileImgUrl());
        editor.putString(KEY_MY_INTERCONNECTIONS_DOC_ID, user.getMyInterconnectionsDocId());
        // commit changes
        editor.apply();
    }

    public static boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public static UserDetailsResponse getUser() {
        String userId = getUserId();
        String fcmToken = getFcmToken();
        String name = getUserName();
        String gender = getGender();
        String mobileNo = getUserMobileNo();
        String emailId = getUserEmail();
        String createdOn = getCreatedOn();
        String profileImgUrl = getProfileImgUrl();
        String myInterconnectionsDocId = getMyInterconnectionsDocId();

        return new UserDetailsResponse(userId, fcmToken, name, gender, emailId,
                mobileNo, createdOn, profileImgUrl, myInterconnectionsDocId);
    }

    public static String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public static String getFcmToken() {
        return sharedPreferences.getString(KEY_FCM_TOKEN, null);
    }

    public static String getUserFirstName() {
        return sharedPreferences.getString(KEY_USER_FIRST_NAME, null);
    }

    public static String getUserLastName() {
        return sharedPreferences.getString(KEY_USER_LAST_NAME, null);
    }

    public static String getUserName() {
        String firstName = getUserFirstName();
        String lastName = getUserLastName();
        if (lastName == null) {
            return firstName;
        }
        return firstName + " " + lastName;
    }

    public static String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }

    public static String getUserMobileNo() {
        return sharedPreferences.getString(KEY_USER_MOBILE_NO, null);
    }

    public static String getMyInterconnectionsDocId() {
        return sharedPreferences.getString(KEY_MY_INTERCONNECTIONS_DOC_ID, null);
    }

    public static String getGender() {
        return sharedPreferences.getString(KEY_GENDER, null);
    }

    public static String getCreatedOn() {
        return sharedPreferences.getString(KEY_CREATED_ON, null);
    }

    public static String getProfileImgUrl() {
        return sharedPreferences.getString(KEY_PROFILE_IMG_URL, null);
    }

    public static void setFcmToken(String token) {
        editor.putString(KEY_FCM_TOKEN, token);
        editor.apply();
    }

    public static void logoutUser(Context context) {
        clearSessionManager();
        FirebaseClients.destroyAllFirebaseClients();
        redirectToSignUpActivity(context);
    }

    public static void clearSessionManager() {
        // Clearing all data from Shared Preferences
        try {
            editor.clear();
            editor.commit();
        } catch (Exception e) {
            //
        }
    }

    private static void redirectToSignUpActivity(Context context) {
        Intent intent = new Intent(context, SignUpActivity.class);
        // Starting registration Activity
        context.startActivity(intent);
    }
}
