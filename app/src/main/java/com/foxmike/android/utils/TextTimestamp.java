package com.foxmike.android.utils;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by chris on 2018-04-24.
 */

public class TextTimestamp {

    long timeStamp;

    public TextTimestamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public TextTimestamp() {
    }

    public String textTime() {
        DateTime dateTime = new DateTime(this.timeStamp);
        String time = String.format("%02d:%02d", dateTime.getHourOfDay(), dateTime.getMinuteOfHour());
        return time;
    }

    public static String textTime(long timeStamp) {
        DateTime dateTime = new DateTime(timeStamp);
        String time = String.format("%02d:%02d", dateTime.getHourOfDay(), dateTime.getMinuteOfHour());
        return time;
    }

    public String textMonth() {
        DateTime dateTime = new DateTime(this.timeStamp);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(this.timeStamp);
        SimpleDateFormat monthDate = new SimpleDateFormat("MMMM");
        String monthName = monthDate.format(cal.getTime());
        return monthName;
    }

    public static String textMonth(long timeStamp) {
        DateTime dateTime = new DateTime(timeStamp);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeStamp);
        SimpleDateFormat monthDate = new SimpleDateFormat("MMMM");
        String monthName = monthDate.format(cal.getTime());
        return monthName;
    }

    public String textDay() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(this.timeStamp);
        SimpleDateFormat textDay = new SimpleDateFormat("EE");
        String dayName = textDay.format(cal.getTime());
        return dayName;
    }

    public String textNumberDay() {
        DateTime dateTime = new DateTime(this.timeStamp);
        return Integer.toString(dateTime.getDayOfMonth());
    }

    public String textFullDay() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(this.timeStamp);
        SimpleDateFormat textDay = new SimpleDateFormat("EEEE");
        String dayName = textDay.format(cal.getTime());
        return dayName;
    }

    public static String textFullDay(long timeStamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeStamp);
        SimpleDateFormat textDay = new SimpleDateFormat("EEEE");
        String dayName = textDay.format(cal.getTime());
        return dayName;
    }

    public String textSDF() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(this.timeStamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }

    public static String textSDF(long timeStamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeStamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }

    public String textDateAndTime() {
        DateTime dateTime = new DateTime(this.timeStamp);
        String dateAndTimeText = dateTime.getDayOfMonth() + " " + this.textMonth() + " " + "KL." + " " + this.textTime();
        return dateAndTimeText;
    }

    public static String textDateAndTime(long timeStamp) {
        DateTime dateTime = new DateTime(timeStamp);
        String dateAndTimeText = dateTime.getDayOfMonth() + " " + TextTimestamp.textMonth(timeStamp) + " " + "KL." + " " + TextTimestamp.textTime(timeStamp);
        return dateAndTimeText;
    }

    public String textSessionDateAndTime() {
        DateTime dateTime = new DateTime(this.timeStamp);
        return this.textFullDay() + " " + dateTime.getDayOfMonth() + " " + this.textMonth() + " " + this.textTime();
    }

    public static String textSessionDateAndTime(long timeStamp) {
        DateTime dateTime = new DateTime(timeStamp);
        return TextTimestamp.textFullDay(timeStamp) + " " + dateTime.getDayOfMonth() + " " + TextTimestamp.textMonth(timeStamp) + " " + TextTimestamp.textTime(timeStamp);
    }

    public String textSessionDate() {
        DateTime dateTime = new DateTime(this.timeStamp);
        return this.textFullDay() + " " + dateTime.getDayOfMonth() + " " + this.textMonth();
    }

    public static String textSessionDate(long timeStamp) {
        DateTime dateTime = new DateTime(timeStamp);
        return TextTimestamp.textFullDay(timeStamp) + " " + dateTime.getDayOfMonth() + " " + TextTimestamp.textMonth(timeStamp);
    }


}
