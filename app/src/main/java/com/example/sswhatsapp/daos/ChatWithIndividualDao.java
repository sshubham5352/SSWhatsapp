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
import com.example.sswhatsapp.models.responses.ChatItemResponse;
import com.example.sswhatsapp.models.responses.ConnectionParticipantResponse;
import com.example.sswhatsapp.models.responses.InterConnectionResponse;
import com.example.sswhatsapp.models.responses.UserDetailsResponse;
import com.example.sswhatsapp.models.responses.UserOnlineAvailabilityResponse;
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

import java.util.ArrayList;
import java.util.HashMap;
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
    private final UserDetailsResponse myUserDetails, receiverUser;
    private final InterConnectionResponse myInterconnection, receiversInterconnection;
    private final ConnectionParticipantResponse myParticipantItem, receiverParticipantItem;
    private final LinkedList<ChatItemResponse> chatsList;
    private String currentBannerTimeStamp;
    private UserOnlineAvailabilityResponse receiverOnlineAvailability;
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
                                 UserDetailsResponse myUserDetails,
                                 UserDetailsResponse receiverUser,
                                 InterConnectionResponse myInterconnection,
                                 InterConnectionResponse receiversInterconnection) {
        mContext = context;
        mListener = listener;
        this.myUserDetails = myUserDetails;
        this.receiverUser = receiverUser;
        this.myInterconnection = myInterconnection;
        this.receiversInterconnection = receiversInterconnection;

        myParticipantItem = new ConnectionParticipantResponse(myUserDetails.getUserId(), false, true);
        receiverParticipantItem = new ConnectionParticipantResponse(receiverUser.getUserId(), false, false);
        firestoreManager = new FirestoreManager(context, this);
        realtimeDbManager = new RealtimeDbManager();
        retrofitManager = new RetrofitManager(this);
        receiverOnlineAvailability = new UserOnlineAvailabilityResponse();
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
        chatCollectionListenerRegistration = firestoreManager.addIndividualChatConnectionListener(myInterconnection.getConnectionId(), chatCollectionListener, lastReceivedChatTimeStamp);
    }

    public void attachChatParticipantListener() {
        chatParticipantListenerRegistration = firestoreManager.addConnectionParticipantListener(myInterconnection.getConnectionId(), receiverUser.getUserId(), chatParticipantListener);
    }

    public void attachReceiverDocListener() {
        firestoreManager.addUserDocListener(receiverUser.getUserId(), receiverDocListener);
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
                        String chatId = doc.getDocument().getString(FirebaseConstants.KEY_CHAT_ID);
                        if (senderId.matches(getMyUserId()) || chatsList.get(getLastChatItemIndex()).getChatId().matches(chatId)) {
                            //fetching the same last chatItem
                            return;
                        }

                        ChatItemResponse chatItem = doc.getDocument().toObject(ChatItemResponse.class);
                        newChatReceivedSuccess(chatItem);
                        updateChatStatus(Constants.CHAT_STATUS_READ, chatItem.getChatId());
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
                ConnectionParticipantResponse response = doc.getDocument().toObject(ConnectionParticipantResponse.class);

                if (receiverParticipantItem.isTyping() != response.isTyping()) {
                    receiverParticipantItem.setTyping(response.isTyping());
                    mListener.receiverTypingStatusUpdated(receiverParticipantItem.isTyping());
                }
                if (receiverParticipantItem.isLive() != response.isLive()) {
                    receiverParticipantItem.setLive(response.isLive());
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
                    if (response.getLastOnlineTime() == receiverOnlineAvailability.lastOnline) {
                        mListener.receiverOnlineStatusUpdated(false, null);
                    } else {
                        String lastOnlineMsg = TimeHandler.getLastSeenTimeStamp(response.getLastOnlineTime());
                        mListener.receiverOnlineStatusUpdated(false, lastOnlineMsg);
                    }
                }

                receiverOnlineAvailability.set(response);
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
        return myUserDetails.getUserId();
    }

    public InterConnectionResponse getMyInterconnection() {
        return myInterconnection;
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
        return receiverOnlineAvailability.isOnline();
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
                if (newChatStatus == Constants.CHAT_STATUS_READ) {
                    updatePreviousChatsToRead(position);
                }
                if (currentChatItem.getChatStatus() < newChatStatus) {
                    currentChatItem.setChatStatus(newChatStatus);
                    mListener.chatItemUpdated(position);
                } else if (currentChatItem.getChatStatus() > newChatStatus) {
                    //particular case when a seen chat item is again converted to received status by NotificationManager
                    updateChatStatus(currentChatItem.getChatStatus(), currentChatItem.getChatId());
                }
                break;
            }
        }
    }

    private void updatePreviousChatsToRead(int lastReadItemPosition) {
        for (int i = lastReadItemPosition - 1; i > 0; i--) {
            if (chatsList.get(i).getChatId().matches(ChatItemResponse.CHAT_DATE_BANNER_ID)) {
                continue;
            }
            if (chatsList.get(i).senderId.matches(getMyUserId())) {
                if (chatsList.get(i).getChatStatus() == Constants.CHAT_STATUS_READ) {
                    return;
                } else {
                    chatsList.get(i).setChatStatus(Constants.CHAT_STATUS_READ);
                    mListener.chatItemUpdated(i);
                }
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
        String topmostBannerTimeStamp;
        int chatsListSizeBeforeInsertion = chatsList.size();
        ArrayList<String> unseenChatsIdList = new ArrayList<>();

        if (chatsListSizeBeforeInsertion == 0) {
            /*
             * As chatsList size is 0 which means this is the first previousChatsCall
             * and therefore we need to assign topmostBannerTimeStamp with the firstChatItem of the Response data
             * NOTE: {@param(topmostBannerTimeStamp), as its name suggests, represents the topmost dateBanner in the chatsList which
             *        will be used to compare and add previousChats in the list.
             *        @param(currentBannerTimeStamp) represents the bottommost dateBanner in the list which is used to compare and
             *        add newChats in the list.
             *       }
             * */
            topmostBannerTimeStamp = querySnapshot.getDocuments().get(0).toObject(ChatItemResponse.class).getTimeStamp();
            currentBannerTimeStamp = topmostBannerTimeStamp;
        } else {
            /*
             * we need to remove the date banner present at the top of the list
             * As more chat items may come under this banner so we would add this banner at it's appropriate position in the list
             * */
            ChatItemResponse topMostHeaderInCurrentList = chatsList.remove(0);
            topmostBannerTimeStamp = topMostHeaderInCurrentList.getTimeStamp();
        }
        /*
         * ADDING FETCHED CHAT DOCUMENTS IN CHAT_LIST
         * DOCUMENTS FETCHED FROM SERVER ARE IN THE DESCENDING ORDER OF THEIR DATE OF SENT
         **/
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            ChatItemResponse chatItem = doc.toObject(ChatItemResponse.class);
            if (!TimeHandler.areSameDays(chatItem.getTimeStamp(), topmostBannerTimeStamp)) {
                addDateBannerInChatList(topmostBannerTimeStamp, true, false);
                topmostBannerTimeStamp = chatItem.getTimeStamp();
            }
            chatsList.addFirst(chatItem);

            if (chatItem.receiverId.matches(getMyUserId()) && chatItem.chatStatus != Constants.CHAT_STATUS_READ) {
                unseenChatsIdList.add(chatItem.chatId);
            }
        }
//               ADDING THE DATE BANNER FOR THE TOP MOST CHATS
        addDateBannerInChatList(topmostBannerTimeStamp, true, false);

        mListener.chatItemsAdded(0, chatsList.size() - chatsListSizeBeforeInsertion);
        /*
         * updating the layout of the top most chat Item
         * As it may no longer be the first chat in the chat thread hence no longer require tail layout
         * */
        mListener.chatItemUpdated(chatsList.size() - chatsListSizeBeforeInsertion + 1);
        //updating the chat status of the unseen chats
        updateChatStatus(Constants.CHAT_STATUS_READ, unseenChatsIdList);
    }


    //CALL FROM CONTROLLER
    public ChatItemResponse addMessageChat(String message) {
        if (!isItTodaysBannerDate()) {
            addDateBannerInChatList(TimeHandler.getCurrentTimeStamp(), false, true);
            mListener.dateBannerAdded(getLastChatItemIndex());
        }
        ChatItemResponse chatItem = new ChatItemResponse(getConnectionId(), Constants.CHAT_CATEGORY_MSG,
                Constants.CHAT_STATUS_PENDING,
                myUserDetails.getUserId(),
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
        if (receiverOnlineAvailability.isOnline && receiverParticipantItem.isLive()) {
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
            dataMap.put(FirebaseConstants.KEY_FCM_TOKEN, myUserDetails.getFcmToken());
            dataMap.put(FirebaseConstants.KEY_USER_ID, myUserDetails.getUserId());                         //notification sender ID
            dataMap.put(FirebaseConstants.KEY_SENDER_ID, myUserDetails.getUserId());
            dataMap.put(FirebaseConstants.KEY_RECEIVER_ID, receiverUser.getUserId());
            dataMap.put(FirebaseConstants.KEY_USER_NAME, myUserDetails.getName());
            dataMap.put(FirebaseConstants.KEY_USER_GENDER, myUserDetails.getGender());
            dataMap.put(FirebaseConstants.KEY_USER_MOBILE_NO, myUserDetails.getMobileNo());
            dataMap.put(FirebaseConstants.KEY_USER_PROFILE_IMG_URL, myUserDetails.getProfileImgUrl());
            dataMap.put(FirebaseConstants.KEY_CONNECTION_ID, myInterconnection.getConnectionId());
            dataMap.put(FirebaseConstants.KEY_CHAT_ID, chatItem.getChatId());
            dataMap.put(FirebaseConstants.KEY_CHAT_CATEGORY, Integer.toString(chatItem.getChatCategory()));
            dataMap.put(FirebaseConstants.KEY_CHAT_MESSAGE, chatItem.getMessage());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        retrofitManager.sendChatNotificationCall(receiverUser.getFcmToken(), dataMap);
    }

    public void sendNotificationV1(ChatItemResponse chatItem) {
        if (receiverOnlineAvailability.isOnline && receiverParticipantItem.isLive()) {
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

        HashMap<String, String> dataMap = new HashMap<>();
        dataMap.put(RetrofitConstants.NOTIFICATION_TYPE, Integer.toString(RetrofitConstants.SEND_CHAT_NOTIFICATION_CALL));
        dataMap.put(FirebaseConstants.KEY_FCM_TOKEN, myUserDetails.getFcmToken());
        dataMap.put(FirebaseConstants.KEY_USER_ID, myUserDetails.getUserId());                         //notification sender ID
        dataMap.put(FirebaseConstants.KEY_SENDER_ID, myUserDetails.getUserId());
        dataMap.put(FirebaseConstants.KEY_RECEIVER_ID, receiverUser.getUserId());
        dataMap.put(FirebaseConstants.KEY_USER_NAME, myUserDetails.getName());
        dataMap.put(FirebaseConstants.KEY_USER_GENDER, myUserDetails.getGender());
        dataMap.put(FirebaseConstants.KEY_USER_MOBILE_NO, myUserDetails.getMobileNo());
        dataMap.put(FirebaseConstants.KEY_USER_PROFILE_IMG_URL, myUserDetails.getProfileImgUrl());
        dataMap.put(FirebaseConstants.KEY_CONNECTION_ID, myInterconnection.getConnectionId());
        dataMap.put(FirebaseConstants.KEY_CHAT_ID, chatItem.getChatId());
        dataMap.put(FirebaseConstants.KEY_CHAT_CATEGORY, Integer.toString(chatItem.getChatCategory()));
        dataMap.put(FirebaseConstants.KEY_CHAT_MESSAGE, chatItem.getMessage());


        retrofitManager.sendChatNotificationCallV1(receiverUser.getFcmToken(), dataMap);
    }


    //NETWORK CALL
    public void sendMessageChat(ChatItemResponse chatItem) {
        firestoreManager.sendMessageChat(myInterconnection.getConnectionId(), chatItem);
    }

    //NETWORK CALL
    public void fetchPreviousChats() {
        isFetchingPreviousChatsNetworkCallInProgress = true;
        if (chatsList.isEmpty()) {
            firestoreManager.getChatItems(myInterconnection.getConnectionId(), myUserDetails.getUserId(), CHATS_FETCH_LIMIT);
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
            firestoreManager.getChatItems(myInterconnection.getConnectionId(), myUserDetails.getUserId(), topMostChatDocId, CHATS_FETCH_LIMIT);
        }
    }

    //NETWORK CALL
    public void updateMyIsEradicatedField(boolean isEradicated) {
        firestoreManager.updateMyIsEradicatedField(myUserDetails.getMyInterconnectionsDocId(), receiverUser.getUserId(), isEradicated);
    }

    //NETWORK CALL
    public void updateReceiversIsEradicatedField(boolean isEradicated) {
        firestoreManager.updateReceiversIsEradicatedField(receiverUser.getMyInterconnectionsDocId(), myUserDetails.getUserId(), isEradicated);
    }

    //NETWORK CALL
    public void updateChatStatus(int newChatStatus, String chatId) {
        firestoreManager.updateChatStatus(newChatStatus, chatId, getConnectionId());
    }

    //NETWORK CALL
    public void updateChatStatus(int newChatStatus, ArrayList<String> chatIdList) {
        if (!chatIdList.isEmpty()) {
            firestoreManager.updateChatsStatus(newChatStatus, chatIdList, getConnectionId(), null);
        }
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
            case FirebaseConstants.GET_PREVIOUS_CHATS_CALL: {
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
            case FirebaseConstants.GET_PREVIOUS_CHATS_CALL: {
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
        updateMyTypingStatus(false);
        realtimeDbManager.removeUserAvailabilityListener(receiverUser.getUserId(), userAvailabilityListener);
        detachChatsCollectionListener();
        detachChatParticipantListener();
    }
}
