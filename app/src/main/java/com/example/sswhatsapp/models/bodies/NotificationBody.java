package com.example.sswhatsapp.models.bodies;


import com.google.firebase.firestore.PropertyName;

import java.util.HashMap;

public class NotificationBody {
    //FIELDS
    @PropertyName("message")
    public NotificationData message;

    public NotificationBody() {
        //empty default constructor
    }

    public NotificationBody(NotificationData message) {
        this.message = message;
    }

    public static class NotificationData {
        @PropertyName("token")
        public String token;
        //        @PropertyName("notification")
//
//        public Notification notification;
        @PropertyName("data")
        public HashMap<String, String> data;

        public NotificationData() {
            //empty default constructor
        }

        public NotificationData(String token, HashMap<String, String> data) {
            this.token = token;
            this.data = data;
        }
    }
}
