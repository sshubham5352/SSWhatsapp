package com.example.sswhatsapp.services;

import androidx.annotation.NonNull;

import com.example.sswhatsapp.firebase.FirebaseConstants;
import com.example.sswhatsapp.firebase.FirestoreNetworkCallListener;
import com.example.sswhatsapp.notificatons.ChatNotificationsManager;
import com.example.sswhatsapp.retrofit.RetrofitConstants;
import com.example.sswhatsapp.utils.SessionManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCMService extends FirebaseMessagingService implements FirestoreNetworkCallListener {

    //STATIC FIELDS
    public static ChatNotificationsManager chatNotificationsManager = null;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // TODO: 07-04-2024  /*
        //  send new fcm token to server
        //  and save to SessionManager too
        //  */
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        String notificationType = message.getData().get(RetrofitConstants.NOTIFICATION_TYPE);

        switch (Integer.parseInt(notificationType)) {
            case RetrofitConstants.SEND_CHAT_NOTIFICATION_CALL: {
                getChatNotificationsManager().chatMessageReceived(message);
                break;
            }
        }
    }


    public ChatNotificationsManager getChatNotificationsManager() {
        if (chatNotificationsManager == null) {
            chatNotificationsManager = new ChatNotificationsManager(this, getApplicationContext(), getResources());
        }
        return chatNotificationsManager;
    }

    public static void clearNotification(String userId) {
        if (chatNotificationsManager != null) {
            chatNotificationsManager.clearNotification(userId);
        }
    }

    public static void clearAllNotifications() {
        if (chatNotificationsManager != null) {
            chatNotificationsManager.clearAllNotifications();
        }
    }

    @Override
    public void onFirestoreNetworkCallSuccess(Object response, int serviceCode) {
        switch (serviceCode) {
            case FirebaseConstants.UPDATE_FIELD_FCM_TOKEN_CALL: {
                // TODO: 06-04-2024
                /*
                 *Change in Session Manager
                 *  */
                break;
            }
        }
    }

    @Override
    public void onFirestoreNetworkCallFailure(Object response, int serviceCode) {
        switch (serviceCode) {
            case FirebaseConstants.UPDATE_FIELD_FCM_TOKEN_CALL: {
                SessionManager.clearSessionManager();
                break;
            }
        }
    }

    @Override
    public void onFirestoreNetworkCallFailure(String errorMessage) {

    }
}
