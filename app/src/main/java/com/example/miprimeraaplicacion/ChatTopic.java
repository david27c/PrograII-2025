package com.example.miprimeraaplicacion;

public class ChatTopic {
    private String id;
    private String title;
    private String description;
    private String lastMessage;
    private long lastMessageTimestamp;
    private String iconUrl;
    private int unreadMessagesCount;

    public ChatTopic() {
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