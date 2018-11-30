package com.foxmike.android.models;

import java.util.HashMap;

/**
 * Created by chris on 2017-12-28.
 */

public class Chats {

    public String chatId;
    public long timestamp;
    public String lastMessage;
    public HashMap<String,Boolean> users;

    public Chats() {
    }

    public Chats(String chatId, long timestamp, String lastMessage, HashMap<String, Boolean> users) {
        this.chatId = chatId;
        this.timestamp = timestamp;
        this.lastMessage = lastMessage;
        this.users = users;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
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

    public HashMap<String, Boolean> getUsers() {
        return users;
    }

    public void setUsers(HashMap<String, Boolean> users) {
        this.users = users;
    }
}
