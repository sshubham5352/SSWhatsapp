package com.example.sswhatsapp.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.Person;
import androidx.core.graphics.drawable.IconCompat;

import com.example.sswhatsapp.R;
import com.example.sswhatsapp.activities.ChatWithIndividualActivity;
import com.example.sswhatsapp.firebase.FirebaseConstants;
import com.example.sswhatsapp.firebase.FirestoreManager;
import com.example.sswhatsapp.firebase.FirestoreNetworkCallListener;
import com.example.sswhatsapp.models.ChatItemResponse;
import com.example.sswhatsapp.models.UserDetailsResponse;
import com.example.sswhatsapp.providers.ContactsProvider;
import com.example.sswhatsapp.utils.Constants;
import com.example.sswhatsapp.utils.PicassoCache;
import com.example.sswhatsapp.utils.SessionManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;
import java.util.HashMap;

public class FCMService extends FirebaseMessagingService implements FirestoreNetworkCallListener {

    //STATIC FIELDS
    public static final String GROUP_KEY_WORK_EMAIL = "com.example.sswhatsapp";
    public static int NOTIFICATION_ID = 601;
    public static final int GROUP_SUMMERY_ID = 101;
    public static final String CHAT_CHANNEL_ID = "chat_message";

    private NotificationCompat.Builder notificationBuilder;
    private NotificationCompat.MessagingStyle messagingStyle;
    private HashMap<String, Person> senderList;
    private FirestoreManager firestoreManager = new FirestoreManager(this);


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

        UserDetailsResponse sender = new UserDetailsResponse(message.getData());
        ChatItemResponse chatItem = new ChatItemResponse(message.getData());

        Intent intent = new Intent(this, ChatWithIndividualActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.INTENT_USER_DETAILS_EXTRA, "notificationSender");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        showMsgChatNotification(sender, chatItem, pendingIntent);
    }


    private void showMsgChatNotification(UserDetailsResponse sender, ChatItemResponse chatItem, PendingIntent pendingIntent) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        }

        if (notificationBuilder == null) {
            createNotificationChannel();

            notificationBuilder = new NotificationCompat.Builder(this, CHAT_CHANNEL_ID)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setColor(getColor(R.color.colorAssetGreen))
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            Person personSender = new Person.Builder()
                    .setName(ContactsProvider.getContactName(sender.getMobileNo(), this))
                    .setIcon(IconCompat.createWithBitmap(PicassoCache.getBitmap(this, getResources(), sender.profileImgUrl)))
                    .build();


            senderList = new HashMap<>();
            senderList.put(sender.userId, personSender);
            messagingStyle = new NotificationCompat.MessagingStyle(personSender).setGroupConversation(true);
            messagingStyle.addMessage(chatItem.message, new Date().getTime(), personSender);
            messagingStyle.setConversationTitle("1 new message");
            notificationBuilder.setStyle(messagingStyle);
        } else {
            messagingStyle.addMessage(chatItem.message, new Date().getTime(), senderList.get(sender.userId));
            notificationBuilder.setWhen(new Date().getTime());
        }

        NotificationManagerCompat.from(this).notify(getString(R.string.app_name), NOTIFICATION_ID, notificationBuilder.build());
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(CHAT_CHANNEL_ID, "SSWhatsapp",
                NotificationManager.IMPORTANCE_HIGH);   // for heads-up notifications
        channel.setDescription("description");

        // Register channel with system
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public void onFirestoreNetworkCallSuccess(Object response, int serviceCode) {
        switch (serviceCode) {
            case FirebaseConstants.UPDATE_FIELD_FCM_TOKEN: {
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
            case FirebaseConstants.UPDATE_FIELD_FCM_TOKEN: {
                SessionManager.clearSessionManager();
                break;
            }
        }
    }

    @Override
    public void onFirestoreNetworkCallFailure(String errorMessage) {

    }
/*
    private void showMsgChatNotification(UserDetailsResponse sender, ChatItemResponse chatItem, PendingIntent pendingIntent) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        }

        if (notificationBuilder == null) {
            createNotificationChannel();

            notificationBuilder = new NotificationCompat.Builder(this, CHAT_CHANNEL_ID);
            notificationBuilder
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setColor(getColor(R.color.colorAssetGreen))
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            Person personSender = new Person.Builder()
                    .setName(ContactsProvider.getContactName(sender.getMobileNo(), this))
                    .setIcon(IconCompat.createWithBitmap(PicassoCache.getBitmap(this, getResources(), sender.profileImgUrl)))
                    .build();

            senderList = new HashMap<>();
            senderList.put(sender.userId, personSender);
            messagingStyle = new NotificationCompat.MessagingStyle(personSender).setGroupConversation(true);
            messagingStyle.addMessage(chatItem.message, new Date().getTime(), personSender);
            notificationBuilder.setStyle(messagingStyle);
        } else {
            messagingStyle.addMessage(chatItem.message, new Date().getTime(), senderList.get(sender.userId));
        }


        NotificationManagerCompat.from(this).notify(getString(R.string.app_name), NOTIFICATION_ID, notificationBuilder.build());
    }*/
}











