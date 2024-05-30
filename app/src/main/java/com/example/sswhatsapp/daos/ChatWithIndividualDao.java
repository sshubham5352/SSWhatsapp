package com.example.sswhatsapp.daos;

import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sswhatsapp.firebase.FirebaseConstants;
import com.example.sswhatsapp.firebase.FirestoreManager;
import com.example.sswhatsapp.firebase.FirestoreNetworkCallListener;
import com.example.sswhatsapp.listeners.ChatWIthIndividualDaoListener;
import com.example.sswhatsapp.models.ChatItemResponse;
import com.example.sswhatsapp.models.ConnectionParticipantResponse;
import com.example.sswhatsapp.models.InterConnection;
import com.example.sswhatsapp.models.UserDetailsResponse;
import com.example.sswhatsapp.retrofit.RetrofitConstants;
import com.example.sswhatsapp.retrofit.RetrofitManager;
import com.example.sswhatsapp.retrofit.RetrofitNetworkCallListener;
import com.example.sswhatsapp.utils.Constants;
import com.example.sswhatsapp.utils.Helper;
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
    private final ConnectionParticipantResponse myParticipantItem, receiverParticipantItem;
    private final ChatWIthIndividualDaoListener mListener;
    private List<ChatItemResponse> chatsList;
    private String currentBannerTimeStamp;

    //FIRESTORE LISTENERS
    EventListener<DocumentSnapshot> receiverDocListener;
    EventListener<QuerySnapshot> chatCollectionListener;
    EventListener<QuerySnapshot> chatParticipantListener;
    ListenerRegistration chatCollectionListenerRegistration;
    ListenerRegistration chatParticipantListenerRegistration;

    //CONSTRUCTOR
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

        myParticipantItem = new ConnectionParticipantResponse(senderUser.getUserId(), false, true);
        receiverParticipantItem = new ConnectionParticipantResponse(receiverUser.getUserId(), false, false);

        firestoreManager = new FirestoreManager(activity, this);
        retrofitManager = new RetrofitManager(this);
        chatsList = new LinkedList<>();

        //init Firestore listener
        initChatsCollectionListener();
        initChatParticipantListener();
        initReceiverDocListener();
    }

    public void attachChatsCollectionListener() {
        if (!myInterconnection.isEradicated() && chatsList.isEmpty()) {
            /*
             * Case where there are chats between the users but not yet fetched from the server
             * So if we attach the listener in this case the received messaged would be added twice
             */
            return;
        }

        String lastReceivedChatTimeStamp;
        if (myInterconnection.isEradicated()) {
            lastReceivedChatTimeStamp = TimeHandler.getCurrentTimeStamp();
        } else {
            lastReceivedChatTimeStamp = chatsList.get(getLastChatItemIndex()).getTimeStamp();
        }
        chatCollectionListenerRegistration = firestoreManager.setIndividualConnectionListener(myInterconnection.getConnectionId(), chatCollectionListener, lastReceivedChatTimeStamp);
    }

    public void attachChatParticipantListener() {
        chatParticipantListenerRegistration = firestoreManager.setConnectionParticipantListener(myInterconnection.getConnectionId(), receiverUser.getUserId(), chatParticipantListener);
    }

    public void attachReceiverDocListener() {
        firestoreManager.setUserDocListener(receiverUser.getUserId(), receiverDocListener);
    }

    public void detachChatsCollectionListener() {
        chatCollectionListenerRegistration.remove();
        chatCollectionListenerRegistration = null;
    }

    public void detachChatParticipantListener() {
        chatParticipantListenerRegistration.remove();
        chatParticipantListenerRegistration = null;
    }

    //FIRESTORE COLLECTION LISTENER
    private void initChatsCollectionListener() {
        chatCollectionListener = (value, error) -> {
            if (error != null || value == null) {
                return;
            }
            for (DocumentChange doc : value.getDocumentChanges()) {
                switch (doc.getType()) {
                    case ADDED: {
                        String senderId = doc.getDocument().getString(FirebaseConstants.KEY_SENDER_ID);
                        if (senderId.matches(receiverUser.getUserId())) {
                            ChatItemResponse chatItem = doc.getDocument().toObject(ChatItemResponse.class);
                            chatReceivedSuccess(chatItem);
                            updateChatStatus(Constants.CHAT_STATUS_READ, chatItem.getChatId());
                        }
                        break;
                    }

                    case MODIFIED: {
                        ChatItemResponse chatItem = doc.getDocument().toObject(ChatItemResponse.class);
                        chatItemStatusUpdated(chatItem.getChatStatus(), chatItem.getChatId());
                        break;
                    }
                }
            }
        };
    }

    //FIRESTORE DOCUMENT LISTENER
    private void initChatParticipantListener() {
        chatParticipantListener = (value, error) -> {
            if (error != null || value == null) {
                return;
            }
            for (DocumentChange doc : value.getDocumentChanges()) {
                ConnectionParticipantResponse participantResponse = doc.getDocument().toObject(ConnectionParticipantResponse.class);
                if (receiverParticipantItem.isTyping() != participantResponse.isTyping()) {
                    receiverParticipantItem.setTyping(participantResponse.isTyping());
                    mListener.receiverTypingStatusUpdated(receiverParticipantItem.isTyping());
                }
                if (receiverParticipantItem.isLive() != participantResponse.isLive()) {
                    receiverParticipantItem.setLive(participantResponse.isLive());
                }
            }
        };
    }

    //FIRESTORE DOCUMENT LISTENER
    private void initReceiverDocListener() {
        receiverDocListener = (value, error) -> {
            if (error != null || value == null) {
                return;
            }

            UserDetailsResponse receiverResponse = value.toObject(UserDetailsResponse.class);
            if (receiverUser.isOnline() != receiverResponse.isOnline()) {
                receiverUser.setOnline(receiverResponse.isOnline());
                mListener.receiverOnlineStatusUpdated(receiverUser.isOnline());
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

        if (receiversInterconnection.isEradicated()) {
            updateReceiversIsEradicatedField(false);
        }
        return chatItem;
    }

    //CALL FROM CONTROLLER
    public void sendNotification(ChatItemResponse chatItem) {
        if (receiverParticipantItem.isLive()) {
            /*
             * Don't send the notification if the user is Live on chat
             **/
            return;
        }
        if (Helper.isNill(receiverUser.getFcmToken())) {
            /*
             * As FCM token of receiver's device in null so can't send the notification
             **/
            return;
        }

        JSONObject dataMap = new JSONObject();
        try {
            dataMap.put(RetrofitConstants.NOTIFICATION_TYPE, Integer.toString(RetrofitConstants.SEND_CHAT_NOTIFICATION_CALL));
            dataMap.put(FirebaseConstants.KEY_FCM_TOKEN, senderUser.getFcmToken());
            dataMap.put(FirebaseConstants.KEY_USER_ID, senderUser.getUserId());                         //notification sender ID
            dataMap.put(FirebaseConstants.KEY_SENDER_ID, senderUser.getUserId());
            dataMap.put(FirebaseConstants.KEY_RECEIVER_ID, receiverUser.getUserId());
            dataMap.put(FirebaseConstants.KEY_USER_NAME, senderUser.getName());
            dataMap.put(FirebaseConstants.KEY_USER_GENDER, senderUser.getGender());
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
    private void chatSentSuccess(String chatId) {
        int position = chatsList.size() - 1;
        for (int i = chatsList.size() - 1; i >= 0; i--) {
            if (chatsList.get(i).getChatId().matches(chatId)) {
                chatsList.get(i).setChatStatus(Constants.CHAT_STATUS_SENT);
                position = i;
                break;
            }
        }

        mListener.chatSentSuccess(position);
    }

    //CALL TO CONTROLLER
    private void chatItemStatusUpdated(int newChatStatus, String chatId) {
        ChatItemResponse currentChatItem;
        for (int position = chatsList.size() - 1; position >= 0; position--) {
            currentChatItem = chatsList.get(position);
            if (currentChatItem.getChatId().matches(chatId)) {
                if (currentChatItem.getChatStatus() < newChatStatus) {
                    currentChatItem.setChatStatus(newChatStatus);
                    mListener.chatStatusUpdated(position);
                } else if (currentChatItem.getChatStatus() > newChatStatus) {
                    updateChatStatus(currentChatItem.getChatStatus(), currentChatItem.getChatId());
                }

                break;
            }
        }
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

    //NETWORK CALL
    public void updateChatStatus(int newChatStatus, String chatId) {
        firestoreManager.updateChatStatus(newChatStatus, chatId, getConnectionId());
    }

    //NETWORK CALL
    public void updateAllChatsStatusAsRead() {
        firestoreManager.updateAllChatsStatusAsRead(getMyUserId(), getConnectionId());
    }

    //NETWORK CALL
    public void updateMyTypingStatus(boolean isTyping) {
        if (myParticipantItem.isTyping() != isTyping) {
            firestoreManager.updateParticipantTypingStatus(isTyping, myParticipantItem.getDocId(), myInterconnection.getConnectionId());
            myParticipantItem.setTyping(isTyping);
        }
    }

    //NETWORK CALL
    public void updateMyLiveOnChatStatus(boolean isLive) {
        myParticipantItem.setLive(isLive);
        firestoreManager.updateMyLiveOnChatStatus(isLive, myParticipantItem.getDocId(), getConnectionId());
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
                attachChatsCollectionListener();
                break;
            }
            case FirebaseConstants.SEND_MESSAGE_CHAT_CALL: {
                ChatItemResponse chatItem = (ChatItemResponse) response;
                chatSentSuccess(chatItem.getChatId());
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
                Log.d(RetrofitConstants.NETWORK_CALL, "onRetrofitNetworkCallSuccess: Notification sent successfully");
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
