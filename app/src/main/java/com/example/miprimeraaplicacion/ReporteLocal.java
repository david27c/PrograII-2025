package com.example.miprimeraaplicacion;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "local_reports")
public class ReporteLocal {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String description;
    private String imageUrl; // Ruta local o URI de la imagen
    private String type;
    private double latitude;
    private double longitude;
    private long timestamp;
    private String status; // Ej: "pendiente_envio", "enviado"
    private String userId; // Para saber qué usuario lo creó localmente

    public ReporteLocal(String title, String description, String imageUrl, String type, double latitude, double longitude, long timestamp, String status, String userId) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.status = status;
        this.userId = userId;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}