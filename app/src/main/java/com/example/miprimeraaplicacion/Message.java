package com.example.miprimeraaplicacion;

public class Message {
    private String messageId;
    private String chatTopicId;
    private String senderId;
    private String senderName;
    private String text;
    private long timestamp;
    private boolean read;

    public Message(String messageId, String chatTopicId, String senderId, String senderName, String text, long timestamp) {
        this.messageId = messageId;
        this.chatTopicId = chatTopicId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.text = text;
        this.timestamp = timestamp;
        this.read = false; // Por defecto, un mensaje nuevo se considera no leído
    }

    // Opcional: Constructor si necesitas inicializar el estado de lectura al crear el objeto
    public Message(String messageId, String chatTopicId, String senderId, String senderName, String text, long timestamp, boolean read) {
        this.messageId = messageId;
        this.chatTopicId = chatTopicId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.text = text;
        this.timestamp = timestamp;
        this.read = read;
    }

    public String getMessageId() { return messageId; }
    public String getChatTopicId() { return chatTopicId; }
    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getText() { return text; }
    public long getTimestamp() { return timestamp; }
    public boolean isRead() { return read; } // La convención para getters booleanos es 'is'

    public void setMessageId(String messageId) { this.messageId = messageId; }
    public void setChatTopicId(String chatTopicId) { this.chatTopicId = chatTopicId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setText(String text) { this.text = text; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setRead(boolean read) { this.read = read; }
}