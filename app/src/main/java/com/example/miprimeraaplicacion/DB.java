package com.example.miprimeraaplicacion;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Arrays;

public class DB extends SQLiteOpenHelper {
    public static final String COL_ID_BEBIDA = "idBebida";
    public static final String COL_CODIGO = "codigo";
    public static final String COL_DESCRIPCION = "descripcion";
    public static final String COL_MARCA = "marca";
    public static final String COL_PRESENTACION = "presentacion";
    public static final String COL_PRECIO = "precio";
    public static final String COL_URL_FOTOS = "urlFotos";
    private static final String DATABASE_NAME = "bebidas";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLA_BEBIDAS = "bebidas";
    private static final String SQLdb = "CREATE TABLE " + TABLA_BEBIDAS + " (" +
            COL_ID_BEBIDA + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_CODIGO + " TEXT, " +
            COL_DESCRIPCION + " TEXT, " +
            COL_MARCA + " TEXT, " +
            COL_PRESENTACION + " TEXT, " +
            COL_PRECIO + " REAL, " +
            COL_URL_FOTOS + " TEXT)";

    public DB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQLdb);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
       }

    public String administrar_bebidas(String accion, String[] datos) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            String mensaje = "ok";
            ContentValues values = new ContentValues();
            switch (accion) {
                case "nuevo":
                    values.put(COL_CODIGO, datos[0]);
                    values.put(COL_DESCRIPCION, datos[1]);
                    values.put(COL_MARCA, datos[2]);
                    values.put(COL_PRESENTACION, datos[3]);
                    values.put(COL_PRECIO, Double.parseDouble(datos[4])); // Convertir precio a Double
                    values.put(COL_URL_FOTOS, datos[5]); // Almacenar rutas separadas por un delimitador
                    long newRowId = db.insert(TABLA_BEBIDAS, null, values);
                    if (newRowId == -1) {
                        mensaje = "Error al insertar la bebida.";
                    }
                    break;
                case "modificar":
                    values.put(COL_CODIGO, datos[1]);
                    values.put(COL_DESCRIPCION, datos[2]);
                    values.put(COL_MARCA, datos[3]);
                    values.put(COL_PRESENTACION, datos[4]);
                    values.put(COL_PRECIO, Double.parseDouble(datos[5])); // Convertir precio a Double
                    values.put(COL_URL_FOTOS, datos[6]); // Almacenar rutas separadas por un delimitador
                    int rowsAffected = db.update(TABLA_BEBIDAS, values, COL_ID_BEBIDA + " = ?", new String[]{datos[0]});
                    if (rowsAffected == 0) {
                        mensaje = "No se encontró la bebida para modificar.";
                    }
                    break;
                case "eliminar":
                    int rowsDeleted = db.delete(TABLA_BEBIDAS, COL_ID_BEBIDA + " = ?", new String[]{datos[0]});
                    if (rowsDeleted == 0) {
                        mensaje = "No se encontró la bebida para eliminar.";
                    }
                    break;
            }
            db.close();
            return mensaje;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public Cursor lista_bebidas() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLA_BEBIDAS, null);
    }

    // Función para obtener una bebida por su ID (necesaria para la edición)
    public Bebidas obtener_bebida(String id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLA_BEBIDAS,
                new String[]{COL_ID_BEBIDA, COL_CODIGO, COL_DESCRIPCION, COL_MARCA, COL_PRESENTACION, COL_PRECIO, COL_URL_FOTOS},
                COL_ID_BEBIDA + "=?",
                new String[]{id},
                null,
                null,
                null,
                null
        );
        Bebidas bebida = null;
        if (cursor.moveToFirst()) {
            bebida = new Bebidas(
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_ID_BEBIDA)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_CODIGO)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPCION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_MARCA)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_PRESENTACION)),
                    String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PRECIO))), // Obtener como Double y luego a String
                    new ArrayList<>(Arrays.asList(cursor.getString(cursor.getColumnIndexOrThrow(COL_URL_FOTOS)).split(";")))
            );
            cursor.close();
        }
        db.close();
        return bebida;
    }
}