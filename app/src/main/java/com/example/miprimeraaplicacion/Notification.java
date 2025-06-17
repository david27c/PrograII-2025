package com.example.miprimeraaplicacion;

public class Notification {
    private String id;
    private String userId; // Nuevo campo para el ID del usuario al que pertenece la notificación
    private String title;
    private String message;
    private long timestamp;
    private boolean read;
    private String relatedId; // Opcional: ID de reporte, chat, etc.

    public Notification(String id, String notificationUserId, String title, String message, long timestamp, boolean readStatus, String type, String relatedId) {
        // Constructor vacío (para facilidad de uso, por ejemplo, al crear objetos)
    }

    public Notification(String id, String userId, String title, String message, long timestamp, boolean read, String relatedId) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.read = read;
        this.relatedId = relatedId;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; } // Nuevo getter
    public void setUserId(String userId) { this.userId = userId; } // Nuevo setter
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public String getRelatedId() { return relatedId; }
    public void setRelatedId(String relatedId) { this.relatedId = relatedId; }

    public byte[] getType() {
        return new byte[0];
    }
}