package com.example.sswhatsapp.daos;

import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sswhatsapp.firebase.FirebaseConstants;
import com.example.sswhatsapp.firebase.FirestoreManager;
import com.example.sswhatsapp.firebase.FirestoreNetworkCallListener;
import com.example.sswhatsapp.listeners.ChatWIthIndividualDaoListener;
import com.example.sswhatsapp.models.ChatItemResponse;
import com.example.sswhatsapp.models.InterConnection;
import com.example.sswhatsapp.models.UserDetailsResponse;
import com.example.sswhatsapp.retrofit.RetrofitConstants;
import com.example.sswhatsapp.retrofit.RetrofitManager;
import com.example.sswhatsapp.retrofit.RetrofitNetworkCallListener;
import com.example.sswhatsapp.utils.Constants;
import com.example.sswhatsapp.utils.TimeHandler;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class ChatWithIndividualDao implements FirestoreNetworkCallListener, RetrofitNetworkCallListener {
    //fields
    AppCompatActivity mActivity;
    private final FirestoreManager firestoreManager;
    private final RetrofitManager retrofitManager;
    private final UserDetailsResponse senderUser, receiverUser;
    private final InterConnection myInterconnection, receiversInterconnection;
    private final ChatWIthIndividualDaoListener mListener;
    private List<ChatItemResponse> chatsList;
    private String currentBannerTimeStamp;

    //LISTENERS
    EventListener<QuerySnapshot> chatReceiverListener;
    ListenerRegistration chatReceiverListenerRegistration;

    public ChatWithIndividualDao(AppCompatActivity activity, ChatWIthIndividualDaoListener listener,
                                 UserDetailsResponse senderUser,
                                 UserDetailsResponse receiverUser,
                                 InterConnection myInterconnection,
                                 InterConnection receiversInterconnection) {
        mActivity = activity;
        mListener = listener;
        this.senderUser = senderUser;
        this.receiverUser = receiverUser;
        this.myInterconnection = myInterconnection;
        this.receiversInterconnection = receiversInterconnection;

        firestoreManager = new FirestoreManager(activity, this);
        retrofitManager = new RetrofitManager(this);
        chatsList = new LinkedList<>();
        initChatReceiverListener();
    }

    public void attachChatReceiverListener() {
        if (!myInterconnection.isEradicated() && chatsList.isEmpty()) {
            /*
             * Case where there are chats between the users but not yet fetched from the server
             * So if we attach the listener in this case the received messaged would be added twice
             */
            return;
        }

        String lastReceivedChatTimeStamp;
        if (myInterconnection.isEradicated) {
            lastReceivedChatTimeStamp = TimeHandler.getCurrentTimeStamp();
        } else {
            lastReceivedChatTimeStamp = chatsList.get(getLastChatItemIndex()).getTimeStamp();
        }
        chatReceiverListenerRegistration = firestoreManager.setIndividualConnectionListener(myInterconnection.getConnectionId(), chatReceiverListener, lastReceivedChatTimeStamp);
    }

    public void detachChatReceiverListener() {
        chatReceiverListenerRegistration.remove();
        chatReceiverListenerRegistration = null;
    }

    private void initChatReceiverListener() {
        chatReceiverListener = (value, error) -> {
            if (error != null || value == null) {
                return;
            }
            for (DocumentChange doc : value.getDocumentChanges()) {
                if (doc.getType() == DocumentChange.Type.ADDED) {
                    String senderId = doc.getDocument().getString(FirebaseConstants.KEY_SENDER_ID);

                    if (senderId.matches(receiverUser.getUserId())) {
                        ChatItemResponse chatItem = doc.getDocument().toObject(ChatItemResponse.class);
                        chatReceivedSuccess(chatItem);
                    }
                }
            }
        };
    }

    public List<ChatItemResponse> getChatsList() {
        return chatsList;
    }

    public int getLastChatItemIndex() {
        return chatsList.size() - 1;
    }

    public UserDetailsResponse getReceiverUser() {
        return receiverUser;
    }

    public String getConnectionId() {
        return myInterconnection.getConnectionId();
    }

    public String getMyUserId() {
        return senderUser.getUserId();
    }

    public boolean isItTodaysBannerDate() {
        if (currentBannerTimeStamp == null)
            return false;
        return TimeHandler.isThisToday(currentBannerTimeStamp);
    }

    public boolean isEradicated() {
        return myInterconnection.isEradicated();
    }

    public void addDateBannerInChatList(String standardTimeStamp) {
        currentBannerTimeStamp = standardTimeStamp;
        String bannerDate = TimeHandler.getChatBannerStamp(standardTimeStamp);
        chatsList.add(new ChatItemResponse(Constants.LAYOUT_TYPE_BANNER_DATE, bannerDate));
    }


    //CALL FROM CONTROLLER
    public ChatItemResponse addMessageChat(String message) {
        if (!isItTodaysBannerDate()) {
            addDateBannerInChatList(TimeHandler.getCurrentTimeStamp());
            mListener.dateBannerAdded(getLastChatItemIndex());
        }
        ChatItemResponse chatItem = new ChatItemResponse(Constants.CHAT_CATEGORY_MSG,
                Constants.CHAT_STATUS_PENDING,
                senderUser.getUserId(),
                receiverUser.getUserId(),
                message,
                TimeHandler.getCurrentTimeStamp(),
                false,
                false,
                false);

        chatsList.add(chatItem);
        mListener.chatAdded(getLastChatItemIndex());

        if (myInterconnection.isEradicated()) {
            updateMyIsEradicatedField(false);
        }

        if (receiversInterconnection.isEradicated) {
            updateReceiversIsEradicatedField(false);
        }
        return chatItem;
    }

    //CALL FROM CONTROLLER
    public void sendNotification(ChatItemResponse chatItem) {
        JSONObject dataMap = new JSONObject();
        try {
            dataMap.put(FirebaseConstants.KEY_USER_ID, senderUser.getUserId());                     //notification sender ID
            dataMap.put(FirebaseConstants.KEY_USER_MOBILE_NO, senderUser.getMobileNo());
            dataMap.put(FirebaseConstants.KEY_USER_PROFILE_IMG_URL, senderUser.getProfileImgUrl());
            dataMap.put(FirebaseConstants.KEY_CONNECTION_ID, myInterconnection.getConnectionId());
            dataMap.put(FirebaseConstants.KEY_CHAT_ID, chatItem.getChatId());
            dataMap.put(FirebaseConstants.KEY_CHAT_CATEGORY, Integer.toString(chatItem.getChatCategory()));
            dataMap.put(FirebaseConstants.KEY_CHAT_MESSAGE, chatItem.getMessage());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        retrofitManager.sendChatNotificationCall(receiverUser.getFcmToken(), dataMap);
    }


    //CALL TO CONTROLLER
    private void chatSentSuccess(ChatItemResponse chatItem) {
        int position = chatsList.size() - 1;
        for (int i = chatsList.size() - 1; i >= 0; i--) {
            if (chatsList.get(i).getChatId().matches(chatItem.getChatId())) {
                chatsList.get(i).setChatStatus(Constants.CHAT_STATUS_SENT);
                position = i;
                break;
            }
        }

        mListener.chatSentSuccess(position);
    }

    //CALL TO CONTROLLER
    private void chatSentFailure(ChatItemResponse chatItem) {
        chatItem.removeChatId();
        chatItem.setChatStatus(Constants.CHAT_STATUS_HALTED);
        int position = chatsList.size() - 1;
        for (int i = chatsList.size() - 1; i >= 0; i--) {
            if (chatsList.get(i).getChatId().matches(chatItem.getChatId())) {
                position = i;
                break;
            }
        }
        mListener.chatSentFailure(position);
    }

    //CALL TO CONTROLLER
    private void chatReceivedSuccess(ChatItemResponse chatItem) {
        if (!isItTodaysBannerDate()) {
            addDateBannerInChatList(TimeHandler.getCurrentTimeStamp());
            mListener.dateBannerAdded(getLastChatItemIndex());
        }

        chatsList.add(chatItem);
        mListener.chatReceivedSuccess(getLastChatItemIndex());
    }


    //NETWORK CALL
    public void sendMessageChat(ChatItemResponse chatItem) {
        firestoreManager.sendMessageChat(myInterconnection.getConnectionId(), chatItem);
    }

    //NETWORK CALL
    public void fetchAllChats() {
        firestoreManager.fetchAllChats(myInterconnection.getConnectionId(), senderUser.getUserId());
    }

    //NETWORK CALL
    public void updateMyIsEradicatedField(boolean isEradicated) {
        firestoreManager.updateMyIsEradicatedField(senderUser.getMyInterconnectionsDocId(), receiverUser.getUserId(), isEradicated);
    }

    //NETWORK CALL
    public void updateReceiversIsEradicatedField(boolean isEradicated) {
        firestoreManager.updateReceiversIsEradicatedField(receiverUser.getMyInterconnectionsDocId(), senderUser.getUserId(), isEradicated);
    }

    @Override
    public void onFirestoreNetworkCallSuccess(Object response, int serviceCode) {
        switch (serviceCode) {
            case FirebaseConstants.FETCH_ALL_CHATS_CALL: {
                QuerySnapshot snapshot = (QuerySnapshot) response;
                ChatItemResponse chatItem;

                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    chatItem = doc.toObject(ChatItemResponse.class);

                    if (currentBannerTimeStamp == null) {
                        addDateBannerInChatList(chatItem.getTimeStamp());
                    } else if (!TimeHandler.areSameDays(chatItem.getTimeStamp(), currentBannerTimeStamp)) {
                        addDateBannerInChatList(chatItem.getTimeStamp());
                    }
                    chatsList.add(chatItem);
                }

                mListener.allChatsAdded(0, chatsList.size());
                attachChatReceiverListener();
                break;
            }
            case FirebaseConstants.SEND_MESSAGE_CHAT_CALL: {
                ChatItemResponse chatItem = (ChatItemResponse) response;
                chatSentSuccess(chatItem);
                break;
            }
            case FirebaseConstants.UPDATE_MY_IS_ERADICATED_FIELD_CALL: {
                myInterconnection.setEradicated((boolean) response);
                break;
            }
            case FirebaseConstants.UPDATE_RECEIVERS_IS_ERADICATED_FIELD_CALL: {
                receiversInterconnection.setEradicated((boolean) response);
                break;
            }
        }

    }

    @Override
    public void onFirestoreNetworkCallFailure(Object response, int serviceCode) {
        switch (serviceCode) {
            case FirebaseConstants.SEND_MESSAGE_CHAT_CALL:
                ChatItemResponse chatItem = (ChatItemResponse) response;
                chatSentFailure(chatItem);
                break;
        }
    }

    @Override
    public void onFirestoreNetworkCallFailure(String errorMessage) {
        Log.d(FirebaseConstants.NETWORK_CALL, errorMessage);
        Toast.makeText(mActivity, errorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRetrofitNetworkCallSuccess(Object response, int serviceCode) {
        switch (serviceCode) {
            case RetrofitConstants.SEND_CHAT_NOTIFICATION_CALL: {
                Toast.makeText(mActivity, "Notification successfully sent!", Toast.LENGTH_LONG).show();
                break;
            }
        }
    }

    @Override
    public void onRetrofitNetworkCallFailure(String errorMessage) {
        Log.d(FirebaseConstants.NETWORK_CALL, errorMessage);
        Toast.makeText(mActivity, errorMessage, Toast.LENGTH_LONG).show();
    }
}
