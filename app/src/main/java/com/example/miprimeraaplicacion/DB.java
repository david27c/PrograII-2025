package com.example.miprimeraaplicacion;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DB extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "bebidas";
    private static final int DATABASE_VERSION = 1;
    private static final String SQLdb = "CREATE TABLE bebidas (idBebida INTEGER PRIMARY KEY AUTOINCREMENT, codigo TEXT, descripcion TEXT, marca TEXT, presentacion TEXT, precio TEXT, urlFoto TEXT)";
    public DB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQLdb);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Actualizar la estrucutra de la base de datos si es necesario
    }
    public String administrar_bebidas(String accion, String[] datos) {
        try{
            SQLiteDatabase db = getWritableDatabase();
            String mensaje = "ok", sql = "";
            switch (accion) {
                case "nuevo":
                    sql = "INSERT INTO bebidas (codigo, descripcion, marca, presentacion, precio, urlFoto) VALUES ('"+ datos[1] +"', '" + datos[2] + "', '" + datos[3] + "', '" + datos[4] + "', '" + datos[5] + "', '" + datos[6] + "')";
                    break;
                case "modificar":
                    sql = "UPDATE bebidas SET codigo = '" + datos[1] + "', descricpion = '" + datos[2] + "', marca = '" + datos[3] + "', presentacion = '" + datos[4] + "', precio = '" + datos[5] + "', urlFoto = '" + datos[6] + "' WHERE idBebida = " + datos[0];
                    break;
                case "eliminar":
                    sql = "DELETE FROM bebidas WHERE idBebida = " + datos[0];
                    break;
            }
            db.execSQL(sql);
            db.close();
            return mensaje;
        } catch (Exception e) {
            return e.getMessage();
        }
    }
    public Cursor lista_bebidas() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM bebidas", null);
    }
}