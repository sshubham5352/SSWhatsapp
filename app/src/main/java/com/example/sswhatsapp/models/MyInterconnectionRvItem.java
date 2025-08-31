package com.example.sswhatsapp.models;

import com.example.sswhatsapp.models.responses.ChatItemResponse;
import com.example.sswhatsapp.models.responses.InterConnectionResponse;
import com.example.sswhatsapp.models.responses.UserDetailsResponse;

public class MyInterconnectionRvItem implements Comparable<MyInterconnectionRvItem> {
    public static final long NULL_CHAT_COUNT = 0;
    //FIELDS
    private String connectionId;
    private String connectionWith;
    private boolean isEradicated;
    private String modifiedAt;
    private UserDetailsResponse receiverUserDetails;
    private ChatItemResponse lastChatItem;
    private long unseenChatCount;


    public MyInterconnectionRvItem() {

    }

    //CONSTRUCTOR
    public MyInterconnectionRvItem(InterConnectionResponse interConnectionResponse) {
        connectionId = interConnectionResponse.connectionId;
        connectionWith = interConnectionResponse.connectionWith;
        isEradicated = interConnectionResponse.isEradicated;
        modifiedAt = interConnectionResponse.modifiedAt;
        unseenChatCount = NULL_CHAT_COUNT;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public String getConnectionWith() {
        return connectionWith;
    }

    public boolean isEradicated() {
        return isEradicated;
    }

    public String getModifiedAt() {
        return modifiedAt;
    }

    public UserDetailsResponse getReceiverUserDetails() {
        return receiverUserDetails;
    }

    public ChatItemResponse getLastChatItem() {
        return lastChatItem;
    }

    public long getUnseenChatCount() {
        return unseenChatCount;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public void setConnectionWith(String connectionWith) {
        this.connectionWith = connectionWith;
    }

    public void setEradicated(boolean eradicated) {
        isEradicated = eradicated;
    }

    public void setModifiedAt(String modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public void setReceiverUserDetails(UserDetailsResponse receiverUserDetails) {
        this.receiverUserDetails = receiverUserDetails;
    }

    public void setLastChatItem(ChatItemResponse lastChatItem) {
        this.lastChatItem = lastChatItem;
    }

    public void setUnseenChatCount(long unseenChatCount) {
        this.unseenChatCount = unseenChatCount;
    }

    public void incrementUnseenChatCount() {
        unseenChatCount++;
    }


    @Override
    public int compareTo(MyInterconnectionRvItem item2) {
        if (lastChatItem == null || item2.lastChatItem == null) {
            return 0;
        }
        return (item2.lastChatItem.timeStamp.compareTo(lastChatItem.timeStamp));
    }

    public static class UnseenChatsCountResponse {
        //FIELDS
        String connectionId;
        long unseenChatCount;

        public UnseenChatsCountResponse(String connectionId, long unseenChatCount) {
            this.connectionId = connectionId;
            this.unseenChatCount = unseenChatCount;
        }

        public String getConnectionId() {
            return connectionId;
        }

        public long getUnseenChatCount() {
            return unseenChatCount;
        }
    }
}
