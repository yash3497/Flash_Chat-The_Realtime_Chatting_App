package com.example.flashchat2.Models;

public class Status {
    private String ImageUrl;
    private long timestamp;

    public Status() {
    }

    public Status(String imageUrl, long timestamp) {
        ImageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
