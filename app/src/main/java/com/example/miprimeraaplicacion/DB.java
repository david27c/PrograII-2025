package com.example.miprimeraaplicacion;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;

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
    }

    public String administrar_bebidas(String accion, String[] datos) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            String mensaje = "ok";
            ContentValues values = new ContentValues();

            switch (accion) {
                case "nuevo":
                    values.put("codigo", datos[1]);
                    values.put("descripcion", datos[2]);
                    values.put("marca", datos[3]);
                    values.put("presentacion", datos[4]);
                    values.put("precio", datos[5]);
                    values.put("urlFoto", datos[6]);
                    db.insert("bebidas", null, values);
                    break;
                case "modificar":
                    values.put("codigo", datos[1]);
                    values.put("descripcion", datos[2]);
                    values.put("marca", datos[3]);
                    values.put("presentacion", datos[4]);
                    values.put("precio", datos[5]);
                    values.put("urlFoto", datos[6]);
                    db.update("bebidas", values, "idBebida = ?", new String[]{datos[0]});
                    break;
                case "eliminar":
                    db.delete("bebidas", "idBebida = ?", new String[]{datos[0]});
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + accion);
            }
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