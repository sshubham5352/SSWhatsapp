package com.example.sswhatsapp.models;

import com.example.sswhatsapp.firebase.FirebaseConstants;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.Map;

public class UserDetailsResponse implements Serializable {
    //fields
    @PropertyName(FirebaseConstants.KEY_USER_ID)
    public String userId;
    @PropertyName(FirebaseConstants.KEY_FCM_TOKEN)
    public String fcmToken;
    @PropertyName(FirebaseConstants.KEY_USER_NAME)
    public String name;
    @PropertyName(FirebaseConstants.KEY_USER_GENDER)
    public String gender;
    @PropertyName(FirebaseConstants.KEY_USER_EMAIL_ID)
    public String emailId;
    @PropertyName(FirebaseConstants.KEY_USER_MOBILE_NO)
    public String mobileNo;
    @PropertyName(FirebaseConstants.KEY_USER_PROFILE_IMG_URL)
    public String profileImgUrl;
    @PropertyName(FirebaseConstants.KEY_USER_TAGLINE)
    public String tagline;
    @PropertyName(FirebaseConstants.KEY_ACCOUNT_CREATED_ON)
    public String createdOn;
    @PropertyName(FirebaseConstants.KEY_MY_INTERCONNECTIONS_DOC_ID)
    public String myInterconnectionsDocId;

    //CONSTRUCTOR: for json conversion
    public UserDetailsResponse() {
        /*
         * Empty Constructor
         * required for firestore to convert document to pojo java class
         */
    }

    //CONSTRUCTOR: for new chat msg
    public UserDetailsResponse(String userId, String fcmToken, String name, String gender, String emailId, String mobileNo, String createdOn, String profileImgUrl) {
        this.userId = userId;
        this.fcmToken = fcmToken;
        this.gender = gender;
        this.name = name;
        this.emailId = emailId;
        this.mobileNo = mobileNo;
        this.profileImgUrl = profileImgUrl;
        this.createdOn = createdOn;
        this.tagline = "Hey there I'm using SS WhatsApp!";
    }

    //CONSTRUCTOR: for session manager
    public UserDetailsResponse(String userId, String fcmToken, String name, String mobileNo, String emailId, String profileImgUrl, String myInterconnectionsDocId) {
        this.userId = userId;
        this.fcmToken = fcmToken;
        this.name = name;
        this.mobileNo = mobileNo;
        this.emailId = emailId;
        this.profileImgUrl = profileImgUrl;
        this.myInterconnectionsDocId = myInterconnectionsDocId;
    }

    //CONSTRUCTOR: for FCM service
    public UserDetailsResponse(Map<String, String> dataMap) {
        userId = dataMap.get(FirebaseConstants.KEY_USER_ID);
        mobileNo = dataMap.get(FirebaseConstants.KEY_USER_MOBILE_NO);
        profileImgUrl = dataMap.get(FirebaseConstants.KEY_USER_PROFILE_IMG_URL);
    }

    public String getUserId() {
        return userId;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }

    public String getEmailId() {
        return emailId;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public String getProfileImgUrl() {
        return profileImgUrl;
    }

    public String getTagline() {
        return tagline;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public String getMyInterconnectionsDocId() {
        return myInterconnectionsDocId;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public void setProfileImgUrl(String profileImgUrl) {
        this.profileImgUrl = profileImgUrl;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMyInterconnectionsDocId(String myInterconnectionsDocId) {
        this.myInterconnectionsDocId = myInterconnectionsDocId;
    }
}
