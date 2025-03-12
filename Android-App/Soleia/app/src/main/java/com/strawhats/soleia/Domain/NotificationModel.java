package com.strawhats.soleia.Domain;

public class NotificationModel {
    private String title;
    private String message;
    private String timestamp;

    // Constructor
    public NotificationModel(String title, String message, String timestamp) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }
}