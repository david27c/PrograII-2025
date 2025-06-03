package com.example.miprimeraaplicacion;

public class Message {
    private String senderId;
    private String senderName; // Nombre del usuario que envía el mensaje
    private String text;
    private long timestamp;

    public Message() {
        // Constructor vacío requerido por Firestore
    }

    public Message(String senderId, String senderName, String text, long timestamp) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.text = text;
        this.timestamp = timestamp;
    }

    // Getters y Setters
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}