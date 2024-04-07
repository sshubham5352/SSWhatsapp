package com.example.sswhatsapp.models;

import java.util.ArrayList;
import java.util.List;

public class UserDeviceContact implements Comparable<UserDeviceContact> {
    //fields
    private String name, mobileNo;

    public UserDeviceContact(String name, String mobileNo) {
        this.name = name;
        this.mobileNo = mobileNo;
    }

    public static void removeNumber(List<UserDeviceContact> list, String number) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).mobileNo.matches(number)) {
                list.remove(i);
                break;
            }
        }
    }

    public String getName() {
        return name;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public static List<String> getMobileNoList(List<UserDeviceContact> userDeviceContacts, boolean removeCountryCode) {
        List<String> contactList = new ArrayList<>();
        if (removeCountryCode) {
            for (UserDeviceContact e : userDeviceContacts) {
                if (e.mobileNo != null && e.mobileNo.length() >= 10)
                    contactList.add(removeCountryCode(e.mobileNo));
            }
        } else {
            for (UserDeviceContact e : userDeviceContacts) {
                if (e.mobileNo != null && e.mobileNo.length() >= 10)
                    contactList.add(e.mobileNo);
            }
        }
        return contactList;
    }

    public static void removeInvalidNumbers(List<UserDeviceContact> list) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (!isMobileNumberValid(list.get(i).mobileNo)) {
                list.remove(i);
                size--;
                i--;
            }
        }
    }

    public static boolean isMobileNumberValid(String mobileNo) {
        return mobileNo != null && mobileNo.length() >= 10;
    }

    public static String removeCountryCode(String mobileNo) {
        return mobileNo.substring(mobileNo.length() - 10);
    }

    @Override
    public int compareTo(UserDeviceContact contact2) {
        if (name != null && name.length() != 0) {
            return name.toLowerCase().compareTo(contact2.name.toLowerCase());
        }
        return 1;
    }
}
