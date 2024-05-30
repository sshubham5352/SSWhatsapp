package com.example.sswhatsapp.utils;

import android.content.Context;
import android.media.MediaPlayer;

import com.example.sswhatsapp.R;

public class SoundManager {
    //FINAL STATIC FIELDS
    private static MediaPlayer chatSentSound, chatReceivedSound;
    private static boolean isInitiated = false;

    public static void initSoundManager(Context context) {
        chatSentSound = MediaPlayer.create(context, R.raw.sound_chat_sent);
        chatReceivedSound = MediaPlayer.create(context, R.raw.sound_chat_received);
        isInitiated = true;
    }

    public static boolean isInitiated() {
        return isInitiated;
    }

    public static boolean playChatSentSound() {
        if (chatSentSound == null) {
            return false;
        }
        chatSentSound.start();
        return true;
    }

    public static boolean playChatReceivedSound() {
        if (chatReceivedSound == null) {
            return false;
        }
        chatReceivedSound.start();
        return true;
    }
}
