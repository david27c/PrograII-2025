package com.example.miprimeraaplicacion;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date; // Importar java.util.Date
import java.util.List;

public class DBLocal extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "denuncias_app.db";
    // *** IMPORTANTE: Incrementa la versión de la DB porque estamos añadiendo una nueva tabla ***
    private static final int DATABASE_VERSION = 3;

    // Nombres de la tabla y columnas para DENUNCIAS
    private static final String TABLE_DENUNCIAS = "denuncias";
    private static final String COL_ID_DENUNCIA = "id_denuncia";
    private static final String COL_ID_USUARIO = "id_usuario";
    private static final String COL_TITULO = "titulo";
    private static final String COL_DESCRIPCION = "descripcion";
    private static final String COL_TIPO_DENUNCIA = "tipo_denuncia";
    private static final String COL_LATITUD = "latitud";
    private static final String COL_LONGITUD = "longitud";
    private static final String COL_URL_IMAGEN = "url_imagen";
    private static final String COL_FECHA_HORA = "fecha_hora";
    private static final String COL_ESTADO = "estado";

    // Nombres de la tabla y columnas para USUARIOS
    private static final String TABLE_USERS = "users";
    private static final String COL_USER_ID = "user_id"; // Puedes usar un UUID o el email como ID
    private static final String COL_USER_EMAIL = "email";
    private static final String COL_USER_PASSWORD = "password";

    // Sentencia SQL para crear la tabla de DENUNCIAS
    private static final String SQL_CREATE_DENUNCIAS_TABLE =
            "CREATE TABLE " + TABLE_DENUNCIAS + " (" +
                    COL_ID_DENUNCIA + " TEXT PRIMARY KEY," +
                    COL_ID_USUARIO + " TEXT," +
                    COL_TITULO + " TEXT," +
                    COL_DESCRIPCION + " TEXT," +
                    COL_TIPO_DENUNCIA + " TEXT," +
                    COL_LATITUD + " REAL," +
                    COL_LONGITUD + " REAL," +
                    COL_URL_IMAGEN + " TEXT," +
                    COL_FECHA_HORA + " INTEGER," +
                    COL_ESTADO + " TEXT)";

    // Sentencia SQL para crear la tabla de USUARIOS
    private static final String SQL_CREATE_USERS_TABLE =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COL_USER_ID + " TEXT PRIMARY KEY," + // O INTEGER PRIMARY KEY AUTOINCREMENT
                    COL_USER_EMAIL + " TEXT UNIQUE," +   // El email debe ser único
                    COL_USER_PASSWORD + " TEXT)";

    private static final String SQL_DELETE_DENUNCIAS_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_DENUNCIAS;

    private static final String SQL_DELETE_USERS_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_USERS;


    public DBLocal(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_DENUNCIAS_TABLE);
        db.execSQL(SQL_CREATE_USERS_TABLE); // Crea también la tabla de usuarios
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("DBLocal", "Actualizando la base de datos de la versión " + oldVersion + " a " + newVersion + ", lo que destruirá todos los datos antiguos.");
        db.execSQL(SQL_DELETE_DENUNCIAS_TABLE);
        db.execSQL(SQL_DELETE_USERS_TABLE); // Elimina también la tabla de usuarios
        onCreate(db);
    }

    // ******************************************************
    // *** MÉTODOS EXISTENTES PARA LA GESTIÓN DE DENUNCIAS ***
    // ******************************************************

    /**
     * Inserta una nueva denuncia en la base de datos local.
     * @param denuncia El objeto Denuncia a insertar.
     * @return El ID de la denuncia insertada si fue exitosa, o null si ocurrió un error.
     */
    public String insertarDenuncia(Denuncia denuncia) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = getContentValues(denuncia);

        long newRowId = -1;
        try {
            newRowId = db.insert(TABLE_DENUNCIAS, null, values);
            if (newRowId != -1) {
                Log.d("DBLocal", "Denuncia insertada correctamente: " + denuncia.getIdDenuncia());
                return denuncia.getIdDenuncia(); // Devolvemos el ID de la denuncia
            } else {
                Log.e("DBLocal", "Error al insertar denuncia.");
                return null;
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Excepción al insertar denuncia: " + e.getMessage());
            return null;
        } finally {
            db.close();
        }
    }

    /**
     * Obtiene un ContentValues para insertar una denuncia.
     * @param denuncia El objeto Denuncia.
     * @return ContentValues con los datos de la denuncia.
     */
    @NonNull
    private ContentValues getContentValues(Denuncia denuncia) {
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
        return values;
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
        ContentValues values = getValues(denuncia);

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
        } catch (Exception e) {
            Log.e("DBLocal", "Excepción al actualizar denuncia: " + e.getMessage());
            return -1;
        } finally {
            db.close();
        }
        return count;
    }

    /**
     * Obtiene un ContentValues para actualizar una denuncia.
     * No incluye el ID de la denuncia, ya que se usa en la cláusula WHERE.
     * @param denuncia El objeto Denuncia.
     * @return ContentValues con los datos de la denuncia.
     */
    @NonNull
    private ContentValues getValues(Denuncia denuncia) {
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
        return values;
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
        } catch (Exception e) {
            Log.e("DBLocal", "Excepción al eliminar denuncia: " + e.getMessage());
            return -1;
        } finally {
            db.close();
        }
        return count;
    }

    // ***********************************************
    // *** NUEVOS MÉTODOS PARA LA GESTIÓN DE USUARIOS ***
    // ***********************************************

    /**
     * Registra un nuevo usuario en la base de datos local.
     * @param email El email del usuario.
     * @param password La contraseña del usuario.
     * @return true si el usuario se registró exitosamente, false si ya existe o hay un error.
     */
    public boolean registerUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        // Para user_id, puedes generar un UUID o usar el email directamente si es siempre único.
        // Para simplicidad en esta fase, podemos usar un UUID generado.
        // En una app real, si el email es la clave principal, podrías usarlo.
        String userId = java.util.UUID.randomUUID().toString();

        values.put(COL_USER_ID, userId);
        values.put(COL_USER_EMAIL, email);
        values.put(COL_USER_PASSWORD, password); // NOTA: En una app real, hashea las contraseñas.

        long newRowId = -1;
        try {
            newRowId = db.insert(TABLE_USERS, null, values);
            if (newRowId != -1) {
                Log.d("DBLocal", "Usuario registrado correctamente: " + email);
                return true;
            } else {
                Log.e("DBLocal", "Error al registrar usuario. Email puede ya existir.");
                return false;
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Excepción al registrar usuario: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }

    /**
     * Intenta iniciar sesión con un email y contraseña.
     * @param email El email del usuario.
     * @param password La contraseña del usuario.
     * @return El ID del usuario si las credenciales son válidas, o null en caso contrario.
     */
    public String loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                COL_USER_ID
        };

        String selection = COL_USER_EMAIL + " = ? AND " +
                COL_USER_PASSWORD + " = ?"; // NOTA: Comparación de texto plano.
        String[] selectionArgs = { email, password };

        Cursor cursor = null;
        String userId = null;
        try {
            cursor = db.query(
                    TABLE_USERS,   // The table to query
                    projection,                          // The columns to return
                    selection,                           // The columns for the WHERE clause
                    selectionArgs,                       // The values for the WHERE clause
                    null,                                // don't group the rows
                    null,                                // don't filter by row groups
                    null                                 // The sort order
            );

            if (cursor.moveToFirst()) {
                userId = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ID));
                Log.d("DBLocal", "Inicio de sesión exitoso para: " + email);
            } else {
                Log.d("DBLocal", "Fallo de inicio de sesión para: " + email + ". Credenciales incorrectas.");
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Excepción al iniciar sesión: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return userId;
    }

    /**
     * Verifica si un usuario con el email dado ya existe.
     * @param email El email a verificar.
     * @return true si el usuario existe, false en caso contrario.
     */
    public boolean checkUserExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = { COL_USER_ID };
        String selection = COL_USER_EMAIL + " = ?";
        String[] selectionArgs = { email };

        Cursor cursor = null;
        boolean exists = false;
        try {
            cursor = db.query(
                    TABLE_USERS,
                    projection,
                    selection,
                    selectionArgs,
                    null, null, null
            );
            exists = cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e("DBLocal", "Error al verificar si el usuario existe: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return exists;
    }

}