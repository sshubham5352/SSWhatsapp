package com.example.sswhatsapp.firebase;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;

import com.example.sswhatsapp.models.ChatItemResponse;
import com.example.sswhatsapp.models.InterConnection;
import com.example.sswhatsapp.models.UserDetailsResponse;
import com.example.sswhatsapp.notificatons.ChatNotificationsManager;
import com.example.sswhatsapp.utils.Constants;
import com.example.sswhatsapp.utils.FirestoreHelper;
import com.example.sswhatsapp.utils.Helper;
import com.example.sswhatsapp.utils.SessionManager;
import com.example.sswhatsapp.utils.TimeHandler;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class FirestoreManager {
    private final FirebaseFirestore firestoreDb;
    private FirebaseStorage firebaseStorage;
    private final FirestoreNetworkCallListener mListener;
    private ProgressDialog progressDialog;
    private int noOfCallsInProgress;


    //Constructor FOR SERVICE
    public FirestoreManager(FirestoreNetworkCallListener listener) {
        firestoreDb = FirebaseClients.getFirestoreDb();
        mListener = listener;
    }

    //Constructor
    public FirestoreManager(Context context, FirestoreNetworkCallListener listener) {
        firestoreDb = FirebaseClients.getFirestoreDb();
        mListener = listener;

        //init progressBar
        progressDialog = new ProgressDialog(context);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        noOfCallsInProgress = 0;
    }

    private void initFirebaseStorage() {
        if (firebaseStorage == null) firebaseStorage = FirebaseClients.getFirebaseStorage();
    }

    //SETTING LISTENER
    public ListenerRegistration setIndividualConnectionListener(String connectionId, EventListener<QuerySnapshot> listener, String lastReceivedChatTimeStamp) {
        ListenerRegistration listenerRegistration =
                firestoreDb.collection(FirebaseConstants.COLLECTION_CONNECTIONS)
                        .document(connectionId)
                        .collection(FirebaseConstants.COLLECTION_CHATS)
                        .whereGreaterThan(FirebaseConstants.KEY_TIME_STAMP, lastReceivedChatTimeStamp)
                        .addSnapshotListener(listener);
        return listenerRegistration;
    }

    //SETTING LISTENER
    public ListenerRegistration setConnectionParticipantListener(String connectionId, String participantId, EventListener<QuerySnapshot> listener) {
        ListenerRegistration listenerRegistration =
                firestoreDb.collection(FirebaseConstants.COLLECTION_CONNECTIONS)
                        .document(connectionId)
                        .collection(FirebaseConstants.COLLECTION_PARTICIPANTS)
                        .whereEqualTo(FirebaseConstants.KEY_USER_ID, participantId)
                        .addSnapshotListener(listener);
        return listenerRegistration;
    }

    //SETTING LISTENER
    public ListenerRegistration setUserDocListener(String userId, EventListener<DocumentSnapshot> listener) {
        ListenerRegistration listenerRegistration =
                firestoreDb.collection(FirebaseConstants.COLLECTION_USERS)
                        .document(userId)
                        .addSnapshotListener(listener);
        return listenerRegistration;
    }

    public void getFCMToken() {
        progressDialog.setMessage("Fetching FCM Token...");
        noOfCallsInProgress++;
        progressDialog.show();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !Helper.isNill(task.getResult())) {
                mListener.onFirestoreNetworkCallSuccess(task.getResult(), FirebaseConstants.GET_FCM_TOKEN_CALL);
            } else {
                mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + task.getException().getMessage());
            }
            if ((--noOfCallsInProgress) == 0)
                progressDialog.dismiss();

        }).addOnFailureListener(e -> {
            mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.GENERAL_ERROR);
            if ((--noOfCallsInProgress) == 0)
                progressDialog.dismiss();
        });
    }

    public void uploadUserProfileImageToFirebase(Uri imageUri) {
        progressDialog.setMessage("Uploading profile image...");
        noOfCallsInProgress++;
        progressDialog.show();

        initFirebaseStorage();
        String fileName = TimeHandler.getCurrentTimeStamp();
        StorageReference storageChildRef = firebaseStorage.getReference(FirebaseConstants.USER_PROFILE_IMAGES_FOLDER).child(fileName);
        storageChildRef
                .putFile(imageUri)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        storageChildRef
                                .getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    mListener.onFirestoreNetworkCallSuccess(uri, FirebaseConstants.UPLOAD_USER_PROFILE_IMG_CALL);
                                    if ((--noOfCallsInProgress) == 0)
                                        progressDialog.dismiss();
                                })
                                .addOnFailureListener(e -> {
                                    mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + e.getMessage());
                                    if ((--noOfCallsInProgress) == 0)
                                        progressDialog.dismiss();
                                });
                    } else {
                        mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.IMAGE_NOT_UPLOADED_ERROR);
                        if ((--noOfCallsInProgress) == 0)
                            progressDialog.dismiss();
                    }

                }).addOnFailureListener(e -> {
                    mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + e.getMessage());
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                });
    }

    public void updateFcmTokenField(String userId, String fcmToken) {
        progressDialog.setMessage("Sending FCM Token...");
        noOfCallsInProgress++;
        progressDialog.show();

        firestoreDb.collection(FirebaseConstants.COLLECTION_USERS)
                .document(userId)
                .update(FirebaseConstants.KEY_FCM_TOKEN, fcmToken)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mListener.onFirestoreNetworkCallSuccess(fcmToken, FirebaseConstants.UPDATE_FIELD_FCM_TOKEN_CALL);
                    } else {
                        mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.GENERAL_ERROR);
                    }
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                }).addOnFailureListener(e -> {
                    mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + e.getMessage());
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                });
    }

    public void SignUpUser(UserDetailsResponse userDetailsResponse, String password) {
        /*
         * Signing Up Process (one Time only)
         * i) Firstly create a user connections document in "User Connections" collection
         * ii) After successful creation of the document, create a user document in "Users" collection
         *     and save the previously created document reference in this document
         * */
        createUserInterconnectionDoc(userDetailsResponse, password);
    }


    public void createUserInterconnectionDoc(UserDetailsResponse userDetailsResponse, String password) {
        progressDialog.setMessage("Signing in...");
        noOfCallsInProgress++;
        progressDialog.show();

        HashMap<String, Object> doc = new HashMap<>();
        doc.put(FirebaseConstants.KEY_CREATED_ON, userDetailsResponse.getCreatedOn());
        String docId = userDetailsResponse.getUserId() + FirebaseConstants.SUFFIX_DOC_USER_CONNECTIONS;


        firestoreDb.collection(FirebaseConstants.COLLECTION_USER_INTERCONNECTIONS)
                .document(docId)
                .set(doc)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userDetailsResponse.setMyInterconnectionsDocId(docId);
                        createUserAccount(userDetailsResponse, password);
                    } else {
                        mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.IMAGE_NOT_UPLOADED_ERROR);
                    }
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                }).addOnFailureListener(e -> {
                    mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + e.getMessage());
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                });
    }

    public void createUserAccount(UserDetailsResponse userDetailsResponse, String password) {
        progressDialog.setMessage("Signing in...");
        noOfCallsInProgress++;
        progressDialog.show();

        HashMap<String, Object> userDocument = new HashMap<>();
        userDocument.put(FirebaseConstants.KEY_USER_ID, userDetailsResponse.getUserId());
        userDocument.put(FirebaseConstants.KEY_FCM_TOKEN, userDetailsResponse.getFcmToken());
        userDocument.put(FirebaseConstants.KEY_USER_NAME, userDetailsResponse.getName());
        userDocument.put(FirebaseConstants.KEY_USER_GENDER, userDetailsResponse.getGender());
        userDocument.put(FirebaseConstants.KEY_USER_EMAIL_ID, userDetailsResponse.getEmailId());
        userDocument.put(FirebaseConstants.KEY_USER_MOBILE_NO, userDetailsResponse.getMobileNo());
        userDocument.put(FirebaseConstants.KEY_USER_TAGLINE, userDetailsResponse.getTagline());
        userDocument.put(FirebaseConstants.KEY_USER_PASSWORD, password);
        userDocument.put(FirebaseConstants.KEY_ACCOUNT_CREATED_ON, userDetailsResponse.getCreatedOn());
        userDocument.put(FirebaseConstants.KEY_USER_PROFILE_IMG_URL, userDetailsResponse.getProfileImgUrl());
        userDocument.put(FirebaseConstants.KEY_MY_INTERCONNECTIONS_DOC_ID, userDetailsResponse.getMyInterconnectionsDocId());

        firestoreDb.collection(FirebaseConstants.COLLECTION_USERS)
                .document(userDetailsResponse.getUserId())
                .set(userDocument)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mListener.onFirestoreNetworkCallSuccess(userDetailsResponse, FirebaseConstants.SIGN_UP_USER_CALL);
                    } else {
                        mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.IMAGE_NOT_UPLOADED_ERROR);
                    }
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                }).addOnFailureListener(e -> {
                    mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + e.getMessage());
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                });
    }

    public void getUserById(String userId) {
        CollectionReference usersFolderRef = firestoreDb.collection(FirebaseConstants.COLLECTION_USERS);
        Query query = usersFolderRef.whereEqualTo(FirebaseConstants.KEY_USER_ID, userId);
        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mListener.onFirestoreNetworkCallSuccess(task.getResult(), FirebaseConstants.GET_USER_BY_MOBILE_NO_CALL);
                    } else {
                        mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.GENERAL_ERROR);
                    }
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                }).addOnFailureListener(e -> {
                    mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.GENERAL_ERROR);
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                });
    }

    public void getUserByMobileNo(String mobileNo) {
        progressDialog.setMessage("Please wait...");
        noOfCallsInProgress++;
        progressDialog.show();


        CollectionReference usersFolderRef = firestoreDb.collection(FirebaseConstants.COLLECTION_USERS);
        Query query = usersFolderRef.whereEqualTo(FirebaseConstants.KEY_USER_MOBILE_NO, mobileNo);
        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mListener.onFirestoreNetworkCallSuccess(task.getResult(), FirebaseConstants.GET_USER_BY_MOBILE_NO_CALL);
                    } else {
                        mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.GENERAL_ERROR);
                    }
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                }).addOnFailureListener(e -> {
                    mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.GENERAL_ERROR);
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                });
    }

    public void getUserByEmailId(String emailId) {
        progressDialog.setMessage("Please wait...");
        noOfCallsInProgress++;
        progressDialog.show();


        CollectionReference usersFolderRef = firestoreDb.collection(FirebaseConstants.COLLECTION_USERS);
        Query query = usersFolderRef.whereEqualTo(FirebaseConstants.KEY_USER_EMAIL_ID, emailId);
        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mListener.onFirestoreNetworkCallSuccess(task.getResult(), FirebaseConstants.GET_USER_BY_EMAIL_ID_CALL);
                    } else {
                        mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.GENERAL_ERROR);
                    }
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                }).addOnFailureListener(e -> {
                    mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.GENERAL_ERROR);
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                });
    }

    public void getAllUsersExceptMe() {
        progressDialog.setMessage("Please wait...");
        noOfCallsInProgress++;
        progressDialog.show();

        CollectionReference usersFolderRef = firestoreDb.collection(FirebaseConstants.COLLECTION_USERS);
        Query query = usersFolderRef.whereNotEqualTo(FirebaseConstants.KEY_USER_MOBILE_NO, SessionManager.getUserMobileNo());
        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mListener.onFirestoreNetworkCallSuccess(task.getResult(), FirebaseConstants.GET_ALL_USERS_EXCEPT_ME_CALL);
                    } else {
                        mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.GENERAL_ERROR);
                    }
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                }).addOnFailureListener(e -> {
                    mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.GENERAL_ERROR);
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                });
    }

    public void createNewConnection(String myUserId, String otherUserId, String createdOn) {
        progressDialog.setMessage("Please wait...");
        noOfCallsInProgress++;
        progressDialog.show();

        HashMap<String, Object> doc = new HashMap<>();
        doc.put(FirebaseConstants.KEY_CREATED_ON, createdOn);
        String docId = FirestoreHelper.createConnectionName(myUserId, otherUserId);

        firestoreDb.collection(FirebaseConstants.COLLECTION_CONNECTIONS)
                .document(docId)
                .set(doc)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        addParticipantsInConnection(docId, myUserId, otherUserId);
                    } else {
                        mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.IMAGE_NOT_UPLOADED_ERROR);
                    }
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                }).addOnFailureListener(e -> {
                    mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + e.getMessage());
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                });
    }

    public void addParticipantsInConnection(String connectionId, String myUserId, String connectionWith) {
        progressDialog.setMessage("Please wait...");
        noOfCallsInProgress++;
        progressDialog.show();

        WriteBatch queriesBatch = firestoreDb.batch();

        DocumentReference participant1 = firestoreDb
                .collection(FirebaseConstants.COLLECTION_CONNECTIONS)
                .document(connectionId)
                .collection(FirebaseConstants.COLLECTION_PARTICIPANTS)
                .document(FirestoreHelper.createParticipantName(myUserId));

        DocumentReference participant2 = firestoreDb
                .collection(FirebaseConstants.COLLECTION_CONNECTIONS)
                .document(connectionId)
                .collection(FirebaseConstants.COLLECTION_PARTICIPANTS)
                .document(FirestoreHelper.createParticipantName(connectionWith));


        HashMap<String, Object> participant1Data = new HashMap<>();
        participant1Data.put(FirebaseConstants.KEY_DOC_ID, participant1.getId());
        participant1Data.put(FirebaseConstants.KEY_USER_ID, myUserId);
        participant1Data.put(FirebaseConstants.KEY_IS_TYPING, false);
        participant1Data.put(FirebaseConstants.KEY_IS_LIVE, false);

        HashMap<String, Object> participant2Data = new HashMap<>();
        participant2Data.put(FirebaseConstants.KEY_DOC_ID, participant2.getId());
        participant2Data.put(FirebaseConstants.KEY_USER_ID, connectionWith);
        participant2Data.put(FirebaseConstants.KEY_IS_TYPING, false);
        participant2Data.put(FirebaseConstants.KEY_IS_LIVE, false);


        queriesBatch.set(participant1, participant1Data);
        queriesBatch.set(participant2, participant2Data);


        // Commit the batch
        queriesBatch.commit()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        InterConnection connectionRef = new InterConnection(connectionId, connectionWith, true);
                        mListener.onFirestoreNetworkCallSuccess(connectionRef, FirebaseConstants.CREATE_NEW_CONNECTION_CALL);
                    } else {
                        mListener.onFirestoreNetworkCallFailure(connectionId, FirebaseConstants.CREATE_NEW_CONNECTION_CALL);
                    }
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                }).addOnFailureListener(e ->
                {
                    mListener.onFirestoreNetworkCallFailure(connectionId, FirebaseConstants.CREATE_NEW_CONNECTION_CALL);
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                });
    }

    public void getMyInterconnection(String myInterconnectionsDocId, String userId) {
        progressDialog.setMessage("Please wait...");
        noOfCallsInProgress++;
        progressDialog.show();

        Query query = firestoreDb.collection(FirebaseConstants.COLLECTION_USER_INTERCONNECTIONS)
                .document(myInterconnectionsDocId)
                .collection(FirebaseConstants.COLLECTION_MY_INTERCONNECTIONS)
                .whereEqualTo(FirebaseConstants.KEY_CONNECTIONS_WITH, userId);

        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mListener.onFirestoreNetworkCallSuccess(task.getResult(), FirebaseConstants.GET_MY_INTERCONNECTION_CALL);
                    } else {
                        mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.GENERAL_ERROR);
                    }
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                }).addOnFailureListener(e -> {
                    mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.GENERAL_ERROR);
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                });
    }

    public void createMyInterconnection(String myInterconnectionsDocId, InterConnection myInterconnection) {
        progressDialog.setMessage("Please wait...");
        noOfCallsInProgress++;
        progressDialog.show();

        HashMap<String, Object> doc = new HashMap<>();
        doc.put(FirebaseConstants.KEY_CONNECTION_ID, myInterconnection.getConnectionId());
        doc.put(FirebaseConstants.KEY_CONNECTION_WITH, myInterconnection.getConnectionWith());
        doc.put(FirebaseConstants.KEY_IS_ERADICATED, myInterconnection.isEradicated());

        firestoreDb.collection(FirebaseConstants.COLLECTION_USER_INTERCONNECTIONS)
                .document(myInterconnectionsDocId)
                .collection(FirebaseConstants.COLLECTION_MY_INTERCONNECTIONS)
                .add(doc)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mListener.onFirestoreNetworkCallSuccess(myInterconnection, FirebaseConstants.CREATE_MY_INTERCONNECTION_CALL);
                    } else {
                        mListener.onFirestoreNetworkCallFailure(myInterconnection, FirebaseConstants.CREATE_MY_INTERCONNECTION_CALL);
                    }
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                }).addOnFailureListener(e -> {
                    mListener.onFirestoreNetworkCallFailure(myInterconnection, FirebaseConstants.CREATE_MY_INTERCONNECTION_CALL);
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                });
    }

    public void createReceiversInterconnection(String connectionsListRef, InterConnection receiverInterconnection) {
        progressDialog.setMessage("Please wait...");
        noOfCallsInProgress++;
        progressDialog.show();

        HashMap<String, Object> doc = new HashMap<>();
        doc.put(FirebaseConstants.KEY_CONNECTION_ID, receiverInterconnection.getConnectionId());
        doc.put(FirebaseConstants.KEY_CONNECTION_WITH, receiverInterconnection.getConnectionWith());
        doc.put(FirebaseConstants.KEY_IS_ERADICATED, receiverInterconnection.isEradicated());

        firestoreDb.collection(FirebaseConstants.COLLECTION_USER_INTERCONNECTIONS)
                .document(connectionsListRef)
                .collection(FirebaseConstants.COLLECTION_MY_INTERCONNECTIONS)
                .add(doc)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mListener.onFirestoreNetworkCallSuccess(receiverInterconnection, FirebaseConstants.CREATE_RECEIVERS_INTERCONNECTION_CALL);
                    } else {
                        mListener.onFirestoreNetworkCallFailure(receiverInterconnection.getConnectionId(), FirebaseConstants.CREATE_RECEIVERS_INTERCONNECTION_CALL);
                    }
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                }).addOnFailureListener(e -> {
                    mListener.onFirestoreNetworkCallFailure(receiverInterconnection.getConnectionId(), FirebaseConstants.CREATE_RECEIVERS_INTERCONNECTION_CALL);
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                });
    }

    public void deleteConnection(String connectionId) {
        progressDialog.setMessage("Please wait...");
        noOfCallsInProgress++;
        progressDialog.show();

        firestoreDb.collection(FirebaseConstants.COLLECTION_CONNECTIONS)
                .document(connectionId)
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mListener.onFirestoreNetworkCallSuccess(task.getResult(), FirebaseConstants.DELETE_CONNECTION_CALL);
                    } else {
                        mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.GENERAL_ERROR);
                    }
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                }).addOnFailureListener(e -> {
                    mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.GENERAL_ERROR);
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                });
    }

    public void deleteConnectionsReferenceFromList(String connectionsListRef, String connectionWith) {
        progressDialog.setMessage("Please wait...");
        noOfCallsInProgress++;
        progressDialog.show();


        CollectionReference collectionRef = firestoreDb
                .collection(FirebaseConstants.COLLECTION_USER_INTERCONNECTIONS)
                .document(connectionsListRef)
                .collection(FirebaseConstants.COLLECTION_MY_INTERCONNECTIONS);

        Query query = collectionRef.whereEqualTo(FirebaseConstants.KEY_CONNECTIONS_WITH, connectionWith);
        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            collectionRef.document(document.getId()).delete();
                        }
                    }
                });
    }

    public void sendMessageChat(String connectionId, ChatItemResponse chatItem) {
        String chatId = firestoreDb.collection(FirebaseConstants.COLLECTION_CONNECTIONS)
                .document(connectionId)
                .collection(FirebaseConstants.COLLECTION_CHATS)
                .document().getId();
        chatItem.setChatId(chatId);

        HashMap<String, Object> doc = new HashMap<>();
        doc.put(FirebaseConstants.KEY_CHAT_ID, chatId);
        doc.put(FirebaseConstants.KEY_CHAT_CATEGORY, chatItem.getChatCategory());
        doc.put(FirebaseConstants.KEY_CHAT_STATUS, Constants.CHAT_STATUS_SENT);
        doc.put(FirebaseConstants.KEY_SENDER_ID, chatItem.getSenderId());
        doc.put(FirebaseConstants.KEY_RECEIVER_ID, chatItem.getReceiverId());
        doc.put(FirebaseConstants.KEY_CHAT_MESSAGE, chatItem.getMessage());
        doc.put(FirebaseConstants.KEY_TIME_STAMP, chatItem.getTimeStamp());
        doc.put(FirebaseConstants.KEY_IS_STARED, chatItem.isStared());
        doc.put(FirebaseConstants.KEY_IS_DELETED_BY_SENDER, chatItem.isDeletedBySender());
        doc.put(FirebaseConstants.KEY_IS_DELETED_BY_RECEIVER, chatItem.isDeletedByReceiver());

        firestoreDb.collection(FirebaseConstants.COLLECTION_CONNECTIONS)
                .document(connectionId)
                .collection(FirebaseConstants.COLLECTION_CHATS)
                .document(chatId)
                .set(doc)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mListener.onFirestoreNetworkCallSuccess(chatItem, FirebaseConstants.SEND_MESSAGE_CHAT_CALL);
                    } else {
                        mListener.onFirestoreNetworkCallFailure(chatItem, FirebaseConstants.SEND_MESSAGE_CHAT_CALL);
                    }
                }).addOnFailureListener(e -> {
                    mListener.onFirestoreNetworkCallFailure(chatItem, FirebaseConstants.SEND_MESSAGE_CHAT_CALL);
                });
    }

    public void fetchAllChats(String connectionId, String myUserId) {
        progressDialog.setMessage("Please wait...");
        noOfCallsInProgress++;
        progressDialog.show();

        CollectionReference chatsCollection = firestoreDb
                .collection(FirebaseConstants.COLLECTION_CONNECTIONS)
                .document(connectionId)
                .collection(FirebaseConstants.COLLECTION_CHATS);

        Query query = chatsCollection.where(Filter.or(
                Filter.and(Filter.equalTo(FirebaseConstants.KEY_SENDER_ID, myUserId), Filter.equalTo(FirebaseConstants.KEY_IS_DELETED_BY_SENDER, false)),
                Filter.and(Filter.equalTo(FirebaseConstants.KEY_RECEIVER_ID, myUserId), Filter.equalTo(FirebaseConstants.KEY_IS_DELETED_BY_RECEIVER, false))
        )).orderBy(FirebaseConstants.KEY_TIME_STAMP, Query.Direction.ASCENDING);
        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mListener.onFirestoreNetworkCallSuccess(task.getResult(), FirebaseConstants.FETCH_ALL_CHATS_CALL);
                    } else {
                        mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.GENERAL_ERROR);
                    }
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                }).addOnFailureListener(e -> {
                    mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.GENERAL_ERROR);
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                });
    }

    public void updateMyIsEradicatedField(String myInterconnectionsDocId, String connectionWith, boolean isEradicated) {

        firestoreDb.collection(FirebaseConstants.COLLECTION_USER_INTERCONNECTIONS)
                .document(myInterconnectionsDocId)
                .collection(FirebaseConstants.COLLECTION_MY_INTERCONNECTIONS)
                .whereEqualTo(FirebaseConstants.KEY_CONNECTIONS_WITH, connectionWith)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String docId = task.getResult().getDocuments().get(0).getId();
                        firestoreDb.collection(FirebaseConstants.COLLECTION_USER_INTERCONNECTIONS)
                                .document(myInterconnectionsDocId)
                                .collection(FirebaseConstants.COLLECTION_MY_INTERCONNECTIONS)
                                .document(docId)
                                .update(FirebaseConstants.KEY_IS_ERADICATED, isEradicated)
                                .addOnSuccessListener(unused -> {
                                    mListener.onFirestoreNetworkCallSuccess(isEradicated, FirebaseConstants.UPDATE_MY_IS_ERADICATED_FIELD_CALL);
                                })
                                .addOnFailureListener(e -> {
                                    mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.GENERAL_ERROR);
                                });

                    } else {
                        mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.GENERAL_ERROR);
                    }
                }).

                addOnFailureListener(e ->

                {
                    mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.GENERAL_ERROR);
                });
    }

    public void updateReceiversIsEradicatedField(String myInterconnectionsDocId, String connectionWith, boolean isEradicated) {

        firestoreDb.collection(FirebaseConstants.COLLECTION_USER_INTERCONNECTIONS)
                .document(myInterconnectionsDocId)
                .collection(FirebaseConstants.COLLECTION_MY_INTERCONNECTIONS)
                .whereEqualTo(FirebaseConstants.KEY_CONNECTIONS_WITH, connectionWith)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String docId = task.getResult().getDocuments().get(0).getId();
                        firestoreDb.collection(FirebaseConstants.COLLECTION_USER_INTERCONNECTIONS)
                                .document(myInterconnectionsDocId)
                                .collection(FirebaseConstants.COLLECTION_MY_INTERCONNECTIONS)
                                .document(docId)
                                .update(FirebaseConstants.KEY_IS_ERADICATED, isEradicated)
                                .addOnSuccessListener(unused -> {
                                    mListener.onFirestoreNetworkCallSuccess(isEradicated, FirebaseConstants.UPDATE_RECEIVERS_IS_ERADICATED_FIELD_CALL);
                                })
                                .addOnFailureListener(e -> {
                                    mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.GENERAL_ERROR);
                                });

                    } else {
                        mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.GENERAL_ERROR);
                    }
                }).

                addOnFailureListener(e ->

                {
                    mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.GENERAL_ERROR);
                });
    }

    public void updateChatStatus(int newChatStatus, String chatId, String connectionId) {
        firestoreDb.collection(FirebaseConstants.COLLECTION_CONNECTIONS)
                .document(connectionId)
                .collection(FirebaseConstants.COLLECTION_CHATS)
                .document(chatId)
                .update(FirebaseConstants.KEY_CHAT_STATUS, newChatStatus);
    }

    public void updateChatsStatus(int newChatStatus, ArrayList<String> chatIdsList, String connectionId, String senderId) {
        WriteBatch queriesBatch = firestoreDb.batch();

        for (String chatId : chatIdsList) {
            DocumentReference chatDoc = firestoreDb.collection(FirebaseConstants.COLLECTION_CONNECTIONS)
                    .document(connectionId)
                    .collection(FirebaseConstants.COLLECTION_CHATS)
                    .document(chatId);

            queriesBatch.update(chatDoc, FirebaseConstants.KEY_CHAT_STATUS, newChatStatus);
        }

        // Commit the batch
        queriesBatch.commit()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mListener.onFirestoreNetworkCallSuccess(senderId, FirebaseConstants.UPDATE_CHATS_STATUS_CALL);
                    } else {
                        mListener.onFirestoreNetworkCallFailure(connectionId, FirebaseConstants.UPDATE_CHATS_STATUS_CALL);
                    }
                })
                .addOnFailureListener(e -> mListener.onFirestoreNetworkCallFailure(connectionId, FirebaseConstants.UPDATE_CHATS_STATUS_CALL));
    }

    public void updateAllChatsStatusAsRead(String receiverId, String connectionId) {
        firestoreDb.collection(FirebaseConstants.COLLECTION_CONNECTIONS)
                .document(connectionId)
                .collection(FirebaseConstants.COLLECTION_CHATS)
                .whereEqualTo(FirebaseConstants.KEY_RECEIVER_ID, receiverId)
                .whereNotEqualTo(FirebaseConstants.KEY_CHAT_STATUS, Constants.CHAT_STATUS_READ)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                            firestoreDb.collection(FirebaseConstants.COLLECTION_CONNECTIONS)
                                    .document(connectionId)
                                    .collection(FirebaseConstants.COLLECTION_CHATS)
                                    .document(doc.getId())
                                    .update(FirebaseConstants.KEY_CHAT_STATUS, Constants.CHAT_STATUS_READ);
                        }
                    } else {
                        mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.GENERAL_ERROR);
                    }
                }).addOnFailureListener(e -> {
                    mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.GENERAL_ERROR);
                });
    }

    public void updateParticipantTypingStatus(boolean isTyping, String docId, String connectionId) {
        firestoreDb.collection(FirebaseConstants.COLLECTION_CONNECTIONS)
                .document(connectionId)
                .collection(FirebaseConstants.COLLECTION_PARTICIPANTS)
                .document(docId)
                .update(FirebaseConstants.KEY_IS_TYPING, isTyping);
    }

    public void updateMyOnlineStatus(boolean isOnline, String userId) {
        firestoreDb.collection(FirebaseConstants.COLLECTION_USERS)
                .document(userId)
                .update(FirebaseConstants.KEY_IS_ONLINE, isOnline);
    }

    public void updateMyLiveOnChatStatus(boolean isLive, String participantId, String connectionId) {
        firestoreDb.collection(FirebaseConstants.COLLECTION_CONNECTIONS)
                .document(connectionId)
                .collection(FirebaseConstants.COLLECTION_PARTICIPANTS)
                .document(participantId)
                .update(FirebaseConstants.KEY_IS_LIVE, isLive);
    }

    public void isParticipantLiveOnChat(String participantId, String connectionId, ChatNotificationsManager.NotificationRequirements notificationRequirementsObject) {
        firestoreDb.collection(FirebaseConstants.COLLECTION_CONNECTIONS)
                .document(connectionId)
                .collection(FirebaseConstants.COLLECTION_PARTICIPANTS)
                .document(participantId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean isLive = task.getResult().getBoolean(FirebaseConstants.KEY_IS_LIVE);
                        notificationRequirementsObject.setParticipantLive(isLive);
                        mListener.onFirestoreNetworkCallSuccess(notificationRequirementsObject, FirebaseConstants.IS_PARTICIPANT_LIVE_ON_CHAT_CALL);
                    } else {
                        mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.GENERAL_ERROR);
                    }
                }).addOnFailureListener(e -> {
                    mListener.onFirestoreNetworkCallFailure(FirebaseConstants.NETWORK_CALL_FAILURE + FirebaseConstants.GENERAL_ERROR);
                });


    }
}
