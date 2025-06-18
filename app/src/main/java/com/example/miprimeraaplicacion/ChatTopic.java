package com.example.miprimeraaplicacion;

public class ChatTopic {
    private String id;
    private String name;
    private String description;
    private String lastMessage;
    private long lastMessageTimestamp;
    private int unreadCount;

    public ChatTopic(String id, String name, String description, String lastMessage, long lastMessageTimestamp, int unreadCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.lastMessage = lastMessage;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.unreadCount = unreadCount;
    }

    public ChatTopic(String id, String name, String description, long lastMessageTimestamp, int unreadCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.unreadCount = unreadCount;
        this.lastMessage = "";
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getLastMessage() { return lastMessage; }
    public long getLastMessageTimestamp() { return lastMessageTimestamp; }
    public int getUnreadCount() { return unreadCount; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public void setLastMessageTimestamp(long lastMessageTimestamp) { this.lastMessageTimestamp = lastMessageTimestamp; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}