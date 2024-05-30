package com.example.sswhatsapp.utils;

public class Constants {
    //------------------------ GENERAL CONSTANTS ------------------------//
    public static final int NULL_VALUE = -1;


    //------------------------ LOG.D TAGS ------------------------//
    public static final String APP_TAG = "LogSSWhatsApp";


    //------------------------ TOAST CONSTANTS ------------------------//
    public static final String TOAST_SOMETHING_WENT_WRONG = "Something went wrong";


    //------------------------ INTENT CONSTANTS ------------------------//
    public static final String INTENT_TYPE_IMAGE = "image/*";
    public static final int INTENT_CODE_REQUEST_IMG = 101;
    public static final String INTENT_USER_DETAILS_EXTRA = "201";
    public static final String INTENT_MY_INTERCONNECTION_EXTRA = "202";
    public static final String INTENT_RECEIVERS_INTERCONNECTION_EXTRA = "203";
    public static final String ARRIVED_FROM_NOTIFICATION_EXTRA = "204";


    //------------------------ RecyclerView CONSTANTS ------------------------//
    public static final int RV_PAGE_ITEMS_COUNT = 30;


    //------------------------ VALUES CONSTANTS ------------------------//
    //--------- GENDER ---------//
    public static final String GENDER_MALE = "male";
    public static final String GENDER_FEMALE = "female";
    public static final String GENDER_OTHERS = "others";


    //------------------------ CHAT CONSTANTS ------------------------//
    //--------- CATEGORY ---------//
    public static final int CHAT_CATEGORY_MSG = 101;
    public static final int CHAT_CATEGORY_IMG = 102;

    //--------- CHAT STATUS ---------//
    public static final int CHAT_STATUS_PENDING = 0;
    public static final int CHAT_STATUS_SENT = 1;
    public static final int CHAT_STATUS_RECEIVED = 2;
    public static final int CHAT_STATUS_READ = 3;
    public static final int CHAT_STATUS_HALTED = 4;

    //------- LAYOUT TYPE -------//
    public static final int LAYOUT_TYPE_BANNER_DATE = 100;
    public static final int LAYOUT_TYPE_CHAT_MSG_SENT = 101;
    public static final int LAYOUT_TYPE_CHAT_MSG_RECEIVED = 102;


    //------------------------ NOTIFICATION CONSTANTS ------------------------//
    public static final String NOTIFICATIONS_TAG = "SS WHATSAPP";
    public static final String NOTIFICATIONS_GROUP_KEY = "com.example.sswhatsapp";
    public static final String CHAT_CHANNEL_ID = "chat_message";
    public static final int GROUP_SUMMERY_ID = 201;
    public static final String KEY_CHAT_REPLY = "chat_reply";

    //------- ACTIONS -------//
    public static final String NOTIFICATION_ACTION_ID = "notification_action";
    public static final int NOTIFICATION_ACTION_REPLY = 501;
    public static final int NOTIFICATION_ACTION_MARK_AS_READ = 502;
}
