package com.example.sswhatsapp.daos;

import android.content.Context;
import android.widget.Toast;

import com.example.sswhatsapp.firebase.FirebaseConstants;
import com.example.sswhatsapp.firebase.FirestoreManager;
import com.example.sswhatsapp.firebase.FirestoreNetworkCallListener;
import com.example.sswhatsapp.listeners.HomeDaoListener;
import com.example.sswhatsapp.models.MyInterconnectionRvItem;
import com.example.sswhatsapp.models.responses.ChatItemResponse;
import com.example.sswhatsapp.models.responses.InterConnectionResponse;
import com.example.sswhatsapp.models.responses.UserDetailsResponse;
import com.example.sswhatsapp.providers.ContactsProvider;
import com.example.sswhatsapp.utils.Constants;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class HomeDao implements FirestoreNetworkCallListener {
    //FIELDS
    public final Context mContext;
    private final FirestoreManager firestoreManager;
    private final HomeDaoListener mListener;
    private final UserDetailsResponse myUserDetails;
    private final LinkedList<MyInterconnectionRvItem> myInterconnectionsList;
    boolean allLastChatFetched;

    //FIRESTORE LISTENERS
    EventListener<QuerySnapshot> chatCollectionListener;
    EventListener<QuerySnapshot> myInterconnectionsListener;
    HashMap<String, ListenerRegistration> chatListenersRegistrationMap;
    ListenerRegistration myInterconnectionListenerRegistration;


    //CONSTRUCTOR
    public HomeDao(Context context, UserDetailsResponse myUserDetails, HomeDaoListener listener) {
        this.mContext = context;
        this.mListener = listener;
        this.myUserDetails = myUserDetails;
        firestoreManager = new FirestoreManager(context, this);
        chatListenersRegistrationMap = new HashMap<>();
        myInterconnectionsList = new LinkedList<>();
        allLastChatFetched = false;

        //init Firestore listener
        initChatsCollectionListener();
        initMyInterconnectionListener();
    }

    private void initChatsCollectionListener() {
        chatCollectionListener = (value, error) -> {
            if (error != null || value == null) {
                return;
            }
            for (DocumentChange doc : value.getDocumentChanges()) {
                ChatItemResponse chatItem = doc.getDocument().toObject(ChatItemResponse.class);
                switch (doc.getType()) {
                    case ADDED: {
                        newChatItemReceived(chatItem);
                        break;
                    }

                    case MODIFIED: {
                        chatItemModified(chatItem);
                        break;
                    }
                }
            }
        };
    }

    private void initMyInterconnectionListener() {
        myInterconnectionsListener = (value, error) -> {
            if (error != null || value == null) {
                return;
            }
            for (DocumentChange doc : value.getDocumentChanges()) {
                InterConnectionResponse interconnectionResponse = doc.getDocument().toObject(InterConnectionResponse.class);
                switch (doc.getType()) {
                    case ADDED: {
                        for (MyInterconnectionRvItem myInterconnectionRvItem : myInterconnectionsList) {
                            if (myInterconnectionRvItem.getConnectionId().matches(interconnectionResponse.connectionId)) {
                                //if interconnection already exists then simply return
                                return;
                            }
                        }
                        newInterconnectionCreated(interconnectionResponse);

                        break;
                    }

                    case MODIFIED: {
                        for (MyInterconnectionRvItem myInterconnectionRvItem : myInterconnectionsList) {
                            if (myInterconnectionRvItem.getConnectionId().matches(interconnectionResponse.connectionId)) {
                                //if interconnection already exists then simply return
                                // TODO: 18-06-2024  if isEradicated turned true then remove the item
                                return;
                            }
                        }

                        break;
                    }
                }
            }
        };
    }

    public void attachChatCollectionListeners() {
        if (!allLastChatFetched) {
            return;
        }
        for (MyInterconnectionRvItem myInterconnection : myInterconnectionsList) {
            if (!chatListenersRegistrationMap.containsKey(myInterconnection.getConnectionId()) && myInterconnection.getLastChatItem() != null) {
                ListenerRegistration listenerRegistration = firestoreManager.addIndividualChatConnectionListener(myInterconnection.getConnectionId(), chatCollectionListener, myInterconnection.getLastChatItem().getTimeStamp());
                chatListenersRegistrationMap.put(myInterconnection.getConnectionId(), listenerRegistration);
            }
        }
    }

    public void attachMyInterconnectionsListener() {
//        if (myInterconnectionListenerRegistration != null || !allLastChatFetched) {
//            return;
//        }
//        String latestModifiedDate = "1999";
//        if (myInterconnectionsList.isEmpty()) {
//            latestModifiedDate = TimeHandler.getCurrentTimeStamp();
//        } else {
//            for (MyInterconnectionRvItem myInterconnection : myInterconnectionsList) {
//                if (myInterconnection.getModifiedAt().compareTo(latestModifiedDate) > 0) {
//                    latestModifiedDate = myInterconnection.getModifiedAt();
//                }
//            }
//        }
//        myInterconnectionListenerRegistration = firestoreManager.addMyInterconnectionsListener(myUserDetails.myInterconnectionsDocId, myInterconnectionsListener, latestModifiedDate);
    }

    //CALL FROM CHAT_COLLECTION_LISTENER
    private void newChatItemReceived(ChatItemResponse chatItem) {
        MyInterconnectionRvItem myInterconnection;
        for (int i = 0; i < myInterconnectionsList.size(); i++) {
            myInterconnection = myInterconnectionsList.get(i);

            if (myInterconnection.getConnectionId().matches(chatItem.getConnectionId())) {
                if (myInterconnection.getLastChatItem().getChatId().matches(chatItem.getChatId())) {
                    //same chat item fetched(as the listener fetches the last chat item too)
                    if (myInterconnection.getLastChatItem().chatStatus == Constants.CHAT_STATUS_READ) {
                        myInterconnection.setUnseenChatCount(MyInterconnectionRvItem.NULL_CHAT_COUNT);
                        mListener.myInterconnectionItemUpdated(i);
                        return;
                    }
                    return;
                }

                //new chat item
                if (chatItem.senderId.matches(myUserDetails.userId)) {
                    //Chat sent by me
                    myInterconnection.setUnseenChatCount(MyInterconnectionRvItem.NULL_CHAT_COUNT);
                } else {
                    //Chat sent by other user
                    if (chatItem.chatStatus == Constants.CHAT_STATUS_READ)
                        myInterconnection.setUnseenChatCount(MyInterconnectionRvItem.NULL_CHAT_COUNT);
                    else
                        myInterconnection.incrementUnseenChatCount();
                }

                myInterconnection.setLastChatItem(chatItem);
                myInterconnectionsList.add(0, myInterconnectionsList.remove(i));
                mListener.myInterconnectionItemUpdated(i);
                mListener.moveInterconnectionItem(i, 0);
                break;
            }
        }
    }

    //CALL FROM CHAT_COLLECTION_LISTENER
    private void chatItemModified(ChatItemResponse chatItem) {
        MyInterconnectionRvItem myInterconnection;
        for (int i = 0; i < myInterconnectionsList.size(); i++) {
            myInterconnection = myInterconnectionsList.get(i);

            if (myInterconnection.getConnectionId().matches(chatItem.getConnectionId())) {
                if (myInterconnection.getLastChatItem().getChatId().matches(chatItem.getChatId())) {
                    if (chatItem.senderId.matches(myUserDetails.userId)) {
                        myInterconnection.setLastChatItem(chatItem);
                    } else if (chatItem.chatStatus == Constants.CHAT_STATUS_READ) {
                        myInterconnection.setUnseenChatCount(MyInterconnectionRvItem.NULL_CHAT_COUNT);
                    }
                    mListener.myInterconnectionItemUpdated(i);
                }
                break;
            }
        }
    }

    //CALL FROM INTERCONNECTIONS_LISTENER
    private void newInterconnectionCreated(InterConnectionResponse interconnectionResponse) {
        myInterconnectionsList.addFirst(new MyInterconnectionRvItem(interconnectionResponse));
        mListener.newInterconnectionAdded(0);

        getInterconnectedUserDetails(interconnectionResponse.connectionWith);
    }

    //CALL FROM CONTROLLER
    public void addInterConnections(ArrayList<InterConnectionResponse> interConnectionResponseList) {
        for (int i = 0; i < interConnectionResponseList.size(); i++) {
            myInterconnectionsList.add(new MyInterconnectionRvItem(interConnectionResponseList.get(i)));
        }
    }

    //GETTER
    public LinkedList<MyInterconnectionRvItem> getMyInterconnectionsList() {
        return myInterconnectionsList;
    }

    //GETTER
    public int getInterconnectionListSize() {
        return myInterconnectionsList.size();
    }

    //GETTER
    public UserDetailsResponse getMyUserDetails() {
        return myUserDetails;
    }


    //NETWORK CALL
    public void fetchMyInterconnectionsList() {
        firestoreManager.getMyInterconnectionsList(myUserDetails.myInterconnectionsDocId, true);
    }

    //NETWORK CALL
    public void getInterconnectedUsersDetails() {
        List<String> userIdsList = new ArrayList<>();
        for (int i = 0; i < myInterconnectionsList.size(); i++) {
            userIdsList.add(myInterconnectionsList.get(i).getConnectionWith());
        }
        firestoreManager.getUsersById(userIdsList);
    }

    //NETWORK CALL
    public void getInterconnectedUserDetails(String userId) {
        firestoreManager.getUserById(userId);
    }

    //NETWORK CALL
    public void getLastChatItemOfAllInterconnections() {
        for (MyInterconnectionRvItem interconnection : myInterconnectionsList) {
            firestoreManager.getLastChatItem(interconnection.getConnectionId(), myUserDetails.getUserId());
        }
    }

    //NETWORK CALL
    public void getLastChatItemOfInterconnection(String connectionId) {
        firestoreManager.getLastChatItem(connectionId, myUserDetails.getUserId());
    }

    //NETWORK CALL
    public void getUnseenChatCountOfAllInterconnections() {
        for (MyInterconnectionRvItem interconnection : myInterconnectionsList) {
            firestoreManager.getUnseenChatsCount(interconnection.getConnectionId(), myUserDetails.getUserId());
        }
    }

    //NETWORK CALL
    public void getUnseenChatCountOfInterconnection(String connectionId) {
        firestoreManager.getUnseenChatsCount(connectionId, myUserDetails.getUserId());
    }

    @Override
    public void onFirestoreNetworkCallSuccess(Object response, int serviceCode) {
        switch (serviceCode) {
            case (FirebaseConstants.GET_MY_INTERCONNECTIONS_LIST_CALL): {
                QuerySnapshot querySnapshot = (QuerySnapshot) response;
                InterConnectionResponse interConnectionResponse;
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    interConnectionResponse = doc.toObject(InterConnectionResponse.class);
                    myInterconnectionsList.add(new MyInterconnectionRvItem(interConnectionResponse));
                }

                mListener.myInterconnectionsListFetched();
                break;
            }

            case (FirebaseConstants.GET_USERS_BY_ID_LIST_CALL): {
                QuerySnapshot querySnapshot = (QuerySnapshot) response;

                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    UserDetailsResponse userDetailsResponse = doc.toObject(UserDetailsResponse.class);
                    for (int i = 0; i < myInterconnectionsList.size(); i++) {
                        if (myInterconnectionsList.get(i).getConnectionWith().matches(userDetailsResponse.getUserId())) {
                            // TODO: 17-06-2024  Do the LocalPhoneName work in constructor on the UserDetailsResponse
                            userDetailsResponse.setLocalPhoneName(ContactsProvider.getContactName(userDetailsResponse.getMobileNo(), mContext));
                            myInterconnectionsList.get(i).setReceiverUserDetails(userDetailsResponse);
                            break;
                        }
                    }
                }

                mListener.allInterconnectedUsersDetailsAdded();
                break;
            }

            case (FirebaseConstants.GET_USER_BY_ID_CALL): {
                DocumentSnapshot doc = (DocumentSnapshot) response;
                UserDetailsResponse userDetailsResponse = doc.toObject(UserDetailsResponse.class);

                for (int i = 0; i < myInterconnectionsList.size(); i++) {
                    if (myInterconnectionsList.get(i).getConnectionWith().matches(userDetailsResponse.getUserId())) {
                        myInterconnectionsList.get(i).setReceiverUserDetails(userDetailsResponse);
                        mListener.interconnectedUserDetailsAdded(i);
                        break;
                    }
                }
                break;
            }

            case (FirebaseConstants.GET_LAST_CHAT_ITEM_CALL): {
                QuerySnapshot querySnapshot = (QuerySnapshot) response;
                ChatItemResponse chatItemResponse = ((QuerySnapshot) response).getDocuments().get(0).toObject(ChatItemResponse.class);

                allLastChatFetched = true;
                for (int i = 0; i < myInterconnectionsList.size(); i++) {
                    if (myInterconnectionsList.get(i).getConnectionId().matches(chatItemResponse.getConnectionId())) {
                        myInterconnectionsList.get(i).setLastChatItem(chatItemResponse);
                        mListener.myInterconnectionItemUpdated(i);
                    }
                    if (myInterconnectionsList.get(i).getLastChatItem() == null) {
                        allLastChatFetched = false;
                    }
                }

                if (allLastChatFetched) {
                    mListener.sortInterconnectionsList();
                    attachChatCollectionListeners();
                    attachMyInterconnectionsListener();
                }
                break;
            }

            case (FirebaseConstants.GET_UNSEEN_CHATS_COUNT_CALL): {
                MyInterconnectionRvItem.UnseenChatsCountResponse unseenChatsCountResponse = (MyInterconnectionRvItem.UnseenChatsCountResponse) response;

                for (int i = 0; i < myInterconnectionsList.size(); i++) {
                    if (myInterconnectionsList.get(i).getConnectionId().matches(unseenChatsCountResponse.getConnectionId())) {
                        myInterconnectionsList.get(i).setUnseenChatCount(unseenChatsCountResponse.getUnseenChatCount());
                        mListener.myInterconnectionItemUpdated(i);
                        break;
                    }
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
        Toast.makeText(mContext, FirebaseConstants.GENERAL_ERROR, Toast.LENGTH_SHORT).show();
    }

    public void detachChatCollectionListeners() {
        for (ListenerRegistration listenerRegistration : chatListenersRegistrationMap.values()) {
            listenerRegistration.remove();
        }
        chatListenersRegistrationMap.clear();
    }

    public void detachMyInterconnectionsListener() {
        if (myInterconnectionListenerRegistration == null) {
            return;
        }
        myInterconnectionListenerRegistration.remove();
        myInterconnectionListenerRegistration = null;
    }
}
