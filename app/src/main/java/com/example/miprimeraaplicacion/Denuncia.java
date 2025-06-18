package com.example.miprimeraaplicacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Denuncia {
    private String idDenuncia;
    private String idUsuario;
    private String titulo;
    private String descripcion;
    private String tipoDenuncia; // Asegúrate de que esto sea String
    private double latitud;
    private double longitud;
    private String urlImagen;
    private String fechaHora; // Asegúrate de que esto sea String
    private String estado;
    private List<Map<String, Object>> comments; // Este es el campo para los comentarios

    // Constructor principal para crear nuevas Denuncias o cargar desde DB sin comentarios iniciales
    // (los comentarios se añaden después o se cargan vía setter desde DBLocal)
    public Denuncia(String idDenuncia, String idUsuario, String titulo, String descripcion,
                    String tipoDenuncia, double latitud, double longitud, String urlImagen,
                    String fechaHora, String estado) {
        this.idDenuncia = idDenuncia;
        this.idUsuario = idUsuario;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.tipoDenuncia = tipoDenuncia; // Asignar directamente el String
        this.latitud = latitud;
        this.longitud = longitud;
        this.urlImagen = urlImagen;
        this.fechaHora = fechaHora;
        this.estado = estado;
        this.comments = new ArrayList<>(); // Inicializa la lista de comentarios como vacía
    }

    // Constructor secundario para cuando ya tienes la lista de comentarios (útil para GSON/DB)
    public Denuncia(String idDenuncia, String idUsuario, String titulo, String descripcion,
                    String tipoDenuncia, double latitud, double longitud, String urlImagen,
                    String fechaHora, String estado, List<Map<String, Object>> comments) {
        this(idDenuncia, idUsuario, titulo, descripcion, tipoDenuncia, latitud, longitud, urlImagen, fechaHora, estado);
        this.comments = comments != null ? comments : new ArrayList<>();
    }

    // --- Getters ---
    public String getIdDenuncia() { return idDenuncia; }
    public String getIdUsuario() { return idUsuario; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getTipoDenuncia() { return tipoDenuncia; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
    public String getUrlImagen() { return urlImagen; }
    public String getFechaHora() { return fechaHora; }
    public String getEstado() { return estado; }
    public List<Map<String, Object>> getComments() {
        // Asegura que siempre devuelva una lista, no null
        return comments != null ? comments : Collections.emptyList();
    }

    // --- Setters ---
    public void setIdDenuncia(String idDenuncia) { this.idDenuncia = idDenuncia; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setTipoDenuncia(String tipoDenuncia) { this.tipoDenuncia = tipoDenuncia; }
    public void setLatitud(double latitud) { this.latitud = latitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }
    public void setUrlImagen(String urlImagen) { this.urlImagen = urlImagen; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }
    public void setEstado(String estado) { this.estado = estado; }

    // Setter para la lista de comentarios (usado por DBLocal después de deserializar)
    public void setComments(List<Map<String, Object>> comments) {
        this.comments = comments;
    }

    // Método para agregar un comentario individual
    public void addComment(String userId, String username, String text, String timestamp) {
        if (this.comments == null) {
            this.comments = new ArrayList<>(); // Inicializa si es null
        }
        Map<String, Object> comment = new HashMap<>();
        comment.put("userId", userId);
        comment.put("username", username);
        comment.put("text", text);
        comment.put("timestamp", timestamp); // Usa el timestamp como String
        this.comments.add(comment);
    }
}