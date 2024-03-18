package com.example.sswhatsapp.utils;

public class FirestoreHelper {
    public static String createConnectionName(String userId1, String userId2) {
        return userId1 + "_&_" + userId2;
    }

    public static String createParticipantName(String userId) {
        return "Participant_" + userId;
    }
}
