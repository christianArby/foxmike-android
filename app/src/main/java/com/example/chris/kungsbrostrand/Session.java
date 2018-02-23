package com.example.chris.kungsbrostrand;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


public class Session implements Comparable<Session> {
    private String host;
    private String sessionName;
    private String sessionType;
    private String maxParticipants;
    private double latitude;
    private double longitude;
    private SessionDate sessionDate;
    private boolean advertised;
    private HashMap<String,Boolean> participants;
    private HashMap<String,Boolean> posts;
    private String imageUrl;
    private String what;
    private String who;
    private String where;
    private String duration;

    public Session(String host, String sessionName, String sessionType, String maxParticipants, double latitude, double longitude, SessionDate sessionDate, boolean advertised, HashMap<String, Boolean> participants, HashMap<String, Boolean> posts,String imageUrl, String what, String who, String where, String duration) {
        this.host = host;
        this.sessionName = sessionName;
        this.sessionType = sessionType;
        this.maxParticipants = maxParticipants;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sessionDate = sessionDate;
        this.advertised = advertised;
        this.participants = participants;
        this.posts = posts;
        this.imageUrl = imageUrl;
        this.what = what;
        this.who = who;
        this.where = where;
        this.duration = duration;
    }

    public Session() {
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String textTime() {
        String time = String.format("%02d:%02d", this.sessionDate.hour, this.sessionDate.minute);
        return time;
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

    public SessionDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(SessionDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public boolean isAdvertised() {
        return advertised;
    }

    public void setAdvertised(boolean advertised) {
        this.advertised = advertised;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getWhat() {
        return what;
    }

    public void setWhat(String what) {
        this.what = what;
    }

    public String getWho() {
        return who;
    }

    public void setWho(String who) {
        this.who = who;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public HashMap<String, Boolean> getParticipants() {
        if (this.participants==null) {
            participants = new HashMap<String, Boolean>();
        }
        return participants;
    }

    public void setParticipants(HashMap<String, Boolean> participants) {
        this.participants = participants;
    }

    public HashMap<String, Boolean> getPosts() {
        if (this.posts==null) {
            posts = new HashMap<String, Boolean>();
        }
        return posts;
    }

    public void setPosts(HashMap<String, Boolean> posts) {
        this.posts = posts;
    }

    @Override
    public int compareTo(@NonNull Session session) {

        Calendar otherSessioncal = Calendar.getInstance();
        otherSessioncal.set(session.sessionDate.year, session.sessionDate.month, session.sessionDate.day, session.sessionDate.hour, session.sessionDate.minute);
        Date dateOfOtherSession = otherSessioncal.getTime();

        Calendar sessionCal = Calendar.getInstance();
        sessionCal.set(this.sessionDate.year, this.sessionDate.month, this.sessionDate.day, this.sessionDate.hour, this.sessionDate.minute);
        Date dateOfThisSession = sessionCal.getTime();

        return (dateOfThisSession.compareTo(dateOfOtherSession));

    }
}