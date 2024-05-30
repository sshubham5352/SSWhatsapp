package com.example.sswhatsapp.retrofit;

public class RetrofitConstants {
    //------------------------ JSON CONSTANTS ------------------------//
    public static final String JSON_ARRAY_RESULTS = "results";
    public static final String JSON_FAILURE = "failure";
    public static final String JSON_ERROR = "error";

    //------------------------ FCM CONSTANTS ------------------------//
    public static final String REMOTE_CHAT_AUTHORIZATION = "Authorization";
    public static final String REMOTE_CHAT_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";
    public static final String FCM_SERVER_KEY = "AAAAnK60754:APA91bEhK797jXC7M-s5h-nWJTmb6F51LsHOkA2syHtwR31ldsitFOKABqpEdqKu7XOK_owICeIxQJZAG1ycCOdstOJWwHnTtz24s4sk4-vY02yMEGAbbKrHWER20iKi1gjjq5szDM3J";


    //------------------------ NOTIFICATION CONSTANTS ------------------------//
    public static final String NOTIFICATION_TYPE = "notification_type";


    //------------------------ RETROFIT CONSTANTS ------------------------//

    //--------- DATA ITEM CODES ---------//


    //--------- NETWORK CALLS ---------//
    public static final String NETWORK_CALL = "Retrofit_Network_Call";
    public static final String NETWORK_CALL_SUCCESS = "onRetrofitNetworkCallSuccess: ";
    public static final String NETWORK_CALL_FAILURE = "onRetrofitNetworkCallFailure: ";


    //---- CALL CODES ----//
    public static final int SEND_CHAT_NOTIFICATION_CALL = 2001;


    //---- ERROR MESSAGES ----//
    public static final String GENERAL_ERROR = "Something went wrong";
    public static final String UNAUTHORIZED_CALL_ERROR = "Unauthorized Call";   //occurs on code: 401
    public static final String INVALID_REQUEST_ERROR = "Invalid Request";       //occurs on code: 404
}
