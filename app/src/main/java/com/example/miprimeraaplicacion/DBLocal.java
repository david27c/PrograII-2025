package com.example.miprimeraaplicacion;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date; // Importar java.util.Date
import java.util.List;

public class DBLocal extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "denuncias_app.db";
    private static final int DATABASE_VERSION = 2; // Incrementar la versión de la DB

    // Nombres de la tabla y columnas
    private static final String TABLE_DENUNCIAS = "denuncias";
    private static final String COL_ID_DENUNCIA = "id_denuncia";
    private static final String COL_ID_USUARIO = "id_usuario";
    private static final String COL_TITULO = "titulo";
    private static final String COL_DESCRIPCION = "descripcion";
    private static final String COL_TIPO_DENUNCIA = "tipo_denuncia";
    private static final String COL_LATITUD = "latitud";
    private static final String COL_LONGITUD = "longitud";
    private static final String COL_URL_IMAGEN = "url_imagen";
    private static final String COL_FECHA_HORA = "fecha_hora"; // Este campo causaba el error
    private static final String COL_ESTADO = "estado";

    // Sentencia SQL para crear la tabla
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_DENUNCIAS + " (" +
                    COL_ID_DENUNCIA + " TEXT PRIMARY KEY," +
                    COL_ID_USUARIO + " TEXT," +
                    COL_TITULO + " TEXT," +
                    COL_DESCRIPCION + " TEXT," +
                    COL_TIPO_DENUNCIA + " TEXT," +
                    COL_LATITUD + " REAL," +
                    COL_LONGITUD + " REAL," +
                    COL_URL_IMAGEN + " TEXT," +
                    COL_FECHA_HORA + " INTEGER," + // Guardar fecha_hora como INTEGER (timestamp)
                    COL_ESTADO + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_DENUNCIAS;

    public DBLocal(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Implementa aquí la lógica de migración si tu esquema de base de datos cambia en el futuro.
        // Por ahora, simplemente eliminamos y recreamos la tabla (lo cual borrará los datos existentes).
        Log.w("DBLocal", "Actualizando la base de datos de la versión " + oldVersion + " a " + newVersion + ", lo que destruirá todos los datos antiguos.");
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    /**
     * Inserta una nueva denuncia en la base de datos local.
     * @param denuncia El objeto Denuncia a insertar.
     * @return El ID de la fila recién insertada, o -1 si ocurrió un error.
     */
    public String insertarDenuncia(Denuncia denuncia) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_ID_DENUNCIA, denuncia.getIdDenuncia());
        values.put(COL_ID_USUARIO, denuncia.getIdUsuario());
        values.put(COL_TITULO, denuncia.getTitulo());
        values.put(COL_DESCRIPCION, denuncia.getDescripcion());
        values.put(COL_TIPO_DENUNCIA, denuncia.getTipoDenuncia());
        values.put(COL_LATITUD, denuncia.getLatitud());
        values.put(COL_LONGITUD, denuncia.getLongitud());
        values.put(COL_URL_IMAGEN, denuncia.getUrlImagen());
        // Convertir Date a long (milisegundos) antes de guardar
        values.put(COL_FECHA_HORA, denuncia.getFechaHora() != null ? denuncia.getFechaHora().getTime() : null);
        values.put(COL_ESTADO, denuncia.getEstado());

        try {
            long newRowId = db.insert(TABLE_DENUNCIAS, null, values);
            if (newRowId != -1) {
                Log.d("DBLocal", "Denuncia insertada correctamente: " + denuncia.getIdDenuncia());
                return denuncia.getIdDenuncia();
            } else {
                Log.e("DBLocal", "Error al insertar denuncia.");
                return null;
            }
        } finally {
            db.close();
        }
    }

    /**
     * Obtiene todas las denuncias de la base de datos local.
     * @return Una lista de objetos Denuncia.
     */
    public List<Denuncia> obtenerTodasLasDenuncias() {
        List<Denuncia> denuncias = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                COL_ID_DENUNCIA,
                COL_ID_USUARIO,
                COL_TITULO,
                COL_DESCRIPCION,
                COL_TIPO_DENUNCIA,
                COL_LATITUD,
                COL_LONGITUD,
                COL_URL_IMAGEN,
                COL_FECHA_HORA,
                COL_ESTADO
        };

        Cursor cursor = null;
        try {
            cursor = db.query(
                    TABLE_DENUNCIAS,
                    projection,
                    null, null, null, null,
                    COL_FECHA_HORA + " DESC" // Ordenar por fecha_hora descendente
            );

            while (cursor.moveToNext()) {
                String idDenuncia = cursor.getString(cursor.getColumnIndexOrThrow(COL_ID_DENUNCIA));
                String idUsuario = cursor.getString(cursor.getColumnIndexOrThrow(COL_ID_USUARIO));
                String titulo = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITULO));
                String descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPCION));
                String tipoDenuncia = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIPO_DENUNCIA));
                double latitud = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LATITUD));
                double longitud = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LONGITUD));
                String urlImagen = cursor.getString(cursor.getColumnIndexOrThrow(COL_URL_IMAGEN));
                long fechaHoraMillis = cursor.getLong(cursor.getColumnIndexOrThrow(COL_FECHA_HORA));
                String estado = cursor.getString(cursor.getColumnIndexOrThrow(COL_ESTADO));

                // Convertir long (milisegundos) a Date al leer
                Date fechaHora = new Date(fechaHoraMillis);

                Denuncia denuncia = new Denuncia(
                        idDenuncia, idUsuario, titulo, descripcion, tipoDenuncia,
                        latitud, longitud, urlImagen, fechaHora, estado
                );
                denuncias.add(denuncia);
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Error al obtener todas las denuncias: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return denuncias;
    }

    /**
     * Obtiene las denuncias de un usuario específico.
     * @param userId El ID del usuario.
     * @return Una lista de objetos Denuncia.
     */
    public List<Denuncia> obtenerDenunciasPorUsuario(String userId) {
        List<Denuncia> denuncias = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                COL_ID_DENUNCIA,
                COL_ID_USUARIO,
                COL_TITULO,
                COL_DESCRIPCION,
                COL_TIPO_DENUNCIA,
                COL_LATITUD,
                COL_LONGITUD,
                COL_URL_IMAGEN,
                COL_FECHA_HORA,
                COL_ESTADO
        };

        String selection = COL_ID_USUARIO + " = ?";
        String[] selectionArgs = { userId };

        Cursor cursor = null;
        try {
            cursor = db.query(
                    TABLE_DENUNCIAS,
                    projection,
                    selection,
                    selectionArgs,
                    null, null,
                    COL_FECHA_HORA + " DESC" // Ordenar por fecha_hora descendente
            );

            while (cursor.moveToNext()) {
                String idDenuncia = cursor.getString(cursor.getColumnIndexOrThrow(COL_ID_DENUNCIA));
                String idUsuario = cursor.getString(cursor.getColumnIndexOrThrow(COL_ID_USUARIO));
                String titulo = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITULO));
                String descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPCION));
                String tipoDenuncia = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIPO_DENUNCIA));
                double latitud = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LATITUD));
                double longitud = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LONGITUD));
                String urlImagen = cursor.getString(cursor.getColumnIndexOrThrow(COL_URL_IMAGEN));
                long fechaHoraMillis = cursor.getLong(cursor.getColumnIndexOrThrow(COL_FECHA_HORA));
                String estado = cursor.getString(cursor.getColumnIndexOrThrow(COL_ESTADO));

                Date fechaHora = new Date(fechaHoraMillis);

                Denuncia denuncia = new Denuncia(
                        idDenuncia, idUsuario, titulo, descripcion, tipoDenuncia,
                        latitud, longitud, urlImagen, fechaHora, estado
                );
                denuncias.add(denuncia);
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Error al obtener denuncias por usuario: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return denuncias;
    }

    /**
     * Obtiene una denuncia específica por su ID.
     * @param idDenuncia El ID de la denuncia.
     * @return El objeto Denuncia si se encuentra, o null si no.
     */
    public Denuncia obtenerDenunciaPorId(String idDenuncia) {
        SQLiteDatabase db = this.getReadableDatabase();
        Denuncia denuncia = null;

        String[] projection = {
                COL_ID_DENUNCIA,
                COL_ID_USUARIO,
                COL_TITULO,
                COL_DESCRIPCION,
                COL_TIPO_DENUNCIA,
                COL_LATITUD,
                COL_LONGITUD,
                COL_URL_IMAGEN,
                COL_FECHA_HORA,
                COL_ESTADO
        };

        String selection = COL_ID_DENUNCIA + " = ?";
        String[] selectionArgs = { idDenuncia };

        Cursor cursor = null;
        try {
            cursor = db.query(
                    TABLE_DENUNCIAS,
                    projection,
                    selection,
                    selectionArgs,
                    null, null, null
            );

            if (cursor.moveToFirst()) {
                String retrievedIdDenuncia = cursor.getString(cursor.getColumnIndexOrThrow(COL_ID_DENUNCIA));
                String idUsuario = cursor.getString(cursor.getColumnIndexOrThrow(COL_ID_USUARIO));
                String titulo = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITULO));
                String descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPCION));
                String tipoDenuncia = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIPO_DENUNCIA));
                double latitud = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LATITUD));
                double longitud = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LONGITUD));
                String urlImagen = cursor.getString(cursor.getColumnIndexOrThrow(COL_URL_IMAGEN));
                long fechaHoraMillis = cursor.getLong(cursor.getColumnIndexOrThrow(COL_FECHA_HORA));
                String estado = cursor.getString(cursor.getColumnIndexOrThrow(COL_ESTADO));

                Date fechaHora = new Date(fechaHoraMillis);

                denuncia = new Denuncia(
                        retrievedIdDenuncia, idUsuario, titulo, descripcion, tipoDenuncia,
                        latitud, longitud, urlImagen, fechaHora, estado
                );
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Error al obtener denuncia por ID: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return denuncia;
    }

    /**
     * Actualiza una denuncia existente en la base de datos local.
     * @param denuncia El objeto Denuncia con los datos actualizados.
     * @return El número de filas afectadas, o -1 si ocurrió un error.
     */
    public int actualizarDenuncia(Denuncia denuncia) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_ID_USUARIO, denuncia.getIdUsuario());
        values.put(COL_TITULO, denuncia.getTitulo());
        values.put(COL_DESCRIPCION, denuncia.getDescripcion());
        values.put(COL_TIPO_DENUNCIA, denuncia.getTipoDenuncia());
        values.put(COL_LATITUD, denuncia.getLatitud());
        values.put(COL_LONGITUD, denuncia.getLongitud());
        values.put(COL_URL_IMAGEN, denuncia.getUrlImagen());
        // Convertir Date a long (milisegundos) antes de guardar
        values.put(COL_FECHA_HORA, denuncia.getFechaHora() != null ? denuncia.getFechaHora().getTime() : null);
        values.put(COL_ESTADO, denuncia.getEstado());

        String selection = COL_ID_DENUNCIA + " = ?";
        String[] selectionArgs = { denuncia.getIdDenuncia() };

        int count = -1;
        try {
            count = db.update(
                    TABLE_DENUNCIAS,
                    values,
                    selection,
                    selectionArgs);
            if (count > 0) {
                Log.d("DBLocal", "Denuncia actualizada correctamente: " + denuncia.getIdDenuncia());
            } else {
                Log.e("DBLocal", "Error al actualizar denuncia o denuncia no encontrada.");
            }
        } finally {
            db.close();
        }
        return count;
    }

    /**
     * Elimina una denuncia de la base de datos local.
     * @param idDenuncia El ID de la denuncia a eliminar.
     * @return El número de filas afectadas, o -1 si ocurrió un error.
     */
    public int eliminarDenuncia(String idDenuncia) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_ID_DENUNCIA + " = ?";
        String[] selectionArgs = { idDenuncia };
        int count = -1;
        try {
            count = db.delete(
                    TABLE_DENUNCIAS,
                    selection,
                    selectionArgs);
            if (count > 0) {
                Log.d("DBLocal", "Denuncia eliminada correctamente: " + idDenuncia);
            } else {
                Log.e("DBLocal", "Error al eliminar denuncia o denuncia no encontrada.");
            }
        } finally {
            db.close();
        }
        return count;
    }
}