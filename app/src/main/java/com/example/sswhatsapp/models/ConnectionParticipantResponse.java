package com.example.sswhatsapp.models;

import com.example.sswhatsapp.firebase.FirebaseConstants;
import com.google.firebase.firestore.PropertyName;

public class ConnectionParticipantResponse {
    //fields
    @PropertyName(FirebaseConstants.KEY_USER_ID)
    public String userId;
    @PropertyName(FirebaseConstants.KEY_IS_TYPING)
    public boolean isTyping;

    public ConnectionParticipantResponse() {
        /*
         * Empty Constructor
         * required for firestore to convert document to pojo java class
         */
    }


    public ConnectionParticipantResponse(String userId, boolean isTyping) {
        this.userId = userId;
        this.isTyping = isTyping;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isTyping() {
        return isTyping;
    }
}
