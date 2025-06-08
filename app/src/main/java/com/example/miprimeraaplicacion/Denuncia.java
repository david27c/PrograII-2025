package com.example.miprimeraaplicacion;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Collections; // Importar Collections para la lista vacía

public class Denuncia {
    private String idDenuncia;
    private String idUsuario;
    private String titulo;
    private String descripcion;
    private String tipoDenuncia;
    private double latitud;
    private double longitud;
    private String urlImagen;
    @ServerTimestamp
    private Date fechaHora;
    private String estado;
    private List<Map<String, Object>> comments; // Campo para los comentarios

    // Constructor vacío requerido por Firestore para deserialización
    public Denuncia() {
        // Obligatorio para Firestore
    }

    public Denuncia(String idDenuncia, String idUsuario, String titulo, String descripcion,
                    String tipoDenuncia, double latitud, double longitud, String urlImagen,
                    Date fechaHora, String estado) {
        this.idDenuncia = idDenuncia;
        this.idUsuario = idUsuario;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.tipoDenuncia = tipoDenuncia;
        this.latitud = latitud;
        this.longitud = longitud;
        this.urlImagen = urlImagen;
        this.fechaHora = fechaHora;
        this.estado = estado;
        // Inicializar como lista vacía para evitar NullPointerException si no hay comentarios en Firestore
        this.comments = Collections.emptyList();
    }

    // --- Getters y Setters ---

    public String getIdDenuncia() { return idDenuncia; }
    public void setIdDenuncia(String idDenuncia) { this.idDenuncia = idDenuncia; }

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTipoDenuncia() { return tipoDenuncia; }
    public void setTipoDenuncia(String tipoDenuncia) { this.tipoDenuncia = tipoDenuncia; }

    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }

    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }

    public String getUrlImagen() { return urlImagen; }
    public void setUrlImagen(String urlImagen) { this.urlImagen = urlImagen; }

    public Date getFechaHora() { return fechaHora; }
    public void setFechaHora(Date fechaHora) { this.fechaHora = fechaHora; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    // Getter y Setter para comments
    public List<Map<String, Object>> getComments() {
        return comments;
    }
    public void setComments(List<Map<String, Object>> comments) {
        this.comments = comments;
    }
}