package com.foxmike.androidapp.kungsbrostrand;

/**
 * Created by chris on 2017-12-25.
 */

public class Conversation {

    public boolean seen;
    public long timestamp;
    public String lastMessage;

    public Conversation() {
    }

    public Conversation(boolean seen, long timestamp, String lastMessage) {
        this.seen = seen;
        this.timestamp = timestamp;
        this.lastMessage = lastMessage;
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

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
