package com.example.chris.kungsbrostrand;

import android.support.annotation.NonNull;

import com.google.firebase.database.ServerValue;

import java.util.Map;

/**
 * Created by chris on 2018-01-31.
 */

public class Post implements Comparable<Post> {

    private String author;
    private Object timestamp;
    private String message;
    private String senderName;
    private String senderThumbImage;

    public Post(String author, String message, String senderName, String senderThumbImage) {
        this.author = author;
        this.timestamp = ServerValue.TIMESTAMP;
        this.message = message;
        this.senderName = senderName;
        this.senderThumbImage = senderThumbImage;
    }

    public Post() {
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
