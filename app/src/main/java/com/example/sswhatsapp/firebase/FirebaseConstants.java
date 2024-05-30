package com.example.sswhatsapp.firebase;

public class FirebaseConstants {
    //------------------------ FirebaseFirestore ------------------------//


    //--------- DATABASE FOLDERS ---------//
    public static final String USER_PROFILE_IMAGES_FOLDER = "User Profile Images/";


    //--------- COLLECTIONS ---------//
    public static final String COLLECTION_USERS = "Users";
    public static final String COLLECTION_USER_INTERCONNECTIONS = "User Interconnections";
    public static final String COLLECTION_CONNECTIONS = "Connections";

    //------ NESTED COLLECTIONS ------//
    public static final String COLLECTION_MY_INTERCONNECTIONS = "My Interconnections";
    public static final String COLLECTION_PARTICIPANTS = "Participants";
    public static final String COLLECTION_CHATS = "Chats";

    //--------- DOCUMENTS ---------//
    public static final String SUFFIX_DOC_USER_CONNECTIONS = "_interconnections";


    //--- Document FIELDS (KEYS) ---//

    //---- USER DETAILS ----//
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_FCM_TOKEN = "fcm_token";
    public static final String KEY_USER_NAME = "name";
    public static final String KEY_USER_EMAIL_ID = "email_id";
    public static final String KEY_USER_MOBILE_NO = "mobile_no";
    public static final String KEY_USER_GENDER = "gender";
    public static final String KEY_USER_PASSWORD = "password";
    public static final String KEY_USER_TAGLINE = "tagline";
    public static final String KEY_ACCOUNT_CREATED_ON = "account_created_on";
    public static final String KEY_USER_PROFILE_IMG_URL = "img_profile";
    public static final String KEY_CREATED_ON = "created_on";
    public static final String KEY_IS_ONLINE = "is_online";


    //---- CHAT ITEM DETAILS ----//
    public static final String KEY_CHAT_CATEGORY = "chat_category";
    public static final String KEY_CHAT_ID = "chat_id";
    public static final String KEY_SENDER_ID = "sender_id";
    public static final String KEY_RECEIVER_ID = "receiver_id";
    public static final String KEY_IS_STARED = "is_stared";
    public static final String KEY_IS_DELETED_BY_SENDER = "is_deleted_by_sender";
    public static final String KEY_IS_DELETED_BY_RECEIVER = "is_deleted_by_receiver";
    public static final String KEY_CHAT_STATUS = "chat_status";
    public static final String KEY_CHAT_MESSAGE = "message";
    public static final String KEY_TIME_STAMP = "time_stamp";
    public static final String KEY_IMG_URL = "img_url";


    //---- CONNECTION DETAILS ----//
    public static final String KEY_CONNECTION_ID = "connection_id";
    public static final String KEY_MY_INTERCONNECTIONS_DOC_ID = "my_interconnections_doc_id";
    public static final String KEY_CONNECTIONS_WITH = "connection_with";
    public static final String KEY_CONNECTION_WITH = "connection_with";
    public static final String KEY_IS_ERADICATED = "is_eradicated";
    public static final String KEY_IS_LIVE = "is_live";
    public static final String KEY_IS_TYPING = "is_typing";


    //---- MISC KEYS ----//
    public static final String KEY_DOC_ID = "doc_id";


    //--------- NETWORK CALLS ---------//
    public static final String NETWORK_CALL = "Firestore_Network_Call";
    public static final String NETWORK_CALL_SUCCESS = "onFirestoreNetworkCallSuccess: ";
    public static final String NETWORK_CALL_FAILURE = "onFirestoreNetworkCallFailure: ";


    //---- CALL CODES ----//
    public static final int UPLOAD_USER_PROFILE_IMG_CALL = 201;
    public static final int SIGN_UP_USER_CALL = 202;
    public static final int GET_USER_BY_ID_CALL = 203;
    public static final int GET_USER_BY_MOBILE_NO_CALL = 204;
    public static final int GET_USER_BY_EMAIL_ID_CALL = 205;
    public static final int GET_ALL_USERS_EXCEPT_ME_CALL = 206;
    public static final int CREATE_NEW_CONNECTION_CALL = 207;
    public static final int GET_MY_INTERCONNECTION_CALL = 208;
    public static final int CREATE_MY_INTERCONNECTION_CALL = 209;
    public static final int CREATE_RECEIVERS_INTERCONNECTION_CALL = 210;
    public static final int DELETE_CONNECTION_CALL = 211;
    public static final int SEND_MESSAGE_CHAT_CALL = 212;
    public static final int FETCH_ALL_CHATS_CALL = 213;
    public static final int UPDATE_MY_IS_ERADICATED_FIELD_CALL = 214;
    public static final int UPDATE_RECEIVERS_IS_ERADICATED_FIELD_CALL = 215;
    public static final int GET_FCM_TOKEN_CALL = 216;
    public static final int UPDATE_FIELD_FCM_TOKEN_CALL = 217;
    public static final int UPDATE_CHATS_STATUS_CALL = 218;
    public static final int IS_PARTICIPANT_LIVE_ON_CHAT_CALL = 219;


    //---- ERROR MESSAGES ----//
    public static final String GENERAL_ERROR = "Something went wrong";
    public static final String IMAGE_NOT_UPLOADED_ERROR = "Error in uploading image";
}
