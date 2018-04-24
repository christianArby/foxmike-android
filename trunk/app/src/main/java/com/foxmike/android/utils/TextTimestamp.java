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

    public String textMonth() {
        DateTime dateTime = new DateTime(this.timeStamp);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(this.timeStamp);
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

    public String textSDF() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(this.timeStamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }

    public String textDateAndTime() {
        DateTime dateTime = new DateTime(this.timeStamp);
        String dateAndTimeText = dateTime.getDayOfMonth() + " " + this.textMonth() + " " + "KL." + " " + this.textTime();
        return dateAndTimeText;
    }

    public String textSessionDateAndTime() {
        DateTime dateTime = new DateTime(this.timeStamp);
        return this.textFullDay() + " " + dateTime.getDayOfMonth() + " " + this.textMonth() + " " + this.textTime();
    }


}
