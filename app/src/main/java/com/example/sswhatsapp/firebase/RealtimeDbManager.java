package com.example.sswhatsapp.firebase;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class RealtimeDbManager {

    private final FirebaseDatabase realtimeDb;
    private RealtimeDbNetworkCallListener mListener;

    //CONSTRUCTOR
    public RealtimeDbManager() {
        realtimeDb = FirebaseClients.getRealtimeDb();
    }


    //CONSTRUCTOR
    public RealtimeDbManager(RealtimeDbNetworkCallListener listener) {
        realtimeDb = FirebaseClients.getRealtimeDb();
        mListener = listener;
    }

    //ATTACHING LISTENER
    public void setUserAvailabilityListener(String userId, ValueEventListener valueEventListener) {
        realtimeDb
                .getReference(FirebaseConstants.ROOT_USERS)
                .child(userId)
                .addValueEventListener(valueEventListener);
    }


    public void createNewUser(String userId) {
        realtimeDb
                .getReference(FirebaseConstants.ROOT_USERS)
                .child(userId)
                .child(FirebaseConstants.KEY_IS_ONLINE)
                .setValue(true);
        realtimeDb
                .getReference(FirebaseConstants.ROOT_USERS)
                .child(userId)
                .child(FirebaseConstants.KEY_LAST_ONLINE)
                .setValue(ServerValue.TIMESTAMP);
    }

    public void updateMyOnlineStatus(boolean isOnline, String userId) {
        realtimeDb
                .getReference(FirebaseConstants.ROOT_USERS)
                .child(userId)
                .child(FirebaseConstants.KEY_IS_ONLINE)
                .setValue(isOnline);

        if (!isOnline) {
            realtimeDb
                    .getReference(FirebaseConstants.ROOT_USERS)
                    .child(userId)
                    .child(FirebaseConstants.KEY_LAST_ONLINE)
                    .setValue(ServerValue.TIMESTAMP);
        }
    }

    public void updateMyOnlineStatusOnDisconnect(String userId) {
        realtimeDb
                .getReference(FirebaseConstants.ROOT_USERS)
                .child(userId)
                .child(FirebaseConstants.KEY_IS_ONLINE)
                .onDisconnect()
                .setValue(false);

        realtimeDb
                .getReference(FirebaseConstants.ROOT_USERS)
                .child(userId)
                .child(FirebaseConstants.KEY_LAST_ONLINE)
                .onDisconnect()
                .setValue(ServerValue.TIMESTAMP);

        realtimeDb.getReference(FirebaseConstants.ROOT_USERS).keepSynced(true);
    }

    //REMOVING LISTENER
    public void removeUserAvailabilityListener(String userId, ValueEventListener userAvailabilityListener) {
        realtimeDb
                .getReference(FirebaseConstants.ROOT_USERS)
                .child(userId)
                .removeEventListener(userAvailabilityListener);
    }
}
