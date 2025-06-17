// Archivo: MiPrimeraAplicacion/app/src/main/java/com/example/miprimeraaplicacion/Message.java
package com.example.miprimeraaplicacion;

public class Message {
    private String id; // Nuevo campo para el ID del mensaje
    private String chatTopicId; // Nuevo campo para el ID del tema de chat
    private String senderId;
    private String senderName; // Nombre del usuario que envía el mensaje
    private String text;
    private long timestamp;

    public Message() {
        // Constructor vacío requerido por Firestore y para facilidad de uso
    }

    public Message(String id, String chatTopicId, String senderId, String senderName, String text, long timestamp) {
        this.id = id;
        this.chatTopicId = chatTopicId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.text = text;
        this.timestamp = timestamp;
    }

    // Constructor existente para casos donde ID y chatTopicId no son necesarios de inmediato
    public Message(String senderId, String senderName, String text, long timestamp) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.text = text;
        this.timestamp = timestamp;
    }

    // Getters y Setters para los nuevos campos
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getChatTopicId() { return chatTopicId; }
    public void setChatTopicId(String chatTopicId) { this.chatTopicId = chatTopicId; }

    // Getters y Setters existentes
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}