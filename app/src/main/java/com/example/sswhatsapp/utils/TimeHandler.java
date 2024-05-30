package com.example.sswhatsapp.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeHandler {
    public static final String STANDARD_TIME_STAMP_PATTERN = "yyyy_MM_dd_HH_mm_ss.SS";
    public static final String CHAT_TIME_STAMP_PATTERN = "h:mm aa";
    public static final String DATE_BANNER_TIME_STAMP_PATTERN = "d MMMM yyyy";
    public static final String COMPARE_DATE_PATTERN = "yyyy_MM_dd";
    public static final String ONLY_DAY_NAME_PATTERN = "EEEE";
    private static final SimpleDateFormat standardTimeStampFormat = new SimpleDateFormat(STANDARD_TIME_STAMP_PATTERN, Locale.US);
    private static final SimpleDateFormat chatTimeStampFormat = new SimpleDateFormat(CHAT_TIME_STAMP_PATTERN, Locale.US);
    private static final SimpleDateFormat dateBannerTimeStampFormat = new SimpleDateFormat(DATE_BANNER_TIME_STAMP_PATTERN, Locale.US);
    private static final SimpleDateFormat compareDateFormat = new SimpleDateFormat(COMPARE_DATE_PATTERN, Locale.US);
    private static final SimpleDateFormat onlyDayNameFormat = new SimpleDateFormat(ONLY_DAY_NAME_PATTERN, Locale.US);


    public static Date getCurrentDate() {
        return new Date();
    }

    public static Long getCurrentTime() {
        return new Date().getTime();
    }

    public static String getCurrentTimeStamp() {
        //returns current time stamp in standard format
        return standardTimeStampFormat.format(new Date());
    }

    public static String getCurrentTimeStamp(String format) {
        SimpleDateFormat ImageDateFormat = new SimpleDateFormat(format, Locale.US);
        return ImageDateFormat.format(new Date());
    }

    public static String getChatTimeStamp(String standardTimeStamp) {
        //timeStamp given in parameter should be in StandardTimeFormat
        String res;
        try {
            res = chatTimeStampFormat.format(standardTimeStampFormat.parse(standardTimeStamp));
        } catch (ParseException e) {
            res = standardTimeStamp;
        }
        return res;
    }

    public static String getChatBannerStamp(String standardTimeStamp) {
        //timeStamp given in parameter should be in StandardTimeFormat
        /*
         * IF TODAY'S DATE THEN RETURNS today
         * IF YESTERDAY'S DATE THEN RETURNS yesterday
         * IF DATE LIES WITHIN THE 7 DAYS OF THE CURRENT WEEK THEN RETURNS day name
         * ELSE RETURNS DATE DATE_BANNER_FORMAT
         **/
        if (isThisToday(standardTimeStamp)) {
            return "Today";
        }
        if (isThisYesterday(standardTimeStamp)) {
            return "Yesterday";
        }
        if (doesLieInLast7Days(standardTimeStamp)) {
            try {
                return onlyDayNameFormat.format(standardTimeStampFormat.parse(standardTimeStamp));
            } catch (ParseException e) {
                return standardTimeStamp;
            }
        }
        try {
            return dateBannerTimeStampFormat.format(standardTimeStampFormat.parse(standardTimeStamp));
        } catch (ParseException e) {
            //
        }
        return standardTimeStamp;
    }

    public static boolean isThisToday(String standardTimeStamp) {
        //standardTimeStamp given in parameter should be in StandardTimeFormat
        try {
            String givenDate = compareDateFormat.format(standardTimeStampFormat.parse(standardTimeStamp));
            String todaysDate = compareDateFormat.format(getCurrentDate());
            return givenDate.compareTo(todaysDate) == 0;
        } catch (ParseException e) {
            //empty catch block
        }
        return false;
    }

    public static boolean isThisYesterday(String standardTimeStamp) {
        //standardTimeStamp given in parameter should be in StandardTimeFormat
        Date yesterday = new Date(getCurrentDate().getTime() - (86400000L));        //86400000 = 24 * 60 * 60 * 1000
        try {
            String givenDate = compareDateFormat.format(standardTimeStampFormat.parse(standardTimeStamp));
            String yesterdayDate = compareDateFormat.format(yesterday);
            return givenDate.compareTo(yesterdayDate) == 0;
        } catch (ParseException e) {
            //empty catch block
        }
        return false;
    }

    public static boolean areSameDays(String standardTimeStamp1, String standardTimeStamp2) {
        //standardTimeStamps given in parameters should be in StandardTimeFormat
        try {
            String day1 = compareDateFormat.format(standardTimeStampFormat.parse(standardTimeStamp1));
            String day2 = compareDateFormat.format(standardTimeStampFormat.parse(standardTimeStamp2));
            return day1.compareTo(day2) == 0;
        } catch (ParseException e) {
            //empty catch block
        }
        return false;
    }

    public static boolean doesLieInLast7Days(String standardTimeStamp) {
        //standardTimeStamp given in parameter should be in StandardTimeFormat
        Date sevenDaysBack = new Date(getCurrentDate().getTime() - (604800000L));        //604800000 = 7 * 24 * 60 * 60 * 1000
        try {
            String givenDate = compareDateFormat.format(standardTimeStampFormat.parse(standardTimeStamp));
            String sevenDaysBackDate = compareDateFormat.format(sevenDaysBack);
            return givenDate.compareTo(sevenDaysBackDate) > 0;
        } catch (ParseException e) {
            //empty catch block
        }
        return false;
    }
}
