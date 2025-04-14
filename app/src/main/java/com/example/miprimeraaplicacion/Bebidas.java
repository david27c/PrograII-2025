package com.example.miprimeraaplicacion;

import java.util.ArrayList;

public class Bebidas {
    private String idBebida;
    String codigo;
    String descripcion;
    String marca;
    String presentacion;
    String precio;
    ArrayList<String> fotos; // Lista para almacenar múltiples rutas de fotos

    public Bebidas(String idBebida, String codigo, String descripcion, String marca, String presentacion, String precio, ArrayList<String> fotos) {
        this.idBebida = idBebida;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.marca = marca;
        this.presentacion = presentacion;
        this.precio = precio;
        this.fotos = fotos;
    }

    public String getIdBebida() {
        return idBebida;
    }

    public void setIdBebida(String idBebida) {
        this.idBebida = idBebida;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(String presentacion) {
        this.presentacion = presentacion;
    }

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }

    public ArrayList<String> getFotos() {
        return fotos;
    }

    public void setFotos(ArrayList<String> fotos) {
        this.fotos = fotos;
    }

    // Métodos para añadir y eliminar fotos individualmente
    public void addFoto(String fotoPath) {
        if (this.fotos == null) {
            this.fotos = new ArrayList<>();
        }
        this.fotos.add(fotoPath);
    }

    public void removeFoto(String fotoPath) {
        if (this.fotos != null) {
            this.fotos.remove(fotoPath);
        }
    }

    public String getFoto() {
        return "";
    }

    public void set_rev(String rev) {
    }

    public Iterable getUrlFotos() {
        return null;
    }

    public void get_rev() {
    }
}