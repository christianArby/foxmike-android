package com.example.chris.kungsbrostrand;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by chris on 2017-06-09.
 */

public class Session {
    public String userID;
    public String sessionType;
    public String level;
    public String nrOfParticipants;
    public double latitude;
    public double longitude;
    public String time;
    public SessionDate sessionDate;




    public Session(String userID, String sessionType, String level, String nrOfParticipants, double latitude, double longitude, String time, SessionDate sessionDate) {
        this.userID = sessionType;
        this.sessionType = sessionType;
        this.level = level;
        this.nrOfParticipants = nrOfParticipants;
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
        this.sessionDate = sessionDate;
    }

    public String textMonth(SessionDate sessionDate) {
        Calendar cal = Calendar.getInstance();
        cal.set(sessionDate.year, sessionDate.month, sessionDate.day) ;
        SimpleDateFormat monthDate = new SimpleDateFormat("MMMM");
        String monthName = monthDate.format(cal.getTime());
        return monthName;
    }

    //required empty constructor
    public Session() {
    }

    public SessionDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(SessionDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }


    public String getTime() {
        return time;
    }

    public String getLevel() {
        return level;
    }

    public String getNrOfParticipants() {
        return nrOfParticipants;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setNrOfParticipants(String nrOfParticipants) {
        this.nrOfParticipants = nrOfParticipants;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }


}
