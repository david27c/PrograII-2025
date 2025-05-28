package com.example.miprimeraaplicacion;

public class ChatTopic {
    private String id; // ID del documento en Firestore
    private String title;
    private String description; // Opcional
    private String lastMessage; // Contenido del último mensaje
    private long lastMessageTimestamp; // Timestamp del último mensaje
    private String iconUrl; // URL del icono del tema
    private int unreadMessagesCount; // Contador de mensajes no leídos (esto debería ser manejado por el cliente)

    public ChatTopic() {
        // Constructor vacío requerido para Firestore
    }

    public ChatTopic(String id, String title, String description, String lastMessage, long lastMessageTimestamp, String iconUrl, int unreadMessagesCount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.lastMessage = lastMessage;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.iconUrl = iconUrl;
        this.unreadMessagesCount = unreadMessagesCount;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public int getUnreadMessagesCount() {
        return unreadMessagesCount;
    }

    public void setUnreadMessagesCount(int unreadMessagesCount) {
        this.unreadMessagesCount = unreadMessagesCount;
    }
}