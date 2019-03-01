package com.foxmike.android.models;

/**
 * Created by chris on 2018-11-26.
 */

public class FoxmikeNotification {

    private String notificationId;
    private	String type;
    private	String sourceId;
    private	String	p1;
    private	String	p2;
    private Long timestamp;

    public FoxmikeNotification() {
    }

    public FoxmikeNotification(String type, String sourceId, String p1, String p2, Long timestamp) {
        this.type = type;
        this.sourceId = sourceId;
        this.p1 = p1;
        this.p2 = p2;
        this.timestamp = timestamp;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getP1() {
        return p1;
    }

    public void setP1(String p1) {
        this.p1 = p1;
    }

    public String getP2() {
        return p2;
    }

    public void setP2(String p2) {
        this.p2 = p2;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
