package com.example.sswhatsapp.daos;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.sswhatsapp.R;
import com.example.sswhatsapp.firebase.FirebaseConstants;
import com.example.sswhatsapp.firebase.FirestoreManager;
import com.example.sswhatsapp.firebase.FirestoreNetworkCallListener;
import com.example.sswhatsapp.firebase.RealtimeDbManager;
import com.example.sswhatsapp.listeners.ChatWIthIndividualDaoListener;
import com.example.sswhatsapp.models.ChatItemResponse;
import com.example.sswhatsapp.models.ConnectionParticipantResponse;
import com.example.sswhatsapp.models.InterConnection;
import com.example.sswhatsapp.models.UserDetailsResponse;
import com.example.sswhatsapp.models.UserOnlineAvailabilityResponse;
import com.example.sswhatsapp.retrofit.RetrofitConstants;
import com.example.sswhatsapp.retrofit.RetrofitManager;
import com.example.sswhatsapp.retrofit.RetrofitNetworkCallListener;
import com.example.sswhatsapp.utils.Constants;
import com.example.sswhatsapp.utils.Helper;
import com.example.sswhatsapp.utils.TimeHandler;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
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
    public final int CHATS_FETCH_LIMIT;
    public final Context mContext;
    private final FirestoreManager firestoreManager;
    private final RealtimeDbManager realtimeDbManager;
    private final RetrofitManager retrofitManager;
    private final ChatWIthIndividualDaoListener mListener;
    private final UserDetailsResponse senderUser, receiverUser;
    private final InterConnection myInterconnection, receiversInterconnection;
    private final ConnectionParticipantResponse myParticipantItem, receiverParticipantItem;
    private final LinkedList<ChatItemResponse> chatsList;
    private String currentBannerTimeStamp;
    private boolean isReceiverOnline;
    boolean allPreviousChatsFetched, isFetchingPreviousChatsNetworkCallInProgress;

    //FIRESTORE LISTENERS
    EventListener<DocumentSnapshot> receiverDocListener;
    EventListener<QuerySnapshot> chatCollectionListener;
    EventListener<QuerySnapshot> chatParticipantListener;
    ListenerRegistration chatCollectionListenerRegistration;
    ListenerRegistration chatParticipantListenerRegistration;

    //REALTIME DB LISTENERS
    ValueEventListener userAvailabilityListener;

    //CONSTRUCTOR
    public ChatWithIndividualDao(Context context, ChatWIthIndividualDaoListener listener,
                                 UserDetailsResponse senderUser,
                                 UserDetailsResponse receiverUser,
                                 InterConnection myInterconnection,
                                 InterConnection receiversInterconnection) {
        mContext = context;
        mListener = listener;
        this.senderUser = senderUser;
        this.receiverUser = receiverUser;
        this.myInterconnection = myInterconnection;
        this.receiversInterconnection = receiversInterconnection;

        myParticipantItem = new ConnectionParticipantResponse(senderUser.getUserId(), false, true);
        receiverParticipantItem = new ConnectionParticipantResponse(receiverUser.getUserId(), false, false);
        firestoreManager = new FirestoreManager(context, this);
        realtimeDbManager = new RealtimeDbManager();
        retrofitManager = new RetrofitManager(this);
        chatsList = new LinkedList<>();
        currentBannerTimeStamp = null;

        allPreviousChatsFetched = myInterconnection.isEradicated;
        isFetchingPreviousChatsNetworkCallInProgress = false;
        CHATS_FETCH_LIMIT = context.getResources().getInteger(R.integer.chat_fetch_limit);

        //init Firestore listener
        initChatsCollectionListener();
        initChatParticipantListener();
        initReceiverDocListener();
        initUserAvailabilityListener();
    }


    public void attachChatsCollectionListener() {
        if (chatCollectionListenerRegistration != null) {
            //listener already attached
            return;
        }
        if ((!myInterconnection.isEradicated() && chatsList.isEmpty())) {
            /*
             * Case where there are chats between the users but not yet fetched from the server
             * So if we attach the listener in this case the received messaged would be added twice
             */
            return;
        }

        String lastReceivedChatTimeStamp;
        if (chatsList.isEmpty()) {
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
                            newChatReceivedSuccess(chatItem);
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
        };
    }

    //REALTIME CHILD LISTENER
    private void initUserAvailabilityListener() {
        userAvailabilityListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserOnlineAvailabilityResponse response = snapshot.getValue(UserOnlineAvailabilityResponse.class);
                if (response == null) {
                    return;
                }
                if (response.isOnline()) {
                    mListener.receiverOnlineStatusUpdated(true, null);
                } else {
                    String lastOnlineMsg = TimeHandler.getLastOnlineMsg(response.getLastOnlineTime());
                    mListener.receiverOnlineStatusUpdated(false, lastOnlineMsg);
                }

                isReceiverOnline = response.isOnline;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        realtimeDbManager.setUserAvailabilityListener(receiverUser.getUserId(), userAvailabilityListener);
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

    public boolean isReceiverOnline() {
        return isReceiverOnline;
    }

    public boolean areAllPreviousChatsFetched() {
        return allPreviousChatsFetched;
    }

    public void addDateBannerInChatList(String timeStamp, boolean addAtTop, boolean isCurrentBannerTimeStamp) {
        String bannerDate = TimeHandler.getChatBannerTimeStamp(timeStamp);
        /*
         * THE DATE BANNER IS EITHER ADDED AT THE TOP OR AT THE BOTTOM OF THE LIST
         * If @param "addToTop" is true then the date banner will be added at the top
         * else at the bottom of the list
         * */
        if (addAtTop) {
            chatsList.addFirst(new ChatItemResponse(Constants.LAYOUT_TYPE_BANNER_DATE, timeStamp, bannerDate));
        } else {
            chatsList.addLast(new ChatItemResponse(Constants.LAYOUT_TYPE_BANNER_DATE, timeStamp, bannerDate));
        }
        if (isCurrentBannerTimeStamp) {
            currentBannerTimeStamp = timeStamp;
        }
    }


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

    private void chatItemStatusUpdated(int newChatStatus, String chatId) {
        ChatItemResponse currentChatItem;
        for (int position = chatsList.size() - 1; position >= 0; position--) {
            currentChatItem = chatsList.get(position);
            if (currentChatItem.getChatId().matches(chatId)) {
                if (currentChatItem.getChatStatus() < newChatStatus) {
                    currentChatItem.setChatStatus(newChatStatus);
                    mListener.chatItemUpdated(position);
                } else if (currentChatItem.getChatStatus() > newChatStatus) {
                    updateChatStatus(currentChatItem.getChatStatus(), currentChatItem.getChatId());
                }

                break;
            }
        }
    }

    private void newChatReceivedSuccess(ChatItemResponse chatItem) {
        if (!isItTodaysBannerDate()) {
            addDateBannerInChatList(TimeHandler.getCurrentTimeStamp(), false, true);
            mListener.dateBannerAdded(getLastChatItemIndex());
        }

        chatsList.add(chatItem);
        mListener.chatReceivedSuccess(getLastChatItemIndex());
    }

    private void addPreviousChatsInList(QuerySnapshot querySnapshot) {
        String bannerTimeStamp;
        ChatItemResponse chatItem;
        int previousListSize = chatsList.size();

        if (previousListSize == 0) {
            //for the first time previous chat call
            bannerTimeStamp = querySnapshot.getDocuments().get(0).toObject(ChatItemResponse.class).getTimeStamp();
            currentBannerTimeStamp = bannerTimeStamp;
        } else {
            /*
             * we need to remove the date banner present at the top of the list
             * */
            ChatItemResponse topMostHeader = chatsList.remove(0);
            bannerTimeStamp = topMostHeader.getTimeStamp();
        }
        /*
         * ADDING FETCHED CHAT DOCUMENTS IN CHAT_LIST
         * DOCUMENTS FETCHED FROM SERVER ARE IN THE DESCENDING ORDER OF THEIR DATE OF SENT
         **/
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            chatItem = doc.toObject(ChatItemResponse.class);
            if (!TimeHandler.areSameDays(chatItem.getTimeStamp(), bannerTimeStamp)) {
                addDateBannerInChatList(bannerTimeStamp, true, false);
                bannerTimeStamp = chatItem.getTimeStamp();
            }
            chatsList.addFirst(chatItem);
        }
//               ADDING THE DATE BANNER FOR THE TOP MOST CHATS
        addDateBannerInChatList(bannerTimeStamp, true, false);

        mListener.chatItemsAdded(0, chatsList.size() - previousListSize);
        /*
         * updating the layout of the top most chat Item
         * As it may no longer be the first chat in the chat thread hence no longer require tail layout
         * */
        mListener.chatItemUpdated(chatsList.size() - previousListSize + 1);
    }


    //CALL FROM CONTROLLER
    public ChatItemResponse addMessageChat(String message) {
        if (!isItTodaysBannerDate()) {
            addDateBannerInChatList(TimeHandler.getCurrentTimeStamp(), false, true);
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
        mListener.chatItemsAdded(getLastChatItemIndex());

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
        if (isReceiverOnline || receiverParticipantItem.isLive()) {
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

    //NETWORK CALL
    public void sendMessageChat(ChatItemResponse chatItem) {
        firestoreManager.sendMessageChat(myInterconnection.getConnectionId(), chatItem);
    }

    //NETWORK CALL
    public void fetchPreviousChats() {
        isFetchingPreviousChatsNetworkCallInProgress = true;
        if (chatsList.isEmpty()) {
            firestoreManager.fetchChatItems(myInterconnection.getConnectionId(), senderUser.getUserId(), CHATS_FETCH_LIMIT);
        } else {
            String topMostChatDocId = null;
            if (!chatsList.isEmpty()) {
                for (int i = 0; i < chatsList.size(); i++) {
                    if (chatsList.get(i).getChatCategory() != Constants.LAYOUT_TYPE_BANNER_DATE) {
                        topMostChatDocId = chatsList.get(i).getChatId();
                        break;
                    }
                }
            }
            firestoreManager.fetchChatItems(myInterconnection.getConnectionId(), senderUser.getUserId(), topMostChatDocId, CHATS_FETCH_LIMIT);
        }
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
            case FirebaseConstants.FETCH_PREVIOUS_CHATS_CALL: {
                QuerySnapshot snapshot = (QuerySnapshot) response;

                if (!snapshot.getDocuments().isEmpty()) {
                    addPreviousChatsInList(snapshot);
                }

                if (snapshot.getDocuments().size() < CHATS_FETCH_LIMIT) {
                    allPreviousChatsFetched = true;
                    if (chatsList.size() < 15) {
                        mListener.changeRvStackingOrder(false);
                    }
                }

                mListener.hideLoadingAnimation();
                attachChatsCollectionListener();
                isFetchingPreviousChatsNetworkCallInProgress = false;
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
            case FirebaseConstants.FETCH_PREVIOUS_CHATS_CALL: {
                isFetchingPreviousChatsNetworkCallInProgress = false;
                mListener.hideLoadingAnimation();
                break;
            }
            case FirebaseConstants.SEND_MESSAGE_CHAT_CALL: {
                ChatItemResponse chatItem = (ChatItemResponse) response;
                chatSentFailure(chatItem);
                break;
            }
        }
    }

    @Override
    public void onFirestoreNetworkCallFailure(String errorMessage) {
        Log.d(FirebaseConstants.NETWORK_CALL, errorMessage);
        Toast.makeText(mContext, errorMessage, Toast.LENGTH_LONG).show();
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
    }


    public void detachChatsCollectionListener() {
        if (chatCollectionListenerRegistration == null) {
            return;
        }
        chatCollectionListenerRegistration.remove();
        chatCollectionListenerRegistration = null;
    }

    public void detachChatParticipantListener() {
        if (chatParticipantListenerRegistration == null) {
            return;
        }
        chatParticipantListenerRegistration.remove();
        chatParticipantListenerRegistration = null;
    }

    //CALL FROM ACTIVITY
    public void onDestroy() {
        realtimeDbManager.removeUserAvailabilityListener(receiverUser.getUserId(), userAvailabilityListener);
    }
}
