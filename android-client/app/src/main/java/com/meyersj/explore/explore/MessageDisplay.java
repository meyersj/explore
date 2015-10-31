package com.meyersj.explore.explore;

/**
 * Created by jeff on 10/30/15.
 */
public class MessageDisplay {

    private String username;
    private String message;
    private String timestamp;

    public MessageDisplay(String username, String message) {
        this.username = username;
        this.message = message;
    }

    public MessageDisplay(String username, String message, String timestamp) {
        this(username, message);
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return this.username;
    }

    public String getMessage() {
        return this.message;
    }

    public String getTimestamp() {
        return this.timestamp;
    }
}
