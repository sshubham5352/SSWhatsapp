package com.example.sswhatsapp.providers;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.example.sswhatsapp.models.UserDeviceContact;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ContactsProvider {

    public static List<UserDeviceContact> getUserDeviceContacts(Context context) {
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
                //plus any other properties you wish to query
        };
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, null);

        List<UserDeviceContact> contacts = new LinkedList<>();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String mobileNo = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
                contacts.add(new UserDeviceContact(name, mobileNo));
            }
        }
        return contacts;
    }
}
