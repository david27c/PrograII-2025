package com.example.miprimeraaplicacion;

public class Comment {
    private String userId;
    private String authorName;
    private String text;
    private long timestamp;

    public Comment() {
        // Constructor vacío requerido para Firestore
    }

    public Comment(String userId, String authorName, String text, long timestamp) {
        this.userId = userId;
        this.authorName = authorName;
        this.text = text;
        this.timestamp = timestamp;
    }

    // Getters y Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}