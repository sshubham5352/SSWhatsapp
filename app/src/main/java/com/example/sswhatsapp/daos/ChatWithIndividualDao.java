package com.example.sswhatsapp.daos;

import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sswhatsapp.firebase.FirebaseConstants;
import com.example.sswhatsapp.firebase.FirestoreManager;
import com.example.sswhatsapp.firebase.FirestoreNetworkCallListener;
import com.example.sswhatsapp.listeners.ChatWIthIndividualDaoListener;
import com.example.sswhatsapp.models.ChatItemResponse;
import com.example.sswhatsapp.models.UserDetailsResponse;
import com.example.sswhatsapp.utils.Constants;
import com.example.sswhatsapp.utils.TimeHandler;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.LinkedList;
import java.util.List;

public class ChatWithIndividualDao implements FirestoreNetworkCallListener {
    //fields
    AppCompatActivity mActivity;
    private UserDetailsResponse connectionWithUser;
    private String myConnectionsListRef;
    private List<ChatItemResponse> chatsList;
    private ChatWIthIndividualDaoListener mListener;
    private String connectionId, myUserId;
    private FirestoreManager firestoreManager;
    private String currentBannerTimeStamp;
    private boolean isEradicated;

    //LISTENERS
    EventListener<QuerySnapshot> chatReceiverListener;
    ListenerRegistration chatReceiverListenerRegistration;

    public ChatWithIndividualDao(AppCompatActivity activity, ChatWIthIndividualDaoListener listener,
                                 UserDetailsResponse connectionWithUser,
                                 String myConnectionsListRef,
                                 String connectionId, String myUserId, boolean isEradicated) {
        mActivity = activity;
        mListener = listener;
        this.connectionWithUser = connectionWithUser;
        this.myConnectionsListRef = myConnectionsListRef;
        this.connectionId = connectionId;
        this.myUserId = myUserId;
        this.isEradicated = isEradicated;
        firestoreManager = new FirestoreManager(activity, this);
        chatsList = new LinkedList<>();
        initChatReceiverListener();
    }

    public void attachChatReceiverListener() {
        if (!isEradicated && chatsList.isEmpty()) {
            /*
             * Case where there are chats between the users but not yet fetched from the server
             * So if we attach the listener in this case the received messaged would be added twice
             */
            return;
        }

        String lastReceivedChatTimeStamp;
        if (isEradicated) {
            lastReceivedChatTimeStamp = TimeHandler.getCurrentTimeStamp();
        } else {
            lastReceivedChatTimeStamp = chatsList.get(getLastChatItemIndex()).getTimeStamp();
        }
        chatReceiverListenerRegistration = firestoreManager.setIndividualConnectionListener(connectionId, chatReceiverListener, lastReceivedChatTimeStamp);
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

                    if (senderId.matches(connectionWithUser.getUserId())) {
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

    public UserDetailsResponse getConnectionWithUser() {
        return connectionWithUser;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public String getMyUserId() {
        return myUserId;
    }

    public boolean isItTodaysBannerDate() {
        if (currentBannerTimeStamp == null)
            return false;
        return TimeHandler.isThisToday(currentBannerTimeStamp);
    }

    public boolean isEradicated() {
        return isEradicated;
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
                myUserId,
                connectionWithUser.getUserId(),
                message,
                TimeHandler.getCurrentTimeStamp(),
                false,
                false,
                false);

        chatsList.add(chatItem);
        mListener.chatAdded(getLastChatItemIndex());

        if (isEradicated) {
            updateIsEradicatedField(false);
        }
        return chatItem;
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
        if (isEradicated) {
            updateIsEradicatedField(false);
        }
        chatsList.add(chatItem);
        mListener.chatReceivedSuccess(getLastChatItemIndex());
    }


    //NETWORK CALL
    public void sendMessageChat(ChatItemResponse chatItem) {
        firestoreManager.sendMessageChat(connectionId, chatItem);
    }

    //NETWORK CALL
    public void fetchAllChats() {
        firestoreManager.fetchAllChats(connectionId, myUserId);
    }

    //NETWORK CALL
    public void updateIsEradicatedField(boolean isEradicated) {
        firestoreManager.updateIsEradicatedField(myConnectionsListRef, connectionWithUser.getUserId(), isEradicated);
    }

    @Override
    public void onFirestoreNetworkCallSuccess(Object response, int serviceCode) {
        switch (serviceCode) {
            case FirebaseConstants.FETCH_ALL_MESSAGES_CALL: {
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
            case FirebaseConstants.SEND_MESSAGE_CHAT_CALL:
                ChatItemResponse chatItem = (ChatItemResponse) response;
                chatSentSuccess(chatItem);
                break;
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
}
