package com.foxmike.android.models;

import android.support.annotation.NonNull;

import com.google.firebase.database.ServerValue;

/**
 * Created by chris on 2018-01-31.
 */

public class Post implements Comparable<Post> {

    private String author;
    private Object timestamp;
    private String message;
    private String senderName;
    private String senderThumbImage;
    private String sourceId;
    private String sourceName;

    public Post(String author, String message, String senderName, String senderThumbImage, String sourceId, String sourceName) {
        this.author = author;
        this.timestamp = ServerValue.TIMESTAMP;
        this.message = message;
        this.senderName = senderName;
        this.senderThumbImage = senderThumbImage;
        this.sourceId = sourceId;
        this.sourceName = sourceName;
    }

    public Post() {
    }

    public String getsourceName() {
        return sourceName;
    }

    public void setsourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    @Override
    public int compareTo(@NonNull Post post) {
        return ((int) (long) this.timestamp - (int) (long) post.timestamp);
    }
}
