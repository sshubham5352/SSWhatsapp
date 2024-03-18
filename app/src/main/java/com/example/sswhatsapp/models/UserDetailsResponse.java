package com.example.sswhatsapp.models;

import com.example.sswhatsapp.firebase.FirebaseConstants;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;

public class UserDetailsResponse implements Serializable {
    //fields
    @PropertyName(FirebaseConstants.KEY_USER_ID)
    public String userId;
    @PropertyName(FirebaseConstants.KEY_USER_NAME)
    public String name;
    @PropertyName(FirebaseConstants.KEY_USER_GENDER)
    public String gender;
    @PropertyName(FirebaseConstants.KEY_USER_EMAIL_ID)
    public String emailId;
    @PropertyName(FirebaseConstants.KEY_USER_MOBILE_NO)
    public String mobileNo;
    @PropertyName(FirebaseConstants.KEY_USER_PROFILE_IMG_URL)
    public String imgProfile;
    @PropertyName(FirebaseConstants.KEY_USER_TAGLINE)
    public String tagline;
    @PropertyName(FirebaseConstants.KEY_ACCOUNT_CREATED_ON)
    public String createdOn;
    @PropertyName(FirebaseConstants.KEY_CONNECTIONS_REF)
    public String myConnectionsListRef;

    public UserDetailsResponse() {
        /*
         * Empty Constructor
         * required for firestore to convert document to pojo java class
         */
    }

    public UserDetailsResponse(String userId, String name, String gender, String emailId, String mobileNo, String createdOn, String imgProfile) {
        this.userId = userId;
        this.gender = gender;
        this.name = name;
        this.emailId = emailId;
        this.mobileNo = mobileNo;
        this.imgProfile = imgProfile;
        this.createdOn = createdOn;
        this.tagline = "Hey there I'm using SS WhatsApp!";
    }

    public String getUserId() {
        return userId;
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

    public String getImgProfile() {
        return imgProfile;
    }

    public String getTagline() {
        return tagline;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public String getMyConnectionsListRef() {
        return myConnectionsListRef;
    }

    public void setMyConnectionsListRef(String myConnectionsListRef) {
        this.myConnectionsListRef = myConnectionsListRef;
    }
}
