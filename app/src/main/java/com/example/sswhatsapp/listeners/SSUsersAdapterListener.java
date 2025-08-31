package com.example.sswhatsapp.listeners;

import com.example.sswhatsapp.models.responses.UserDetailsResponse;

public interface SSUsersAdapterListener {
    void onSSUserClick(UserDetailsResponse user);

    void onSSUsersListCompletelyShown();
}
