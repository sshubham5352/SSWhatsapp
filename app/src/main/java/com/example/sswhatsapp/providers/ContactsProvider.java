package com.example.sswhatsapp.providers;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.example.sswhatsapp.models.UserDeviceContact;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class ContactsProvider {

    public static List<UserDeviceContact> getUserDeviceContacts(Context context) {
        List<UserDeviceContact> contacts = new LinkedList<>();
        HashSet<String> uniqueContacts = new HashSet<>();

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                //plus any other properties you wish to query
        };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);


        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String mobileNo = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                mobileNo = convertNumberToStandardFormat(mobileNo);
                if (!uniqueContacts.contains(mobileNo)) {
                    uniqueContacts.add(mobileNo);
                    contacts.add(new UserDeviceContact(name, mobileNo));
                }
            }
        }
        cursor.close();
        return contacts;
    }

    public static String getContactName(final String phoneNumber, Context context) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName = null;
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(0);
            }
            cursor.close();
        }

        if (contactName != null)
            return contactName;
        return phoneNumber;
    }

    private static String convertNumberToStandardFormat(String phoneNo) {
        /*
         * Standard format is: 9999999999 (plain 10 digit number)
         * without any spaces, country code or anything
         * */

        StringBuilder result = new StringBuilder(10);
        char curChar;
        for (int i = phoneNo.length() - 1; i >= 0; i--) {
            curChar = phoneNo.charAt(i);
            if (curChar > 47 && curChar < 58) {
                /*
                 * ASCII values of integers: '0': 48 & '9': 57
                 * */
                result.append(curChar);
                if (result.length() == 10)
                    break;
            }
        }

        return result.reverse().toString();
    }
}
