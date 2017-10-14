package com.example.chris.kungsbrostrand;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


public class Session {
    public String host;
    public String sessionName;
    public String sessionType;
    public String level;
    public String maxParticipants;
    public String description;
    public double latitude;
    public double longitude;
    public String time;
    public SessionDate sessionDate;
    public int countParticipants;



    public HashMap<String,Boolean> participants;
    public String imageUri;

    public Session(String host, String sessionName, String sessionType, String level, String maxParticipants, double latitude, double longitude, String time, SessionDate sessionDate, int countParticipants, HashMap<String, Boolean> participants, String imageUri, String description) {
        this.host = host;
        this.sessionName = sessionName;
        this.sessionType = sessionType;
        this.level = level;
        this.maxParticipants = maxParticipants;
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
        this.sessionDate = sessionDate;
        this.countParticipants = countParticipants;
        this.participants = participants;
        this.imageUri = imageUri;
        this.description = description;
    }

    public String textMonth(SessionDate sessionDate) {
        Calendar cal = Calendar.getInstance();
        cal.set(sessionDate.year, sessionDate.month, sessionDate.day) ;
        SimpleDateFormat monthDate = new SimpleDateFormat("MMMM");
        String monthName = monthDate.format(cal.getTime());
        return monthName;
    }

    public String textDay(SessionDate sessionDate) {
        Calendar cal = Calendar.getInstance();
        cal.set(sessionDate.year, sessionDate.month, sessionDate.day) ;
        SimpleDateFormat textDay = new SimpleDateFormat("EE");
        String dayName = textDay.format(cal.getTime());
        return dayName;
    }

    public String textFullDay(SessionDate sessionDate) {
        Calendar cal = Calendar.getInstance();
        cal.set(sessionDate.year, sessionDate.month, sessionDate.day) ;
        SimpleDateFormat textDay = new SimpleDateFormat("EEEE");
        String dayName = textDay.format(cal.getTime());
        return dayName;
    }

    //required empty constructor
    public Session() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(String maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public SessionDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(SessionDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public int getCountParticipants() {
        return countParticipants;
    }

    public void setCountParticipants(int countParticipants) {
        this.countParticipants = countParticipants;
    }

    public HashMap<String, Boolean> getParticipants() {
        return participants;
    }

    public void setParticipants(HashMap<String, Boolean> participants) {
        this.participants = participants;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
