package com.example.miprimeraaplicacion;

public class Notification {
    private String id;
    private String userId; // Si es necesario, sino se puede remover
    private String type;
    private String title;    // AÑADIDO: Atributo 'title'
    private String message;
    private long timestamp;
    private boolean read;
    private String relatedId; // Si es necesario, sino se puede remover

    // Constructor corregido para incluir 'title' y asignar 'message'
    public Notification(String id, String title, String message, long timestamp, boolean read) {
        this.id = id;
        this.title = title;    // Asignación de 'title'
        this.message = message;
        this.timestamp = timestamp;
        this.read = read;
        // Los campos userId, type, relatedId no están en este constructor,
        // si son obligatorios, deberían añadirse o tener un constructor más completo.
        // Por ahora, se inicializan a null o valores por defecto.
        this.userId = null; // O "" si prefieres String vacío
        this.type = null;   // O ""
        this.relatedId = null; // O ""
    }

    // Constructor más completo si 'userId', 'type' y 'relatedId' son siempre necesarios desde el inicio
    public Notification(String id, String userId, String type, String title, String message, long timestamp, boolean read, String relatedId) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.read = read;
        this.relatedId = relatedId;
    }


    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getType() { return type; }
    public String getTitle() { return title; } // AÑADIDO: Getter para 'title'
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
    public boolean isRead() { return read; }
    public String getRelatedId() { return relatedId; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setType(String type) { this.type = type; }
    public void setTitle(String title) { this.title = title; } // AÑADIDO: Setter para 'title'
    public void setMessage(String message) { this.message = message; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setRead(boolean read) { this.read = read; }
    public void setRelatedId(String relatedId) { this.relatedId = relatedId; }
}