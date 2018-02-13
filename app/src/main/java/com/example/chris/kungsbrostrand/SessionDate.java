package com.example.chris.kungsbrostrand;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

    public String textMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(this.year, this.month, this.day) ;
        SimpleDateFormat monthDate = new SimpleDateFormat("MMMM");
        String monthName = monthDate.format(cal.getTime());
        return monthName;
    }

    public String textDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(this.year, this.month, this.day) ;
        SimpleDateFormat textDay = new SimpleDateFormat("EE");
        String dayName = textDay.format(cal.getTime());
        return dayName;
    }

    public String textFullDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(this.year, this.month, this.day) ;
        SimpleDateFormat textDay = new SimpleDateFormat("EEEE");
        String dayName = textDay.format(cal.getTime());
        return dayName;
    }

    public String textSDF() {
        Calendar cal = Calendar.getInstance();
        cal.set(this.year, this.month, this.day) ;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }

    public Date getDateOfSession () {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date sessionDate = sdf.parse(this.textSDF());
            return sessionDate;
        } catch (ParseException e) {
            //handle exception
            e.printStackTrace();
            return null;
        }
    }
}
