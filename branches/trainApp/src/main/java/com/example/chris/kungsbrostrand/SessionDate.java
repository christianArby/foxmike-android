package com.example.chris.kungsbrostrand;

import java.util.Calendar;

/**
 * Created by chris on 2017-06-14.
 */

public class SessionDate {

    Calendar mCalendar;
    int year;
    int month;
    int day;
    int hour;
    int minute;

    public SessionDate(Calendar mCalendar) {
        this.year = mCalendar.get(Calendar.YEAR);
        this.month = mCalendar.get(Calendar.MONTH);
        this.day = mCalendar.get(Calendar.DAY_OF_MONTH);
        this.hour = mCalendar.get(Calendar.HOUR_OF_DAY);
        this.minute = mCalendar.get(Calendar.MINUTE);
    }
    public SessionDate(){}
}
