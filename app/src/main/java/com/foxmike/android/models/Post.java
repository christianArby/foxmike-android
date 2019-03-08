package com.foxmike.android.models;

import android.support.annotation.NonNull;

import com.google.firebase.database.ServerValue;

/**
 * Created by chris on 2018-01-31.
 */

public class Post implements Comparable<Post> {

    private String authorId;
    private Object timestamp;
    private String message;
    private String sourceId;

    public Post(String authorId, String message, String sourceId) {
        this.authorId = authorId;
        this.timestamp = ServerValue.TIMESTAMP;
        this.message = message;
        this.sourceId = sourceId;
    }

    public Post() {
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
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


    @Override
    public int compareTo(@NonNull Post post) {
        return ((int) (long) this.timestamp - (int) (long) post.timestamp);
    }
}
