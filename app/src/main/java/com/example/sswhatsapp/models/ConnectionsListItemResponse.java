package com.example.sswhatsapp.models;

import androidx.annotation.NonNull;

import com.example.sswhatsapp.firebase.FirebaseConstants;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.QuerySnapshot;

public class ConnectionsListItemResponse {
    //fields
    @PropertyName(FirebaseConstants.KEY_CONNECTIONS_REF)
    public String connectionId;
    @PropertyName(FirebaseConstants.KEY_CONNECTION_WITH)
    public String connectionWith;
    @PropertyName(FirebaseConstants.KEY_IS_ERADICATED)
    public boolean isEradicated;


    public ConnectionsListItemResponse() {
        /*
         * Empty Constructor
         * required for firestore to convert document to pojo java class
         */
    }

    public ConnectionsListItemResponse(String connectionId, String connectionWith, boolean isEradicated) {
        this.connectionId = connectionId;
        this.connectionWith = connectionWith;
        this.isEradicated = isEradicated;
    }

    public ConnectionsListItemResponse(String connectionId, String connectionWith) {
        this.connectionId = connectionId;
        this.connectionWith = connectionWith;
        this.isEradicated = true;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public String getConnectionWith() {
        return connectionWith;
    }

    public boolean isEradicated() {
        return isEradicated;
    }

    @NonNull
    @Override
    public String toString() {
        return "ConnectionRef: " + connectionId +
                "Connection With: " + connectionWith +
                "Is Eradicated: " + isEradicated;
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
