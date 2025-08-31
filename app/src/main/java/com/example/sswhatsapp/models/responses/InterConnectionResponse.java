package com.example.sswhatsapp.models.responses;

import androidx.annotation.NonNull;

import com.example.sswhatsapp.firebase.FirebaseConstants;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;

public class InterConnectionResponse implements Serializable {
    //fields
    @PropertyName(FirebaseConstants.KEY_CONNECTION_ID)
    public String connectionId;
    @PropertyName(FirebaseConstants.KEY_CONNECTION_WITH)
    public String connectionWith;
    @PropertyName(FirebaseConstants.KEY_IS_ERADICATED)
    public boolean isEradicated;
    @PropertyName(FirebaseConstants.KEY_MODIFIED_AT)
    public String modifiedAt;


    //CONSTRUCTOR
    public InterConnectionResponse() {
        /*
         * Empty Constructor
         * required for firestore to convert document to pojo java class
         */
    }

    //CONSTRUCTOR
    public InterConnectionResponse(String connectionId, String connectionWith, boolean isEradicated, String modifiedAt) {
        this.connectionId = connectionId;
        this.connectionWith = connectionWith;
        this.isEradicated = isEradicated;
        this.modifiedAt = modifiedAt;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public String getConnectionWith() {
        return connectionWith;
    }

    public String getModifiedAt() {
        return modifiedAt;
    }

    public boolean isEradicated() {
        return isEradicated;
    }

    public void setEradicated(boolean eradicated) {
        isEradicated = eradicated;
    }


    @NonNull
    @Override
    public String toString() {
        return "ConnectionRef: " + connectionId +
                "Connection With: " + connectionWith +
                "Is Eradicated: " + isEradicated +
                "Modified at: " + modifiedAt;
    }

    public static class CustomMyConnectionResponse {
        QuerySnapshot querySnapshot;
        String connectionWith;

        public CustomMyConnectionResponse(QuerySnapshot querySnapshot, String connectionWith) {
            this.querySnapshot = querySnapshot;
            this.connectionWith = connectionWith;
        }

        public QuerySnapshot getQuerySnapshot() {
            return querySnapshot;
        }

        public String getConnectionWith() {
            return connectionWith;
        }

        public void setQuerySnapshot(QuerySnapshot querySnapshot) {
            this.querySnapshot = querySnapshot;
        }

        public void setConnectionWith(String connectionWith) {
            this.connectionWith = connectionWith;
        }
    }
}
