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
    public static final String COMPARE_HOUR_PATTERN = "HH";
    public static final String COMPARE_MINUTE_PATTERN = "mm";
    public static final String FULL_DAY_NAME_PATTERN = "EEEE";
    private static SimpleDateFormat standardTimeStampFormat;
    private static SimpleDateFormat chatTimeStampFormat;
    private static SimpleDateFormat dateBannerTimeStampFormat;
    private static SimpleDateFormat compareDateFormat;
    private static SimpleDateFormat compareHourFormat;
    private static SimpleDateFormat compareMinuteFormat;
    private static SimpleDateFormat fullDayNameFormat;


    //GETTERS


    public static SimpleDateFormat getStandardTimeStampFormat() {
        if (standardTimeStampFormat == null)
            standardTimeStampFormat = new SimpleDateFormat(STANDARD_TIME_STAMP_PATTERN, Locale.US);
        return standardTimeStampFormat;
    }

    public static SimpleDateFormat getChatTimeStampFormat() {
        if (chatTimeStampFormat == null)
            chatTimeStampFormat = new SimpleDateFormat(CHAT_TIME_STAMP_PATTERN, Locale.US);
        return chatTimeStampFormat;
    }

    public static SimpleDateFormat getDateBannerTimeStampFormat() {
        if (dateBannerTimeStampFormat == null)
            dateBannerTimeStampFormat = new SimpleDateFormat(DATE_BANNER_TIME_STAMP_PATTERN, Locale.US);
        return dateBannerTimeStampFormat;
    }

    public static SimpleDateFormat getCompareDateFormat() {
        if (compareDateFormat == null)
            compareDateFormat = new SimpleDateFormat(COMPARE_DATE_PATTERN, Locale.US);
        return compareDateFormat;
    }

    public static SimpleDateFormat getCompareHourFormat() {
        if (compareHourFormat == null)
            compareHourFormat = new SimpleDateFormat(COMPARE_HOUR_PATTERN, Locale.US);
        return compareHourFormat;
    }

    public static SimpleDateFormat getCompareMinuteFormat() {
        if (compareMinuteFormat == null)
            compareMinuteFormat = new SimpleDateFormat(COMPARE_MINUTE_PATTERN, Locale.US);
        return compareMinuteFormat;
    }

    public static SimpleDateFormat getFullDayNameFormat() {
        if (fullDayNameFormat == null)
            fullDayNameFormat = new SimpleDateFormat(FULL_DAY_NAME_PATTERN, Locale.US);
        return fullDayNameFormat;
    }

    public static Date getCurrentDate() {
        return new Date();
    }

    public static Long getCurrentTime() {
        return new Date().getTime();
    }

    public static String getCurrentTimeStamp() {
        //returns current time stamp in standard format
        return getStandardTimeStampFormat().format(new Date());
    }

    public static String getCurrentTimeStamp(String format) {
        SimpleDateFormat ImageDateFormat = new SimpleDateFormat(format, Locale.US);
        return ImageDateFormat.format(new Date());
    }

    public static String getChatTimeStamp(String standardTimeStamp) {
        //timeStamp given in parameter should be in StandardTimeFormat
        String res;
        try {
            res = getChatTimeStampFormat().format(getStandardTimeStampFormat().parse(standardTimeStamp));
        } catch (ParseException e) {
            res = standardTimeStamp;
        }
        return res;
    }

    public static String getChatBannerTimeStamp(String standardTimeStamp) {
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
                return getFullDayNameFormat().format(getStandardTimeStampFormat().parse(standardTimeStamp));
            } catch (ParseException e) {
                return standardTimeStamp;
            }
        }
        try {
            return getDateBannerTimeStampFormat().format(getStandardTimeStampFormat().parse(standardTimeStamp));
        } catch (ParseException e) {
            //
        }
        return standardTimeStamp;
    }

    public static String getLastOnlineMsg(long timeMillis) {
        /*
         * IF SENT TODAY THEN RETURN HOUR OR MINUTE DIFFERENCE FROM CURRENT TIME
         * IF YESTERDAY'S DATE THEN RETURNS "yesterday"
         * ELSE RETURNS DATE DATE_BANNER_FORMAT
         **/

        long minutesPast = (getCurrentTime() - timeMillis) / (1000 * 60);

        if (minutesPast < 1) {
            return null;
        }
        if (minutesPast < 60) {
            return "last seen " + minutesPast + "min ago";
        } else if (minutesPast < (60 * 24)) {
            return "last seen " + (minutesPast / 60) + "hr ago";
        } else if (minutesPast < (60 * 24 * 2)) {
            return "last seen " + "yesterday";
        } else {
            return "last seen on " + getDateBannerTimeStampFormat().format(new Date(timeMillis));
        }
    }


    public static boolean isThisToday(String standardTimeStamp) {
        //standardTimeStamp given in parameter should be in StandardTimeFormat
        try {
            String givenDate = getCompareDateFormat().format(getStandardTimeStampFormat().parse(standardTimeStamp));
            String todaysDate = getCompareDateFormat().format(getCurrentDate());
            return givenDate.compareTo(todaysDate) == 0;
        } catch (ParseException e) {
            //empty catch block
        }
        return false;
    }

    public static int hoursPast(String standardTimeStamp) {
        //standardTimeStamp given in parameter should be in StandardTimeFormat
        /*
         * RETURNS THE HOUR DIFFERENCE BETWEEN THE GIVEN DATE AND CURRENT DATE*/
        try {
            String givenDate = getCompareHourFormat().format(getStandardTimeStampFormat().parse(standardTimeStamp));
            String todaysDate = getCompareHourFormat().format(getCurrentDate());
            return todaysDate.compareTo(givenDate);
        } catch (ParseException e) {
            //empty catch block
        }
        return 0;
    }

    public static int minutesPast(String standardTimeStamp) {
        //standardTimeStamp given in parameter should be in StandardTimeFormat
        /*
         * RETURNS THE HOUR DIFFERENCE BETWEEN THE GIVEN DATE AND CURRENT DATE*/
        try {
            String givenDate = getCompareMinuteFormat().format(getStandardTimeStampFormat().parse(standardTimeStamp));
            String todaysDate = getCompareMinuteFormat().format(getCurrentDate());
            return todaysDate.compareTo(givenDate);
        } catch (ParseException e) {
            //empty catch block
        }
        return 0;
    }

    public static boolean isThisYesterday(String standardTimeStamp) {
        //standardTimeStamp given in parameter should be in StandardTimeFormat
        Date yesterday = new Date(getCurrentDate().getTime() - (86400000L));        //86400000 = 24 * 60 * 60 * 1000
        try {
            String givenDate = getCompareDateFormat().format(getStandardTimeStampFormat().parse(standardTimeStamp));
            String yesterdayDate = getCompareDateFormat().format(yesterday);
            return givenDate.compareTo(yesterdayDate) == 0;
        } catch (ParseException e) {
            //empty catch block
        }
        return false;
    }

    public static boolean areSameDays(String standardTimeStamp1, String standardTimeStamp2) {
        //standardTimeStamps given in parameters should be in StandardTimeFormat
        try {
            String day1 = getCompareDateFormat().format(getStandardTimeStampFormat().parse(standardTimeStamp1));
            String day2 = getCompareDateFormat().format(getStandardTimeStampFormat().parse(standardTimeStamp2));
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
            String givenDate = getCompareDateFormat().format(getStandardTimeStampFormat().parse(standardTimeStamp));
            String sevenDaysBackDate = getCompareDateFormat().format(sevenDaysBack);
            return givenDate.compareTo(sevenDaysBackDate) > 0;
        } catch (ParseException e) {
            //empty catch block
        }
        return false;
    }
}
