package com.example.miprimeraaplicacion;

public class Bebidas {
    private final String idBebida;
    String codigo;
    String descripcion;
    String marca;
    String presentacion;
    String precio;
    String foto;

    public Bebidas(String idBebida, String codigo, String descripcion, String marca, String presentacion, String precio, String foto) {
        this.idBebida = idBebida;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.marca = marca;
        this.presentacion = presentacion;
        this.precio = precio;
        this.foto = foto;
    }

    public Bebidas() {
        this.idBebida = "";
    }

    public String getCodigo() {
        return codigo;
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

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getIdBebida() {
        return idBebida;
    }
}