package com.example.miprimeraaplicacion;

public class Report {
    private String id; // ID del reporte (generado por UUID o Firestore)
    private String userId; // ID del usuario que lo creó
    private String userName; // Nombre o correo del usuario que lo creó
    private String title;
    private String description;
    private String type; // Ej: "Baches", "Basura"
    private double latitude;
    private double longitude;
    private long timestamp; // Fecha y hora de creación (en milisegundos)
    private String status; // Ej: "pendiente", "en_proceso", "resuelto"
    private String mediaUrl; // URL de la imagen/video en Firebase Storage
    private String mediaType; // "image" o "video"
    private boolean reportToAuthorities; // Si se marcó la casilla de reportar a autoridades

    public Report() {
        // Constructor vacío requerido para Firestore
    }

    public Report(String id, String userId, String userName, String title, String description, String type, double latitude, double longitude, long timestamp, String status, String mediaUrl, String mediaType, boolean reportToAuthorities) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.title = title;
        this.description = description;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.status = status;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
        this.reportToAuthorities = reportToAuthorities;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public boolean isReportToAuthorities() {
        return reportToAuthorities;
    }

    public void setReportToAuthorities(boolean reportToAuthorities) {
        this.reportToAuthorities = reportToAuthorities;
    }
}