package com.example.sswhatsapp.listeners;

public interface ChatWIthIndividualDaoListener {

    void dateBannerAdded(int position);

    void chatAdded(int position);

    void allChatsAdded(int startPosition, int chatCount);

    void chatSentSuccess(int position);

    void chatSentFailure(int position);

    void chatReceivedSuccess(int position);
}
