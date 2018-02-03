package com.example.chris.kungsbrostrand;

import java.util.HashMap;

/**
 * Created by chris on 2018-01-08.
 */

public class Presence {
    private HashMap<String,Boolean> connections;
    private long lastOnline;

    public Presence(HashMap<String, Boolean> connections, long lastOnline) {
        this.connections = connections;
        this.lastOnline = lastOnline;
    }

    public Presence() {
    }

    public HashMap<String, Boolean> getConnections() {
        return connections;
    }

    public void setConnections(HashMap<String, Boolean> connections) {
        this.connections = connections;
    }

    public long getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(long lastOnline) {
        this.lastOnline = lastOnline;
    }

    public Boolean isOnline() {
        if (this.connections!=null) {
            if (this.connections.containsValue("true")) {
                return true;
            } else {
                return false;
            }
        } else {
            return  false;
        }
    }
}
