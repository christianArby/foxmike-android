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

    public SessionDate(Calendar mCalendar) {
        this.year = mCalendar.get(Calendar.YEAR);
        this.month = mCalendar.get(Calendar.MONTH);
        this.day = mCalendar.get(Calendar.DAY_OF_MONTH);
    }
    public SessionDate(){}
}
