package com.example.sswhatsapp.listeners;

public interface HomeDaoListener {
    void myInterconnectionsListFetched();

    void allInterconnectedUsersDetailsAdded();

    void interconnectedUserDetailsAdded(int position);

    void newInterconnectionAdded(int position);

    void interconnectionRemoved(int position);

    void myInterconnectionItemUpdated(int position);

    void moveInterconnectionItem(int fromPosition, int toPosition);

    void sortInterconnectionsList();
}
