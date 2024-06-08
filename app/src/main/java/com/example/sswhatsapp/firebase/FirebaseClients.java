package com.example.sswhatsapp.firebase;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class FirebaseClients {
    public static FirebaseFirestore firestoreDb;
    public static FirebaseDatabase realtimeDb;
    public static FirebaseStorage firebaseStorage;

    //CONSTRUCTOR
    private FirebaseClients() {
        //private empty constructor
    }

    public static FirebaseFirestore getFirestoreDb() {
        if (firestoreDb == null) {
            firestoreDb = FirebaseFirestore.getInstance();
        }
        return firestoreDb;
    }

    public static FirebaseDatabase getRealtimeDb() {
        if (realtimeDb == null) {
            realtimeDb = FirebaseDatabase.getInstance();
        }
        return realtimeDb;
    }

    public static FirebaseStorage getFirebaseStorage() {
        if (firebaseStorage == null) {
            firebaseStorage = FirebaseStorage.getInstance();
        }
        return firebaseStorage;
    }

    public static void destroyAllFirebaseClients() {
        firestoreDb = null;
        realtimeDb = null;
        firebaseStorage = null;
    }
}
