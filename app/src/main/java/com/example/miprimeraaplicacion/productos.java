package com.example.miprimeraaplicacion;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONObject;

public class productos {
    String idProducto;
    String codigo;
    String descripcion;
    String marca;
    String presentacion;
    String precio;
    String foto;
    String foto1;
    String foto2;
    private int stock;
    private double precioCompra;
    private double precioVenta;
    private double porcentajeGanancia;

    public productos(String idProducto, String codigo, String descripcion, String marca, String presentacion, String precio, String foto, String foto1, String foto2) {
        this.idProducto = idProducto;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.marca = marca;
        this.presentacion = presentacion;
        this.precio = precio;
        this.foto = foto;
        this.foto1 = foto1;
        this.foto2 = foto2;
    }

    // Nuevo constructor que incluye stock y cálculos
    public productos(String idProducto, String codigo, String descripcion, String marca, String presentacion, double precioCompra, double precioVenta, int stock, String foto, String foto1, String foto2) {
        this.idProducto = idProducto;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.marca = marca;
        this.presentacion = presentacion;
        this.precioCompra = precioCompra;
        this.precioVenta = precioVenta;
        this.stock = stock;
        this.porcentajeGanancia = calcularPorcentajeGanancia();
        this.foto = foto;
        this.foto1 = foto1;
        this.foto2 = foto2;
    }

    private double calcularPorcentajeGanancia() {
        if (precioCompra == 0) return 0;
        return ((precioVenta - precioCompra) / precioCompra) * 100;
    }

    // Getters y Setters originales
    public String getidProducto() { return idProducto; }
    public void setidProducto(String idProducto) { this.idProducto = idProducto; }

    public String getcodigo() { return codigo; }
    public void setcodigo(String codigo) { this.codigo = codigo; }

    public String getdescripcion() { return descripcion; }
    public void setdescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getmarca() { return marca; }
    public void setmarca(String marca) { this.marca = marca; }

    public String getpresentacion() { return presentacion; }
    public void setpresentacion(String presentacion) { this.presentacion = presentacion; }

    public String getprecio() { return precio; }
    public void setprecio(String precio) { this.precio = precio; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public String getFoto1() { return foto1; }
    public void setFoto1(String foto1) { this.foto1 = foto1; }

    public String getFoto2() { return foto2; }
    public void setFoto2(String foto2) { this.foto2 = foto2; }

    // Nuevos Getters y Setters
    public double getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(double precioCompra) {
        this.precioCompra = precioCompra;
        this.porcentajeGanancia = calcularPorcentajeGanancia();
    }

    public double getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(double precioVenta) {
        this.precioVenta = precioVenta;
        this.porcentajeGanancia = calcularPorcentajeGanancia();
    }

    public double getPorcentajeGanancia() { return porcentajeGanancia; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    // Método para insertar en SQLite
    public void insertarProducto(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put("id", idProducto);
        values.put("nombre", descripcion); // usando descripcion como "nombre"
        values.put("precio_compra", precioCompra);
        values.put("precio_venta", precioVenta);
        values.put("porcentaje_ganancia", porcentajeGanancia);
        values.put("stock", stock);
        db.insert("productos", null, values);
    }

    // Método para generar JSON para CouchDB
    public JSONObject toJSON() {
        JSONObject productoJson = new JSONObject();
        try {
            productoJson.put("_id", idProducto);
            productoJson.put("nombre", descripcion); // usando descripcion como nombre
            productoJson.put("precio_compra", precioCompra);
            productoJson.put("precio_venta", precioVenta);
            productoJson.put("porcentaje_ganancia", porcentajeGanancia);
            productoJson.put("stock", stock);
            productoJson.put("tipo", "producto");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return productoJson;
    }
}