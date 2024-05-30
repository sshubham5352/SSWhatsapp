package com.example.sswhatsapp.models;

import com.example.sswhatsapp.firebase.FirebaseConstants;
import com.example.sswhatsapp.utils.FirestoreHelper;
import com.google.firebase.firestore.PropertyName;

public class ConnectionParticipantResponse {
    //fields
    @PropertyName(FirebaseConstants.KEY_DOC_ID)
    public String docId;
    @PropertyName(FirebaseConstants.KEY_USER_ID)
    public String userId;
    @PropertyName(FirebaseConstants.KEY_IS_TYPING)
    public boolean isTyping;
    @PropertyName(FirebaseConstants.KEY_IS_LIVE)
    public boolean isLive;


    public ConnectionParticipantResponse() {
        /*
         * Empty Constructor
         * required for firestore to convert document to pojo java class
         */
    }


    public ConnectionParticipantResponse(String userId, boolean isTyping, boolean isLive) {
        this.userId = userId;
        this.isTyping = isTyping;
        this.isLive = isLive;
        this.docId = FirestoreHelper.createParticipantName(userId);
    }

    public String getDocId() {
        return docId;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isTyping() {
        return isTyping;
    }

    public boolean isLive() {
        return isLive;
    }

    public void setTyping(boolean typing) {
        isTyping = typing;
    }

    public void setLive(boolean live) {
        isLive = live;
    }
}
