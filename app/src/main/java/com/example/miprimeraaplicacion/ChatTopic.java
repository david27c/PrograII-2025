package com.example.miprimeraaplicacion;

public class ChatTopic {
    private String id;
    private String name;
    private String description; // AÑADIDO: Campo para la descripción del tema
    private String lastMessage; // Campo para el último mensaje real
    private long lastMessageTimestamp;
    private int unreadCount;

    public ChatTopic() {
        // Constructor vacío requerido por Firestore
    }

    // Constructor actualizado para que coincida con los parámetros que le pasas
    public ChatTopic(String id, String name, String description, long lastMessageTimestamp, int unreadCount) {
        this.id = id;
        this.name = name;
        this.description = description; // Asigna la descripción
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.unreadCount = unreadCount;
        this.lastMessage = ""; // Inicializa lastMessage (o ajústalo según necesites)
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // NUEVO: Getter y Setter para la descripción
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Getters y Setters existentes para lastMessage, lastMessageTimestamp, unreadCount
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public long getLastMessageTimestamp() { return lastMessageTimestamp; }
    public void setLastMessageTimestamp(long lastMessageTimestamp) { this.lastMessageTimestamp = lastMessageTimestamp; }
    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}