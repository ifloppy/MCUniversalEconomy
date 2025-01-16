package com.iruanp.mcuniversaleconomy.notification;

import java.util.UUID;

public class Notification {
    private int id;
    private UUID recipient;
    private String message;
    private long timestamp;

    public Notification(UUID recipient, String message, long timestamp) {
        this.recipient = recipient;
        this.message = message;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UUID getRecipient() {
        return recipient;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }
}