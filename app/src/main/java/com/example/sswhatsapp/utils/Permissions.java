package com.example.sswhatsapp.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Permissions {
    //STATIC FIELDS
    public static final int PERMISSION_DENIED = -1;
    public static final int PERMISSION_GRANTED = 0;
    //PERMISSION REQUEST CODES
    public static final int PERMISSION_READ_CONTACTS_CODE = 101;

    public static boolean isReadContactsGranted(Activity activity, boolean askForIfNotGranted) {
        boolean isGranted = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
        if (!isGranted && askForIfNotGranted) {
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.READ_CONTACTS}, PERMISSION_READ_CONTACTS_CODE);
        }
        return isGranted;
    }
}
