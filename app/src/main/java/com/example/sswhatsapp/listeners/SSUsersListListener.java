package com.example.sswhatsapp.listeners;

import com.example.sswhatsapp.models.UserDetailsResponse;

public interface SSUsersListListener {
    void onSSUserClick(UserDetailsResponse user);

    void onSSUsersListCompletelyShown();
}
