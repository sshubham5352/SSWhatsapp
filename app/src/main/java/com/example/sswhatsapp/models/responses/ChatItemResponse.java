package com.example.sswhatsapp.models.responses;

import com.example.sswhatsapp.firebase.FirebaseConstants;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.Map;

public class ChatItemResponse implements Serializable {
    //Fields
    public static final String CHAT_DATE_BANNER_ID = "DBID";
    @PropertyName(FirebaseConstants.KEY_CONNECTION_ID)
    public String connectionId;
    @PropertyName(FirebaseConstants.KEY_CHAT_CATEGORY)
    public int chatCategory;
    @PropertyName(FirebaseConstants.KEY_CHAT_STATUS)
    public int chatStatus;
    @PropertyName(FirebaseConstants.KEY_CHAT_ID)
    public String chatId;
    @PropertyName(FirebaseConstants.KEY_SENDER_ID)
    public String senderId;
    @PropertyName(FirebaseConstants.KEY_RECEIVER_ID)
    public String receiverId;
    @PropertyName(FirebaseConstants.KEY_CHAT_MESSAGE)
    public String message;
    @PropertyName(FirebaseConstants.KEY_TIME_STAMP)
    public String timeStamp;
    @PropertyName(FirebaseConstants.KEY_IMG_URL)
    public String imgUrl;
    @PropertyName(FirebaseConstants.KEY_IS_STARED)
    public boolean isStared;
    @PropertyName(FirebaseConstants.KEY_IS_DELETED_BY_SENDER)
    public boolean isDeletedBySender;
    @PropertyName(FirebaseConstants.KEY_IS_DELETED_BY_RECEIVER)
    public boolean isDeletedByReceiver;

    //non Firebase Properties
    public String dateBannerTitle;

    //CONSTRUCTOR
    public ChatItemResponse() {
        /*
         * Empty Constructor
         * required for firestore to convert document to pojo java class
         */
    }


    //CONSTRUCTOR: for chat msg
    public ChatItemResponse(String connectionId, int chatCategory, int chatStatus, String senderId, String receiverId, String message, String timeStamp, boolean isStared, boolean isDeletedBySender, boolean isDeletedByReceiver) {
        this.connectionId = connectionId;
        this.chatCategory = chatCategory;
        this.chatStatus = chatStatus;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timeStamp = timeStamp;
        this.isStared = isStared;
        this.isDeletedBySender = isDeletedBySender;
        this.isDeletedByReceiver = isDeletedByReceiver;
    }

    //CONSTRUCTOR: for date banner
    public ChatItemResponse(int chatCategory, String timeStamp, String dateBannerTitle) {
        chatId = CHAT_DATE_BANNER_ID;
        this.chatCategory = chatCategory;
        this.timeStamp = timeStamp;
        this.dateBannerTitle = dateBannerTitle;
    }

    //CONSTRUCTOR: for FCM service
    public ChatItemResponse(Map<String, String> dataMap) {
        connectionId = dataMap.get(FirebaseConstants.KEY_CONNECTION_ID);
        chatId = dataMap.get(FirebaseConstants.KEY_CHAT_ID);
        chatCategory = Integer.parseInt(dataMap.get(FirebaseConstants.KEY_CHAT_CATEGORY));
        senderId = dataMap.get(FirebaseConstants.KEY_SENDER_ID);
        receiverId = dataMap.get(FirebaseConstants.KEY_RECEIVER_ID);
        message = dataMap.get(FirebaseConstants.KEY_CHAT_MESSAGE);
    }

    public String getConnectionId() {
        return connectionId;
    }

    public String getChatId() {
        return chatId;
    }

    public int getChatCategory() {
        return chatCategory;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public boolean isStared() {
        return isStared;
    }

    public boolean isDeletedBySender() {
        return isDeletedBySender;
    }

    public boolean isDeletedByReceiver() {
        return isDeletedByReceiver;
    }

    public int getChatStatus() {
        return chatStatus;
    }

    public String getMessage() {
        return message;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getDateBannerTitle() {
        return dateBannerTitle;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public void removeChatId() {
        this.chatId = null;
    }

    public void setChatStatus(int chatStatus) {
        this.chatStatus = chatStatus;
    }
}
