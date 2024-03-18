package com.example.sswhatsapp.utils;

import android.content.Context;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;

import com.example.sswhatsapp.R;

import java.util.regex.Pattern;

public class Helper {
    public static final Pattern EMAIL_PATTERN = Patterns.EMAIL_ADDRESS;
    public static final Pattern MOBILE_NO_PATTERN = Patterns.PHONE;
    public static final Pattern PASSWORD_PATTERN = Pattern.compile("^" +
            "(?=.*[@#$%^&+=])" +     // at least 1 special character
            "(?=.*[A-Z])" +         // at least 1 upper case character
            "(?=.*\\d)" +           // at least 1 digit
            "(?=\\S+$)" +            // no white spaces
            ".{8,}" +                // at least 8 characters and max 12
            "$");

    private static String[] genderList;


    public static boolean isNill(String s) {
        return (s == null || s.length() != 0);
    }

    public static boolean isFieldEmpty(EditText field) {
        if (field.getText().toString().trim().length() == 0) {
            field.getText().clear();
            return true;
        }
        return false;
    }

    public static void setText(String txt, TextView textView, boolean setNA) {
        if (txt != null && txt.length() != 0)
            textView.setText(txt);
        else if (setNA)
            textView.setText(R.string.na);
    }

    public static void setText(String txt1, String txt2, TextView textView, boolean setNA) {
        if (txt1 != null && txt1.length() != 0)
            textView.setText(txt1);
        else if (txt2 != null && txt2.length() != 0)
            textView.setText(txt2);
        else if (setNA)
            textView.setText(R.string.na);
    }

    public static boolean isEmailValid(String email) {
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isMobileNoValid(String mobileNo) {
        mobileNo = mobileNo.trim();
        return (MOBILE_NO_PATTERN.matcher(mobileNo).matches()) && mobileNo.length() == 10;
    }

    public static boolean isPasswordValid(String password) {
        return (PASSWORD_PATTERN.matcher(password.trim()).matches());
    }

    public static String getGender(int id) {
        if (id == R.id.radio_btn_male)
            return Constants.GENDER_MALE;
        if (id == R.id.radio_btn_female)
            return Constants.GENDER_FEMALE;
        if (id == R.id.radio_btn_others)
            return Constants.GENDER_OTHERS;
        return null;
    }

    public static String getFirstName(String name) {
        String[] userNameSubParts = name.split(" ");
        return userNameSubParts[0];
    }

    public static void makeViewInactive(EditText view) {
        view.setAlpha(0.5f);
        view.setEnabled(false);
        view.setFocusable(false);
        view.setFocusableInTouchMode(false);
        view.setClickable(false);
    }

    public static void makeViewActive(EditText view) {
        view.setAlpha(1f);
        view.setEnabled(true);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setClickable(true);
    }

    public static int getProfilePlaceholderImg(Context context, String gender) {
        if (genderList == null) {
            genderList = context.getResources().getStringArray(R.array.genders);
        }
        if (gender.matches(genderList[0]))
            return R.drawable.img_male_icon;
        if (gender.matches(genderList[1]))
            return R.drawable.img_female_icon;
        if (gender.matches(genderList[2]))
            return R.drawable.img_others_gender_icon;
        else
            return R.drawable.img_user_placeholder;
    }
}
