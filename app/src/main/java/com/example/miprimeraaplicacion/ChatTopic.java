package com.example.miprimeraaplicacion;

public class ChatTopic {
    private String id;
    private String name;
    private String description;
    private String lastMessage;
    private long lastMessageTimestamp;
    private int unreadCount;

    public ChatTopic(String id, String name, String description, String lastMessage, long lastMessageTimestamp, int unreadCount) {
    }

    public ChatTopic(String id, String name, String description, long lastMessageTimestamp, int unreadCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.unreadCount = unreadCount;
        this.lastMessage = ""; // Inicializa lastMessage (o ajústalo según necesites)
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public long getLastMessageTimestamp() { return lastMessageTimestamp; }
    public void setLastMessageTimestamp(long lastMessageTimestamp) { this.lastMessageTimestamp = lastMessageTimestamp; }
    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}