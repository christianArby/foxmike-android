package com.example.chris.kungsbrostrand;

/**
 * Created by chris on 2017-12-18.
 */

public class Message {

    private String message;
    private long time;
    private boolean seen;
    private String senderUserID;
    private String senderName;
    private String senderThumbImage;

    public Message(String message, long time, boolean seen, String senderUserID, String senderName, String senderThumbImage) {
        this.message = message;
        this.time = time;
        this.seen = seen;
        this.senderUserID = senderUserID;
        this.senderName = senderName;
        this.senderThumbImage = senderThumbImage;
    }

    public Message() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getSenderUserID() {
        return senderUserID;
    }

    public void setSenderUserID(String senderUserID) {
        this.senderUserID = senderUserID;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderThumbImage() {
        return senderThumbImage;
    }

    public void setSenderThumbImage(String senderThumbImage) {
        this.senderThumbImage = senderThumbImage;
    }
}
