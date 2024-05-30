package com.example.sswhatsapp.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.RemoteInput;

import com.example.sswhatsapp.firebase.FirebaseConstants;
import com.example.sswhatsapp.notificatons.ChatNotificationsManager;
import com.example.sswhatsapp.services.FCMService;
import com.example.sswhatsapp.utils.Constants;
import com.example.sswhatsapp.utils.Helper;

public class NotificationActionsBroadcastReceiver extends BroadcastReceiver {

    private ChatNotificationsManager notificationsManager;

    //CONSTRUCTOR
    public NotificationActionsBroadcastReceiver() {
        notificationsManager = FCMService.chatNotificationsManager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int actionType = intent.getIntExtra(Constants.NOTIFICATION_ACTION_ID, 0);

        switch (actionType) {
            case Constants.NOTIFICATION_ACTION_REPLY: {
                Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
                if (remoteInput == null) {
                    return;
                }
                String replyMsg = remoteInput.getCharSequence(Constants.KEY_CHAT_REPLY).toString().trim();
                if (Helper.isNill(replyMsg)) {
                    return;
                }
                String userId = intent.getStringExtra(FirebaseConstants.KEY_USER_ID);
                notificationsManager.actionReplyMsg(replyMsg, userId);
                break;
            }

            case Constants.NOTIFICATION_ACTION_MARK_AS_READ: {
                String userId = intent.getStringExtra(FirebaseConstants.KEY_USER_ID);
                notificationsManager.actionMarkAsRead(userId);
                break;
            }
        }
    }
}
