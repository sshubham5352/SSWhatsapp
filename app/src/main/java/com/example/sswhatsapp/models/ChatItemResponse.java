package com.example.sswhatsapp.models;

import com.example.sswhatsapp.firebase.FirebaseConstants;
import com.google.firebase.firestore.PropertyName;

public class ChatItemResponse {
    //Fields
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
    @PropertyName(FirebaseConstants.KEY_MESSAGE)
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


    public ChatItemResponse() {
        /*
         * Empty Constructor
         * required for firestore to convert document to pojo java class
         */
    }

    public ChatItemResponse(int chatCategory, boolean isStared, boolean isDeletedBySender,
                            boolean isDeletedByReceiver, int chatStatus, String message,
                            String timeStamp, String senderId, String receiverId) {
        this.chatCategory = chatCategory;
        this.isStared = isStared;
        this.isDeletedBySender = isDeletedBySender;
        this.isDeletedByReceiver = isDeletedByReceiver;
        this.chatStatus = chatStatus;
        this.message = message;
        this.timeStamp = timeStamp;
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

    public ChatItemResponse(int chatCategory, int chatStatus, String message, String timeStamp, String senderId, String receiverId) {
        this.chatCategory = chatCategory;
        this.isStared = false;
        this.isDeletedBySender = false;
        this.isDeletedByReceiver = false;
        this.chatStatus = chatStatus;
        this.message = message;
        this.timeStamp = timeStamp;
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

    //CHAT MESSAGE CONSTRUCTOR
    public ChatItemResponse(int chatCategory, int chatStatus, String senderId, String receiverId, String message, String timeStamp, boolean isStared, boolean isDeletedBySender, boolean isDeletedByReceiver) {
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

    //DATE BANNER CONSTRUCTOR
    public ChatItemResponse(int chatCategory, String timeStamp) {
        chatId = "";
        this.chatCategory = chatCategory;
        this.timeStamp = timeStamp;
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
