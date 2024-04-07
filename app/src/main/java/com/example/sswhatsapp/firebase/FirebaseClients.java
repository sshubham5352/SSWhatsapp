package com.example.sswhatsapp.firebase;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class FirebaseClients {
    public static FirebaseFirestore firestoreDb;
    public static FirebaseStorage firebaseStorage;

    private FirebaseClients() {
        //private empty constructor for singleton approach
    }

    public static FirebaseFirestore getFirestoreDb() {
        if (firestoreDb == null)
            firestoreDb = FirebaseFirestore.getInstance();

        return firestoreDb;
    }

    public static FirebaseStorage getFirebaseStorage() {
        if (firebaseStorage == null)
            firebaseStorage = FirebaseStorage.getInstance();

        return firebaseStorage;
    }

    public static void destroyAllFirebaseClients() {
        firestoreDb = null;
        firebaseStorage = null;
    }
}
