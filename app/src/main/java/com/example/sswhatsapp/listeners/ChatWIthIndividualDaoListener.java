package com.example.sswhatsapp.listeners;

public interface ChatWIthIndividualDaoListener {

    void dateBannerAdded(int position);

    void chatItemsAdded(int position);

    void chatItemsAdded(int startPosition, int chatCount);

    void chatSentSuccess(int position);

    void chatSentFailure(int position);

    void chatReceivedSuccess(int position);

    void chatItemUpdated(int position);

    void receiverTypingStatusUpdated(boolean isTyping);

    void receiverOnlineStatusUpdated(boolean isOnline, String lastOnline);

    void hideLoadingAnimation();

    void changeRvStackingOrder(boolean stackFromEnd);
}
