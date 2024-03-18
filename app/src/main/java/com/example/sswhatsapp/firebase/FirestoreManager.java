package com.example.sswhatsapp.firebase;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;

import com.example.sswhatsapp.models.ChatItemResponse;
import com.example.sswhatsapp.models.ConnectionsListItemResponse;
import com.example.sswhatsapp.models.UserDetailsResponse;
import com.example.sswhatsapp.utils.Constants;
import com.example.sswhatsapp.utils.FirestoreHelper;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class FirestoreManager {
    private final FirebaseFirestore firestoreDb;
    private FirebaseStorage firebaseStorage;
    private final FirestoreNetworkCallListener mListener;
    private final ProgressDialog progressDialog;
    private int noOfCallsInProgress;


    //Constructor
    public FirestoreManager(Context context, FirestoreNetworkCallListener listener) {
        firestoreDb = FirestoreClient.getFirestoreDb();
        mListener = listener;

        //init progressBar
        progressDialog = new ProgressDialog(context);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        noOfCallsInProgress = 0;
    }

    private void initFirebaseStorage() {
        if (firebaseStorage == null) firebaseStorage = FirestoreClient.getFirebaseStorage();
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

    public void SignUpUser(UserDetailsResponse userDetailsResponse, String password) {
        /*
         * Signing Up Process (one Time only)
         * i) Firstly create a user connections document in User Connections collection
         * ii) After successful creation of the document, create a user document in Users collection
         *     and save the previously created document reference in this document
         * */
        createUserConnectionsDoc(userDetailsResponse, password);
    }

    public void createUserConnectionsDoc(UserDetailsResponse userDetailsResponse, String password) {
        progressDialog.setMessage("Signing in...");
        noOfCallsInProgress++;
        progressDialog.show();

        HashMap<String, Object> doc = new HashMap<>();
        doc.put(FirebaseConstants.KEY_CREATED_ON, userDetailsResponse.getCreatedOn());
        String docId = userDetailsResponse.getUserId() + FirebaseConstants.SUFFIX_DOC_USER_CONNECTIONS;


        firestoreDb.collection(FirebaseConstants.COLLECTION_USER_CONNECTIONS)
                .document(docId)
                .set(doc)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userDetailsResponse.setMyConnectionsListRef(docId);
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
        userDocument.put(FirebaseConstants.KEY_USER_NAME, userDetailsResponse.getName());
        userDocument.put(FirebaseConstants.KEY_USER_GENDER, userDetailsResponse.getGender());
        userDocument.put(FirebaseConstants.KEY_USER_EMAIL_ID, userDetailsResponse.getEmailId());
        userDocument.put(FirebaseConstants.KEY_USER_MOBILE_NO, userDetailsResponse.getMobileNo());
        userDocument.put(FirebaseConstants.KEY_USER_TAGLINE, userDetailsResponse.getTagline());
        userDocument.put(FirebaseConstants.KEY_USER_PASSWORD, password);
        userDocument.put(FirebaseConstants.KEY_ACCOUNT_CREATED_ON, userDetailsResponse.getCreatedOn());
        userDocument.put(FirebaseConstants.KEY_USER_PROFILE_IMG_URL, userDetailsResponse.getImgProfile());
        userDocument.put(FirebaseConstants.KEY_CONNECTIONS_REF, userDetailsResponse.getMyConnectionsListRef());

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

    public void addParticipantsInConnection(String connectionId, String myUserId, String otherUserId) {
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
                .document(FirestoreHelper.createParticipantName(otherUserId));


        HashMap<String, Object> participant1Data = new HashMap<>();
        participant1Data.put(FirebaseConstants.KEY_USER_ID, myUserId);
        participant1Data.put(FirebaseConstants.KEY_IS_TYPING, false);

        HashMap<String, Object> participant2Data = new HashMap<>();
        participant2Data.put(FirebaseConstants.KEY_USER_ID, otherUserId);
        participant2Data.put(FirebaseConstants.KEY_IS_TYPING, false);


        queriesBatch.set(participant1, participant1Data);
        queriesBatch.set(participant2, participant2Data);


        // Commit the batch
        queriesBatch.commit()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ConnectionsListItemResponse connectionRef = new ConnectionsListItemResponse(connectionId, otherUserId);
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

    public void getMyConnection(String myConnectionsListRef, String userId) {
        progressDialog.setMessage("Please wait...");
        noOfCallsInProgress++;
        progressDialog.show();

        Query query = firestoreDb.collection(FirebaseConstants.COLLECTION_USER_CONNECTIONS)
                .document(myConnectionsListRef)
                .collection(FirebaseConstants.COLLECTION_MY_CONNECTIONS)
                .whereEqualTo(FirebaseConstants.KEY_CONNECTIONS_WITH, userId);

        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ConnectionsListItemResponse.CustomMyConnectionResponse response = new ConnectionsListItemResponse.CustomMyConnectionResponse(task.getResult(), userId);
                        mListener.onFirestoreNetworkCallSuccess(response, FirebaseConstants.GET_MY_CONNECTION_CALL);
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

    public void addConnectionToMyList(String myConnectionsListRef, ConnectionsListItemResponse newConnection) {
        progressDialog.setMessage("Please wait...");
        noOfCallsInProgress++;
        progressDialog.show();

        HashMap<String, Object> doc = new HashMap<>();
        doc.put(FirebaseConstants.KEY_CONNECTIONS_REF, newConnection.getConnectionId());
        doc.put(FirebaseConstants.KEY_CONNECTION_WITH, newConnection.getConnectionWith());
        doc.put(FirebaseConstants.KEY_IS_ERADICATED, newConnection.isEradicated());

        firestoreDb.collection(FirebaseConstants.COLLECTION_USER_CONNECTIONS)
                .document(myConnectionsListRef)
                .collection(FirebaseConstants.COLLECTION_MY_CONNECTIONS)
                .add(doc)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mListener.onFirestoreNetworkCallSuccess(newConnection.connectionId, FirebaseConstants.ADD_REF_TO_MY_CONNECTIONS_LIST_CALL);
                    } else {
                        mListener.onFirestoreNetworkCallFailure(newConnection, FirebaseConstants.ADD_REF_TO_MY_CONNECTIONS_LIST_CALL);
                    }
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                }).addOnFailureListener(e -> {
                    mListener.onFirestoreNetworkCallFailure(newConnection, FirebaseConstants.ADD_REF_TO_MY_CONNECTIONS_LIST_CALL);
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                });
    }

    public void addConnectionToOtherUserList(String connectionsListRef, ConnectionsListItemResponse newConnection) {
        progressDialog.setMessage("Please wait...");
        noOfCallsInProgress++;
        progressDialog.show();

        HashMap<String, Object> doc = new HashMap<>();
        doc.put(FirebaseConstants.KEY_CONNECTIONS_REF, newConnection.getConnectionId());
        doc.put(FirebaseConstants.KEY_CONNECTION_WITH, newConnection.getConnectionWith());
        doc.put(FirebaseConstants.KEY_IS_ERADICATED, newConnection.isEradicated());

        firestoreDb.collection(FirebaseConstants.COLLECTION_USER_CONNECTIONS)
                .document(connectionsListRef)
                .collection(FirebaseConstants.COLLECTION_MY_CONNECTIONS)
                .add(doc)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mListener.onFirestoreNetworkCallSuccess(newConnection.connectionId, FirebaseConstants.ADD_REF_TO_OTHERS_CONNECTIONS_LIST_CALL);
                    } else {
                        mListener.onFirestoreNetworkCallFailure(newConnection, FirebaseConstants.ADD_REF_TO_OTHERS_CONNECTIONS_LIST_CALL);
                    }
                    if ((--noOfCallsInProgress) == 0)
                        progressDialog.dismiss();
                }).addOnFailureListener(e -> {
                    mListener.onFirestoreNetworkCallFailure(newConnection, FirebaseConstants.ADD_REF_TO_OTHERS_CONNECTIONS_LIST_CALL);
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
                .collection(FirebaseConstants.COLLECTION_USER_CONNECTIONS)
                .document(connectionsListRef)
                .collection(FirebaseConstants.COLLECTION_MY_CONNECTIONS);

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
        doc.put(FirebaseConstants.KEY_MESSAGE, chatItem.getMessage());
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
                        mListener.onFirestoreNetworkCallSuccess(task.getResult(), FirebaseConstants.FETCH_ALL_MESSAGES_CALL);
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

    public void updateIsEradicatedField(String myConnectionsListRef, String connectionWith, boolean isEradicated) {

        firestoreDb.collection(FirebaseConstants.COLLECTION_USER_CONNECTIONS)
                .document(myConnectionsListRef)
                .collection(FirebaseConstants.COLLECTION_MY_CONNECTIONS)
                .whereEqualTo(FirebaseConstants.KEY_CONNECTIONS_WITH, connectionWith)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String docId = task.getResult().getDocuments().get(0).getId();
                        firestoreDb.collection(FirebaseConstants.COLLECTION_USER_CONNECTIONS)
                                .document(myConnectionsListRef)
                                .collection(FirebaseConstants.COLLECTION_MY_CONNECTIONS)
                                .document(docId)
                                .update(FirebaseConstants.KEY_IS_ERADICATED, isEradicated)
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
}
