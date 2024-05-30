package com.example.sswhatsapp.notificatons;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;
import androidx.core.app.RemoteInput;
import androidx.core.graphics.drawable.IconCompat;

import com.example.sswhatsapp.R;
import com.example.sswhatsapp.activities.ChatWithIndividualActivity;
import com.example.sswhatsapp.broadcastreceivers.NotificationActionsBroadcastReceiver;
import com.example.sswhatsapp.firebase.FirebaseConstants;
import com.example.sswhatsapp.firebase.FirestoreManager;
import com.example.sswhatsapp.firebase.FirestoreNetworkCallListener;
import com.example.sswhatsapp.models.ChatItemResponse;
import com.example.sswhatsapp.models.InterConnection;
import com.example.sswhatsapp.models.UserDetailsResponse;
import com.example.sswhatsapp.providers.ContactsProvider;
import com.example.sswhatsapp.retrofit.RetrofitConstants;
import com.example.sswhatsapp.retrofit.RetrofitManager;
import com.example.sswhatsapp.retrofit.RetrofitNetworkCallListener;
import com.example.sswhatsapp.utils.Constants;
import com.example.sswhatsapp.utils.FirestoreHelper;
import com.example.sswhatsapp.utils.Helper;
import com.example.sswhatsapp.utils.PicassoCache;
import com.example.sswhatsapp.utils.SessionManager;
import com.example.sswhatsapp.utils.TimeHandler;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatNotificationsManager implements FirestoreNetworkCallListener, RetrofitNetworkCallListener {
    //STATIC FIELDS
    private final Context mContext;
    private final Resources mResources;
    private final UserDetailsResponse myUserDetails;
    private NotificationCompat.Builder notificationBuilder;
    private final NotificationManager notificationManager;
    private final FirestoreManager firestoreManager;
    private final RetrofitManager retrofitManager;
    HashMap<String, ChatNotificationItem> notificationsList;
    public static int notificationIdTrack = 401;

    //CONSTRUCTOR
    public ChatNotificationsManager(Context context, Context applicationContext, Resources resources) {
        mContext = context;
        mResources = resources;
        notificationsList = new HashMap<>();
        firestoreManager = new FirestoreManager(this);
        retrofitManager = new RetrofitManager(this);
        notificationManager = mContext.getSystemService(NotificationManager.class);

        initNotificationsChannel();
        initNotificationBuilder();
        SessionManager.initSessionManager(applicationContext);
        myUserDetails = SessionManager.getUser();
    }


    private void initNotificationsChannel() {
        NotificationChannel channel = new NotificationChannel(Constants.CHAT_CHANNEL_ID, "SSWhatsapp",
                NotificationManager.IMPORTANCE_HIGH);   // for heads-up notifications
        channel.setDescription("Chats Notifications");
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        notificationManager.createNotificationChannel(channel);
    }

    private void initNotificationBuilder() {
        notificationBuilder = new NotificationCompat.Builder(mContext, Constants.CHAT_CHANNEL_ID)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.app_notification_icon)
                .setColor(mContext.getColor(R.color.colorAssetGreen))
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
    }

    private void addActionBtnsInNotification(int notificationId, String userId) {
        RemoteInput remoteInput = new RemoteInput.Builder(Constants.KEY_CHAT_REPLY)
                .setLabel("Type here..")
                .build();


        // Pending intent for the "reply" action to trigger
        PendingIntent replyActionPendingIntent = PendingIntent.getBroadcast(
                mContext,
                Constants.NOTIFICATION_ACTION_REPLY,
                new Intent(mContext, NotificationActionsBroadcastReceiver.class)
                        .putExtra(Constants.NOTIFICATION_ACTION_ID, Constants.NOTIFICATION_ACTION_REPLY)
                        .putExtra(FirebaseConstants.KEY_USER_ID, userId),
                PendingIntent.FLAG_UPDATE_CURRENT);

        //Pending intent for "mark as read" action to trigger
        PendingIntent markAsReadPendingIntent = PendingIntent.getBroadcast(
                mContext,
                Constants.NOTIFICATION_ACTION_MARK_AS_READ,
                new Intent(mContext, NotificationActionsBroadcastReceiver.class)
                        .putExtra(Constants.NOTIFICATION_ACTION_ID, Constants.NOTIFICATION_ACTION_MARK_AS_READ)
                        .putExtra(FirebaseConstants.KEY_USER_ID, userId),
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.img_chat_icon, "Reply", replyActionPendingIntent)
                .addRemoteInput(remoteInput)
                .build();

        notificationBuilder.clearActions();
        notificationBuilder.addAction(action);
        notificationBuilder.addAction(R.drawable.img_chat_icon, "Mark as read", markAsReadPendingIntent);
    }

    public void chatMessageReceived(RemoteMessage message) {
        if (!checkNotificationPermission()) {
            return;
        }

        UserDetailsResponse userSender = new UserDetailsResponse(message.getData());
        ChatItemResponse chatItem = new ChatItemResponse(message.getData());
        String connectionId = message.getData().get(FirebaseConstants.KEY_CONNECTION_ID);
        ChatNotificationItem notificationItem = notificationsList.get(userSender.getUserId());

        if (notificationItem == null) {
            //Notification does not exist
            notificationItem = new ChatNotificationItem(mContext, mResources, userSender, connectionId);
            notificationsList.put(userSender.getUserId(), notificationItem);
            addActionBtnsInNotification(notificationItem.notificationId, userSender.getUserId());
        }
        notificationItem.addChatItem(chatItem, false);

        notificationBuilder.setWhen(TimeHandler.getCurrentTime());
        notificationBuilder.setStyle(notificationItem.messagingStyle);
        notificationBuilder.setContentIntent(notificationItem.pendingIntent);
        notificationManager.notify(Constants.NOTIFICATIONS_TAG, notificationItem.notificationId, notificationBuilder.build());

        updateChatStatusAsReceived(chatItem.chatId, connectionId);
    }

    private boolean checkNotificationPermission() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(mContext, "Notification permission not given to SSWhatsapp!", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    //CALL FROM BROADCAST RECEIVER
    public void actionReplyMsg(String replyMsg, String notificationSenderId) {
        if (!checkNotificationPermission()) {
            return;
        }
        ChatItemResponse chatItem = new ChatItemResponse(Constants.CHAT_CATEGORY_MSG,
                Constants.CHAT_STATUS_SENT,
                myUserDetails.getUserId(),
                notificationSenderId,
                replyMsg,
                TimeHandler.getCurrentTimeStamp(),
                false, false, false);

        ChatNotificationItem notificationItem = notificationsList.get(notificationSenderId);
        sendChatMsg(chatItem, notificationItem.connectionId);

        NotificationRequirements notificationRequirementsObject = new NotificationRequirements(chatItem, notificationItem.userSender.getFcmToken(), notificationItem.connectionId);
        checkIsParticipantLiveOnChat(notificationSenderId, notificationItem.connectionId, notificationRequirementsObject);
    }


    //CALL FROM BROADCAST RECEIVER
    public void actionMarkAsRead(String notificationSenderId) {
        ChatNotificationItem notificationItem = notificationsList.get(notificationSenderId);
        ArrayList<String> chatIdsList = notificationItem.getReceivedChatsIds();

        updateChatsStatusAsRead(chatIdsList, notificationItem.connectionId, notificationSenderId);
    }

    public void clearNotification(String userId) {
        ChatNotificationItem notificationItem = notificationsList.remove(userId);
        if (notificationItem != null) {
            notificationManager.cancel(Constants.NOTIFICATIONS_TAG, notificationItem.notificationId);
        }
    }

    public void clearAllNotifications() {
        /*
         * Removes all notifications from notification bar
         * */
        notificationManager.cancelAll();
    }

    //NETWORK CALL
    public void sendChatMsg(ChatItemResponse chatItem, String connectionId) {
        firestoreManager.sendMessageChat(connectionId, chatItem);
    }

    //NETWORK CALL
    public void checkIsParticipantLiveOnChat(String userId, String connectionId, NotificationRequirements notificationRequirementsObject) {
        firestoreManager.isParticipantLiveOnChat(FirestoreHelper.createParticipantName(userId), connectionId, notificationRequirementsObject);
    }

    //RETROFIT CALL
    public void sendNotification(ChatItemResponse chatItem, String receiverFcmToken, String connectionId) {
        if (Helper.isNill(receiverFcmToken)) {
            /*
             * As FCM token of receiver's device in null so can't send the notification
             **/
            return;
        }
        JSONObject dataMap = new JSONObject();
        try {
            dataMap.put(RetrofitConstants.NOTIFICATION_TYPE, Integer.toString(RetrofitConstants.SEND_CHAT_NOTIFICATION_CALL));
            dataMap.put(FirebaseConstants.KEY_FCM_TOKEN, myUserDetails.getFcmToken());
            dataMap.put(FirebaseConstants.KEY_USER_ID, myUserDetails.getUserId());                     //notification sender ID
            dataMap.put(FirebaseConstants.KEY_USER_NAME, myUserDetails.getName());
            dataMap.put(FirebaseConstants.KEY_SENDER_ID, myUserDetails.getUserId());
            dataMap.put(FirebaseConstants.KEY_USER_GENDER, myUserDetails.getGender());
            dataMap.put(FirebaseConstants.KEY_USER_MOBILE_NO, myUserDetails.getMobileNo());
            dataMap.put(FirebaseConstants.KEY_USER_PROFILE_IMG_URL, myUserDetails.getProfileImgUrl());
            dataMap.put(FirebaseConstants.KEY_CONNECTION_ID, connectionId);
            dataMap.put(FirebaseConstants.KEY_CHAT_ID, chatItem.getChatId());
            dataMap.put(FirebaseConstants.KEY_CHAT_CATEGORY, Integer.toString(chatItem.getChatCategory()));
            dataMap.put(FirebaseConstants.KEY_CHAT_MESSAGE, chatItem.getMessage());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        retrofitManager.sendChatNotificationCall(receiverFcmToken, dataMap);
    }


    //NETWORK CALL
    private void updateChatStatusAsReceived(String chatId, String connectionId) {
        firestoreManager.updateChatStatus(Constants.CHAT_STATUS_RECEIVED, chatId, connectionId);
    }

    //NETWORK CALL
    public void updateChatsStatusAsRead(ArrayList<String> chatIdsList, String connectionId, String notificationSenderId) {
        firestoreManager.updateChatsStatus(Constants.CHAT_STATUS_READ, chatIdsList, connectionId, notificationSenderId);
    }


    @Override
    public void onFirestoreNetworkCallSuccess(Object response, int serviceCode) {
        switch (serviceCode) {
            case FirebaseConstants.SEND_MESSAGE_CHAT_CALL: {
                ChatItemResponse chatItem = (ChatItemResponse) response;
                ChatNotificationItem notificationItem = notificationsList.get(chatItem.getReceiverId());
                notificationItem.addChatItem(chatItem, true);
                notificationBuilder.setWhen(TimeHandler.getCurrentTime());
                notificationBuilder.setContentIntent(notificationItem.pendingIntent);
                notificationManager.notify(Constants.NOTIFICATIONS_TAG, notificationItem.notificationId, notificationBuilder.build());
                break;
            }

            case FirebaseConstants.UPDATE_CHATS_STATUS_CALL: {
                String notificationSenderId = (String) response;
                clearNotification(notificationSenderId);
                break;
            }

            case FirebaseConstants.IS_PARTICIPANT_LIVE_ON_CHAT_CALL: {
                NotificationRequirements notificationRequirementsObject = (NotificationRequirements) response;
                if (!notificationRequirementsObject.isParticipantLive()) {
                    sendNotification(notificationRequirementsObject.chatItem, notificationRequirementsObject.receiverFcmToken, notificationRequirementsObject.connectionId);
                }
                break;
            }
        }
    }

    @Override
    public void onFirestoreNetworkCallFailure(Object response, int serviceCode) {
    }

    @Override
    public void onFirestoreNetworkCallFailure(String errorMessage) {
        Log.d(FirebaseConstants.NETWORK_CALL, errorMessage);
    }

    @Override
    public void onRetrofitNetworkCallSuccess(Object response, int serviceCode) {
        switch (serviceCode) {
            case RetrofitConstants.SEND_CHAT_NOTIFICATION_CALL: {
                Log.d(RetrofitConstants.NETWORK_CALL, "onRetrofitNetworkCallSuccess: Notification sent successfully");
                break;
            }
        }
    }

    @Override
    public void onRetrofitNetworkCallFailure(String errorMessage) {

    }

    private static class ChatNotificationItem {
        //DATA FIELDS
        final Context mContext;
        final Resources mResources;
        final int notificationId;
        final UserDetailsResponse userSender;
        Person personSender;
        NotificationCompat.MessagingStyle messagingStyle;
        PendingIntent pendingIntent;
        String connectionId;
        final ArrayList<ChatItemResponse> chatList;


        //CONSTRUCTOR
        public ChatNotificationItem(Context context, Resources resources, UserDetailsResponse userSender, String connectionId) {
            this.userSender = userSender;
            mContext = context;
            mResources = resources;
            this.connectionId = connectionId;
            chatList = new ArrayList<>();

            initPendingIntent();
            initMessagingStyle();

            notificationId = notificationIdTrack;
            notificationIdTrack++;
        }

        private void initPendingIntent() {
            Intent intent = new Intent(mContext, ChatWithIndividualActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            InterConnection myInterconnection = new InterConnection(connectionId, SessionManager.getUserId(), false);
            InterConnection receiversInterconnection = new InterConnection(connectionId, userSender.getUserId(), false);
            intent.putExtra(Constants.INTENT_USER_DETAILS_EXTRA, userSender);
            intent.putExtra(Constants.INTENT_MY_INTERCONNECTION_EXTRA, myInterconnection);
            intent.putExtra(Constants.INTENT_RECEIVERS_INTERCONNECTION_EXTRA, receiversInterconnection);

            pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }

        private void initMessagingStyle() {
            Person.Builder personBuilder = new Person.Builder();

            if (Helper.isNill(userSender.getProfileImgUrl())) {
                personBuilder.setIcon(IconCompat.createWithBitmap(BitmapFactory.decodeResource(mResources, Helper.getProfilePlaceholderImg(mContext, userSender.getGender()))));
            } else {
                personBuilder.setIcon(IconCompat.createWithBitmap(PicassoCache.getBitmap(mContext, mResources, userSender.profileImgUrl, R.drawable.img_user_placeholder)));
            }
            personSender = personBuilder
                    .setName(ContactsProvider.getContactName(userSender.getMobileNo(), mContext))
                    .build();

            messagingStyle = new NotificationCompat.MessagingStyle(personSender).setGroupConversation(true);
        }

        public void addChatItem(ChatItemResponse chatItem, boolean isReplyMsg) {
            if (isReplyMsg) {
                messagingStyle.addMessage("You: " + chatItem.message, TimeHandler.getCurrentTime(), personSender);
            } else {
                messagingStyle.addMessage(chatItem.message, TimeHandler.getCurrentTime(), personSender);
            }

            chatList.add(chatItem);
            messagingStyle.setConversationTitle(getMessageCountTitle());
        }

        private String getMessageCountTitle() {
            if (chatList.size() == 1) {
                return "1 message";
            }
            return chatList.size() + " messages";
        }

        public ArrayList<String> getReceivedChatsIds() {
            ArrayList<String> chatIdsList = new ArrayList<>();
            for (ChatItemResponse chatItem : chatList) {
                if (chatItem.getSenderId().matches(userSender.getUserId())) {
                    chatIdsList.add(chatItem.getChatId());
                }
            }

            return chatIdsList;
        }
    }

    public static class NotificationRequirements {
        ChatItemResponse chatItem;
        String receiverFcmToken, connectionId;
        boolean isParticipantLive;

        public NotificationRequirements(ChatItemResponse chatItem, String receiverFcmToken, String connectionId) {
            this.chatItem = chatItem;
            this.receiverFcmToken = receiverFcmToken;
            this.connectionId = connectionId;
        }

        public ChatItemResponse getChatItem() {
            return chatItem;
        }

        public String getReceiverFcmToken() {
            return receiverFcmToken;
        }

        public String getConnectionId() {
            return connectionId;
        }

        public boolean isParticipantLive() {
            return isParticipantLive;
        }

        public void setParticipantLive(boolean participantLive) {
            isParticipantLive = participantLive;
        }
    }
}
