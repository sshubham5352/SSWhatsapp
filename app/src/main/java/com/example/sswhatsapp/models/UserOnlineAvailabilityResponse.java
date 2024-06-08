package com.example.sswhatsapp.models;

import com.example.sswhatsapp.firebase.FirebaseConstants;
import com.google.firebase.database.PropertyName;

public class UserOnlineAvailabilityResponse {
    //fields
    @PropertyName(FirebaseConstants.KEY_IS_ONLINE)
    public boolean isOnline;
    @PropertyName(FirebaseConstants.KEY_LAST_ONLINE)
    public long lastOnline;

    public UserOnlineAvailabilityResponse() {
        //empty constructor
    }

    public boolean isOnline() {
        return isOnline;
    }

    public long getLastOnlineTime() {
        return lastOnline;
    }

    public void set(UserOnlineAvailabilityResponse response) {
        isOnline = response.isOnline;
        lastOnline = response.lastOnline;
    }
}
