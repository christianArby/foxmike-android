package com.example.chris.kungsbrostrand;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


public class Session {
    private String host;
    private String sessionName;
    private String sessionType;
    private String level;
    private String maxParticipants;
    private double latitude;
    private double longitude;
    private String time;
    private SessionDate sessionDate;
    private int countParticipants;
    private HashMap<String,Boolean> participants;



    public String imageUri;

    public Session(String sessionName, String host, String sessionType, String level, String maxParticipants, double latitude, double longitude, String time, SessionDate sessionDate, int countParticipants, HashMap<String,Boolean> participants, String imageUri) {
        this.host = host;
        this.sessionName = sessionName;
        this.sessionType = sessionType;
        this.level = level;
        this.maxParticipants = maxParticipants;
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
        this.sessionDate = sessionDate;
        this.countParticipants= countParticipants;
        this.participants = participants;
        this.imageUri = imageUri;
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

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getMaxParticipants() {
        return maxParticipants;
    }

    public int getCountParticipants() {
        return countParticipants;
    }

    public void setCountParticipants(int nrOfParticipants) {
        this.countParticipants = nrOfParticipants;
    }

    public SessionDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(SessionDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public String gethost() {
        return host;
    }

    public void sethost(String host) {
        this.host = host;
    }


    public String getTime() {
        return time;
    }

    public String getLevel() {
        return level;
    }


    public void setTime(String time) {
        this.time = time;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setMaxParticipants(String maxParticipants) {
        this.maxParticipants = maxParticipants;
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

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }


}
