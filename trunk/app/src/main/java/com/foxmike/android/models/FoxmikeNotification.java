package com.foxmike.android.models;

/**
 * Created by chris on 2018-11-26.
 */

public class FoxmikeNotification {

    private	String	type;
    private	String	param1;
    private	String	param2;
    private	String	param3;
    private	String	sourceId;
    private	String	thumbNail;
    private Long timestamp;

    public FoxmikeNotification() {
    }

    public FoxmikeNotification(String type, String param1, String param2, String param3, String sourceId, String thumbNail, Long timestamp) {
        this.type = type;
        this.param1 = param1;
        this.param2 = param2;
        this.param3 = param3;
        this.sourceId = sourceId;
        this.thumbNail = thumbNail;
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getParam1() {
        return param1;
    }

    public void setParam1(String param1) {
        this.param1 = param1;
    }

    public String getParam2() {
        return param2;
    }

    public void setParam2(String param2) {
        this.param2 = param2;
    }

    public String getParam3() {
        return param3;
    }

    public void setParam3(String param3) {
        this.param3 = param3;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getThumbNail() {
        return thumbNail;
    }

    public void setThumbNail(String thumbNail) {
        this.thumbNail = thumbNail;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
