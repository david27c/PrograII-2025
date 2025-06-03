package com.example.miprimeraaplicacion;

public class Report {
    private String id;
    private String userId;
    private String description;
    private String type;
    private String location;
    private String imageUrl;
    private long timestamp;
    private String status;
    private boolean reportToAuthorities;

    public Report() {
        // Constructor vacío requerido por Firestore
    }

    public Report(String id, String userId, String description, String type, String location, String imageUrl, long timestamp, String status, boolean reportToAuthorities) {
        this.id = id;
        this.userId = userId;
        this.description = description;
        this.type = type;
        this.location = location;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.status = status;
        this.reportToAuthorities = reportToAuthorities;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isReportToAuthorities() { return reportToAuthorities; }
    public void setReportToAuthorities(boolean reportToAuthorities) { this.reportToAuthorities = reportToAuthorities; }
}