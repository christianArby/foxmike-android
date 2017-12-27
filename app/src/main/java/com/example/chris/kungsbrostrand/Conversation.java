package com.example.chris.kungsbrostrand;

/**
 * Created by chris on 2017-12-25.
 */

public class Conversation {

    public boolean seen;
    public long timestamp;

    public Conversation() {
    }

    public Conversation(boolean seen, long timestamp) {
        this.seen = seen;
        this.timestamp = timestamp;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
