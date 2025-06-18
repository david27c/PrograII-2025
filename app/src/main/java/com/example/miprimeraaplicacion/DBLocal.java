package com.example.miprimeraaplicacion;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DBLocal extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "denuncias_app.db";
    // ¡IMPORTANTE: INCREMENTAR LA VERSIÓN DE LA BASE DE DATOS CADA VEZ QUE CAMBIES EL ESQUEMA!
    // Para que los cambios en onCreate/onUpgrade se apliquen.
    private static final int DATABASE_VERSION = 19; // <--- ¡INCREMENTADO A 19!

    // Nombres de la tabla y columnas para USUARIOS
    private static final String TABLE_USERS = "users";
    private static final String COL_USER_ID = "user_id";
    private static final String COL_EMAIL = "email";
    private static final String COL_PASSWORD = "password";
    private static final String COL_USERNAME = "username";
    private static final String COL_FULL_NAME = "full_name";
    private static final String COL_PHONE = "phone";
    private static final String COL_ADDRESS = "address";
    private static final String COL_PROFILE_IMAGE_URL = "profile_image_url";
    private static final String COL_REPORTS_COUNT = "reports_count";
    private static final String COL_SHOW_FULL_NAME_PUBLIC = "show_full_name_public";
    private static final String COL_SHOW_PROFILE_PHOTO_IN_COMMENTS = "show_profile_photo_in_comments";
    private static final String COL_SHOW_EMAIL_PUBLIC = "show_email_public";
    private static final String COL_SHOW_PHONE_PUBLIC = "show_phone_public";
    private static final String COL_LOGGED_IN_USER_ID = "logged_in_user_id"; // Nueva columna para usuario logueado

    // Nombres de la tabla y columnas para DENUNCIAS
    private static final String TABLE_DENUNCIAS = "denuncias";
    private static final String COL_DENUNCIA_ID = "denuncia_id";
    private static final String COL_DENUNCIA_USUARIO_ID = "usuario_id";
    private static final String COL_DENUNCIA_TITULO = "titulo";
    private static final String COL_DENUNCIA_DESCRIPCION = "descripcion";
    private static final String COL_DENUNCIA_TIPO = "tipo_denuncia";
    private static final String COL_DENUNCIA_LATITUD = "latitud";
    private static final String COL_DENUNCIA_LONGITUD = "longitud";
    private static final String COL_DENUNCIA_URL_IMAGEN = "url_imagen";
    private static final String COL_DENUNCIA_FECHA_HORA = "fecha_hora"; // Guarda como String
    private static final String COL_DENUNCIA_ESTADO = "estado";
    private static final String COL_DENUNCIA_COMMENTS = "comments_json"; // Guarda los comentarios como JSON String

    // Nombres de la tabla y columnas para NOTIFICACIONES
    private static final String TABLE_NOTIFICATIONS = "notifications";
    private static final String COL_NOTIFICATION_ID = "notification_id";
    private static final String COL_NOTIFICATION_USER_ID = "user_id"; // ID del usuario al que va la notificación
    private static final String COL_NOTIFICATION_TYPE = "type"; // Ej. "new_report", "comment_on_report"
    private static final String COL_NOTIFICATION_TITLE = "title"; // Título de la notificación
    private static final String COL_NOTIFICATION_MESSAGE = "message";
    private static final String COL_NOTIFICATION_TIMESTAMP = "timestamp"; // Marca de tiempo de creación
    private static final String COL_NOTIFICATION_READ = "read_status"; // 0 para no leído, 1 para leído
    private static final String COL_NOTIFICATION_RELATED_ID = "related_id"; // ID de la denuncia o comentario relacionado

    // Nombres de la tabla y columnas para MENSAJES DE CHAT
    private static final String TABLE_MESSAGES = "messages";
    private static final String COL_MESSAGE_ID = "message_id";
    private static final String COL_MESSAGE_TOPIC_ID = "chat_topic_id"; // Foreign Key a chat_topics
    private static final String COL_MESSAGE_SENDER_ID = "sender_id"; // Foreign Key a users
    private static final String COL_MESSAGE_SENDER_NAME = "sender_name"; // Nombre del remitente
    private static final String COL_MESSAGE_TEXT = "message_text";
    private static final String COL_MESSAGE_TIMESTAMP = "timestamp";

    // Nombres de la tabla y columnas para TEMAS DE CHAT (CHAT TOPICS)
    private static final String TABLE_CHAT_TOPICS = "chat_topics";
    private static final String COL_CHAT_TOPIC_ID = "chat_topic_id";
    private static final String COL_CHAT_TOPIC_NAME = "name";
    private static final String COL_CHAT_TOPIC_DESCRIPTION = "description";
    private static final String COL_CHAT_TOPIC_LAST_MESSAGE = "last_message";
    private static final String COL_CHAT_TOPIC_LAST_MESSAGE_TIMESTAMP = "last_message_timestamp";
    private static final String COL_CHAT_TOPIC_UNREAD_COUNT = "unread_count";

    // GSON para manejar JSON
    private static final Gson gson = new Gson();
    // Tipo para la lista de comentarios, necesario para GSON
    private static final Type COMMENTS_TYPE = new TypeToken<List<Map<String, Object>>>(){}.getType();


    public DBLocal(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear tabla de usuarios
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COL_USER_ID + " TEXT PRIMARY KEY,"
                + COL_EMAIL + " TEXT UNIQUE,"
                + COL_PASSWORD + " TEXT,"
                + COL_USERNAME + " TEXT,"
                + COL_FULL_NAME + " TEXT,"
                + COL_PHONE + " TEXT,"
                + COL_ADDRESS + " TEXT,"
                + COL_PROFILE_IMAGE_URL + " TEXT,"
                + COL_REPORTS_COUNT + " INTEGER DEFAULT 0,"
                + COL_SHOW_FULL_NAME_PUBLIC + " INTEGER DEFAULT 0," // 0 for false, 1 for true
                + COL_SHOW_PROFILE_PHOTO_IN_COMMENTS + " INTEGER DEFAULT 0,"
                + COL_SHOW_EMAIL_PUBLIC + " INTEGER DEFAULT 0,"
                + COL_SHOW_PHONE_PUBLIC + " INTEGER DEFAULT 0" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Crear tabla de denuncias
        String CREATE_DENUNCIAS_TABLE = "CREATE TABLE " + TABLE_DENUNCIAS + "("
                + COL_DENUNCIA_ID + " TEXT PRIMARY KEY,"
                + COL_DENUNCIA_USUARIO_ID + " TEXT,"
                + COL_DENUNCIA_TITULO + " TEXT,"
                + COL_DENUNCIA_DESCRIPCION + " TEXT,"
                + COL_DENUNCIA_TIPO + " TEXT,"
                + COL_DENUNCIA_LATITUD + " REAL,"
                + COL_DENUNCIA_LONGITUD + " REAL,"
                + COL_DENUNCIA_URL_IMAGEN + " TEXT,"
                + COL_DENUNCIA_FECHA_HORA + " TEXT," // Guarda como String
                + COL_DENUNCIA_ESTADO + " TEXT,"
                + COL_DENUNCIA_COMMENTS + " TEXT" + ")"; // Guarda los comentarios como JSON String
        db.execSQL(CREATE_DENUNCIAS_TABLE);

        // Crear tabla de notificaciones
        String CREATE_NOTIFICATIONS_TABLE = "CREATE TABLE " + TABLE_NOTIFICATIONS + "("
                + COL_NOTIFICATION_ID + " TEXT PRIMARY KEY,"
                + COL_NOTIFICATION_USER_ID + " TEXT," // ID del usuario al que va la notificación
                + COL_NOTIFICATION_TYPE + " TEXT," // Ej. "new_report", "comment_on_report"
                + COL_NOTIFICATION_TITLE + " TEXT," // Título de la notificación
                + COL_NOTIFICATION_MESSAGE + " TEXT,"
                + COL_NOTIFICATION_TIMESTAMP + " INTEGER," // Marca de tiempo de creación
                + COL_NOTIFICATION_READ + " INTEGER," // 0 para no leído, 1 para leído
                + COL_NOTIFICATION_RELATED_ID + " TEXT" + ")"; // ID de la denuncia o comentario relacionado
        db.execSQL(CREATE_NOTIFICATIONS_TABLE);

        // NUEVO: Tabla para CHAT_TOPICS
        String CREATE_CHAT_TOPICS_TABLE = "CREATE TABLE " + TABLE_CHAT_TOPICS + "("
                + COL_CHAT_TOPIC_ID + " TEXT PRIMARY KEY,"
                + COL_CHAT_TOPIC_NAME + " TEXT,"
                + COL_CHAT_TOPIC_DESCRIPTION + " TEXT,"
                + COL_CHAT_TOPIC_LAST_MESSAGE + " TEXT,"
                + COL_CHAT_TOPIC_LAST_MESSAGE_TIMESTAMP + " INTEGER,"
                + COL_CHAT_TOPIC_UNREAD_COUNT + " INTEGER DEFAULT 0" + ")";
        db.execSQL(CREATE_CHAT_TOPICS_TABLE);

        // NUEVO: Tabla para MESSAGES
        String CREATE_MESSAGES_TABLE = "CREATE TABLE " + TABLE_MESSAGES + "("
                + COL_MESSAGE_ID + " TEXT PRIMARY KEY,"
                + COL_MESSAGE_TOPIC_ID + " TEXT,"
                + COL_MESSAGE_SENDER_ID + " TEXT,"
                + COL_MESSAGE_SENDER_NAME + " TEXT," // Columna para el nombre del remitente
                + COL_MESSAGE_TEXT + " TEXT,"
                + COL_MESSAGE_TIMESTAMP + " INTEGER,"
                + "FOREIGN KEY(" + COL_MESSAGE_TOPIC_ID + ") REFERENCES " + TABLE_CHAT_TOPICS + "(" + COL_CHAT_TOPIC_ID + "),"
                + "FOREIGN KEY(" + COL_MESSAGE_SENDER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ")" + ")";
        db.execSQL(CREATE_MESSAGES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Implementa la lógica de migración aquí.
        // Para desarrollo, a menudo se borran y se recrean las tablas.
        // En producción, se usarían sentencias ALTER TABLE para preservar los datos.
        // Solo ejecuta las migraciones si la oldVersion es menor que la newVersion necesaria
        if (oldVersion < 19) { // Si la versión es anterior a la 19, recrear todo
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_TOPICS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DENUNCIAS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            onCreate(db); // Vuelve a crear todas las tablas
        }
        // Si hay futuras actualizaciones de esquema, añadir if (oldVersion < 20) { ... }
    }

    // --- Métodos CRUD para USUARIOS ---

    // Método para agregar un nuevo usuario
    public boolean addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_ID, user.getUserId());
        values.put(COL_EMAIL, user.getEmail());
        values.put(COL_PASSWORD, user.getPassword());
        values.put(COL_USERNAME, user.getUsername());
        values.put(COL_FULL_NAME, user.getFullName());
        values.put(COL_PHONE, user.getPhone());
        values.put(COL_ADDRESS, user.getAddress());
        values.put(COL_PROFILE_IMAGE_URL, user.getProfileImageUrl());
        values.put(COL_REPORTS_COUNT, user.getReportsCount());
        values.put(COL_SHOW_FULL_NAME_PUBLIC, user.isShowFullNamePublic() ? 1 : 0);
        values.put(COL_SHOW_PROFILE_PHOTO_IN_COMMENTS, user.isShowProfilePhotoInComments() ? 1 : 0);
        values.put(COL_SHOW_EMAIL_PUBLIC, user.isShowEmailPublic() ? 1 : 0);
        values.put(COL_SHOW_PHONE_PUBLIC, user.isShowPhonePublic() ? 1 : 0);

        long result = -1;
        try {
            result = db.insert(TABLE_USERS, null, values);
            if (result != -1) {
                Log.d("DBLocal", "Usuario insertado exitosamente: " + user.getEmail());
            } else {
                Log.e("DBLocal", "Error al insertar usuario: " + user.getEmail());
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Excepción al insertar usuario: " + e.getMessage());
        } finally {
            db.close();
        }
        return result != -1;
    }

    // Método para validar credenciales del usuario
    @SuppressLint("Range")
    public String validateUserCredentials(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String userId = null;
        try {
            // Se busca por EMAIL y PASSWORD
            cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID},
                    COL_EMAIL + " = ? AND " + COL_PASSWORD + " = ?",
                    new String[]{email, password}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                userId = cursor.getString(cursor.getColumnIndex(COL_USER_ID));
                Log.d("DBLocal", "Credenciales válidas para el usuario ID: " + userId + " (Email: " + email + ")");
            } else {
                Log.d("DBLocal", "Credenciales incorrectas o usuario no registrado para el email: " + email);
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Error al validar credenciales para el email: " + email + ": " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return userId;
    }

    // Método para obtener el perfil del usuario por ID
    @SuppressLint("Range")
    public User obtenerUsuarioPorId(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        User user = null;
        try {
            cursor = db.query(TABLE_USERS, null, COL_USER_ID + " = ?",
                    new String[]{userId}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                String id = cursor.getString(cursor.getColumnIndex(COL_USER_ID));
                String email = cursor.getString(cursor.getColumnIndex(COL_EMAIL));
                String password = cursor.getString(cursor.getColumnIndex(COL_PASSWORD));
                String username = cursor.getString(cursor.getColumnIndex(COL_USERNAME));
                String fullName = cursor.getString(cursor.getColumnIndex(COL_FULL_NAME));
                String phone = cursor.getString(cursor.getColumnIndex(COL_PHONE));
                String address = cursor.getString(cursor.getColumnIndex(COL_ADDRESS));
                String profileImageUrl = cursor.getString(cursor.getColumnIndex(COL_PROFILE_IMAGE_URL));
                int reportsCount = cursor.getInt(cursor.getColumnIndex(COL_REPORTS_COUNT));
                boolean showFullNamePublic = cursor.getInt(cursor.getColumnIndex(COL_SHOW_FULL_NAME_PUBLIC)) == 1;
                boolean showProfilePhotoInComments = cursor.getInt(cursor.getColumnIndex(COL_SHOW_PROFILE_PHOTO_IN_COMMENTS)) == 1;
                boolean showEmailPublic = cursor.getInt(cursor.getColumnIndex(COL_SHOW_EMAIL_PUBLIC)) == 1;
                boolean showPhonePublic = cursor.getInt(cursor.getColumnIndex(COL_SHOW_PHONE_PUBLIC)) == 1;

                user = new User(id, email, password, username, fullName,
                        phone, address, profileImageUrl, reportsCount, showFullNamePublic, showProfilePhotoInComments, showEmailPublic, showPhonePublic);
                Log.d("DBLocal", "Usuario obtenido exitosamente por ID: " + userId);
            } else {
                Log.d("DBLocal", "Usuario no encontrado para ID: " + userId);
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Error al obtener usuario por ID: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return user;
    }

    @SuppressLint("Range")
    public User getUserProfile(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        User user = null;
        try {
            // Ahora la consulta se hace directamente por COL_USER_ID
            cursor = db.query(TABLE_USERS, null, COL_USER_ID + " = ?",
                    new String[]{userId}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                // Se extraen todos los datos del cursor para crear el objeto User
                String id = cursor.getString(cursor.getColumnIndex(COL_USER_ID));
                String email = cursor.getString(cursor.getColumnIndex(COL_EMAIL));
                String password = cursor.getString(cursor.getColumnIndex(COL_PASSWORD));
                String username = cursor.getString(cursor.getColumnIndex(COL_USERNAME));
                String fullName = cursor.getString(cursor.getColumnIndex(COL_FULL_NAME));
                String phone = cursor.getString(cursor.getColumnIndex(COL_PHONE));
                String address = cursor.getString(cursor.getColumnIndex(COL_ADDRESS));
                String profileImageUrl = cursor.getString(cursor.getColumnIndex(COL_PROFILE_IMAGE_URL));
                int reportsCount = cursor.getInt(cursor.getColumnIndex(COL_REPORTS_COUNT));

                // Leer los booleanos de visibilidad (0 o 1)
                boolean showFullNamePublic = cursor.getInt(cursor.getColumnIndex(COL_SHOW_FULL_NAME_PUBLIC)) == 1;
                boolean showProfilePhotoInComments = cursor.getInt(cursor.getColumnIndex(COL_SHOW_PROFILE_PHOTO_IN_COMMENTS)) == 1;
                boolean showEmailPublic = cursor.getInt(cursor.getColumnIndex(COL_SHOW_EMAIL_PUBLIC)) == 1;
                boolean showPhonePublic = cursor.getInt(cursor.getColumnIndex(COL_SHOW_PHONE_PUBLIC)) == 1;

                user = new User(id, username, email, password, fullName, phone, address, profileImageUrl, reportsCount,
                        showFullNamePublic, showProfilePhotoInComments, showEmailPublic, showPhonePublic);

                Log.d("DBLocal", "Perfil de usuario cargado para ID: " + userId + " (Email: " + email + ")");
            } else {
                Log.d("DBLocal", "No se encontró perfil para el usuario ID: " + userId);
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Error al obtener perfil de usuario por ID " + userId + ": " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return user;
    }

    // Método para actualizar un usuario
    public boolean updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_EMAIL, user.getEmail());
        values.put(COL_PASSWORD, user.getPassword());
        values.put(COL_USERNAME, user.getUsername());
        values.put(COL_FULL_NAME, user.getFullName());
        values.put(COL_PHONE, user.getPhone());
        values.put(COL_ADDRESS, user.getAddress());
        values.put(COL_PROFILE_IMAGE_URL, user.getProfileImageUrl());
        values.put(COL_REPORTS_COUNT, user.getReportsCount());
        values.put(COL_SHOW_FULL_NAME_PUBLIC, user.isShowFullNamePublic() ? 1 : 0);
        values.put(COL_SHOW_PROFILE_PHOTO_IN_COMMENTS, user.isShowProfilePhotoInComments() ? 1 : 0);
        values.put(COL_SHOW_EMAIL_PUBLIC, user.isShowEmailPublic() ? 1 : 0);
        values.put(COL_SHOW_PHONE_PUBLIC, user.isShowPhonePublic() ? 1 : 0);

        int rowsAffected = 0;
        try {
            rowsAffected = db.update(TABLE_USERS, values, COL_USER_ID + " = ?",
                    new String[]{user.getUserId()});
            if (rowsAffected > 0) {
                Log.d("DBLocal", "Usuario actualizado exitosamente: " + user.getUserId());
            } else {
                Log.e("DBLocal", "No se encontró usuario para actualizar con ID: " + user.getUserId());
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Error al actualizar usuario: " + e.getMessage());
        } finally {
            db.close();
        }
        return rowsAffected > 0;
    }

    // NUEVO: Método para verificar si un usuario existe por email
    public boolean checkUserExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;
        try {
            cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID}, COL_EMAIL + " = ?",
                    new String[]{email}, null, null, null);
            exists = (cursor != null && cursor.getCount() > 0);
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

    // NUEVO: Método para eliminar un usuario por ID
    public boolean deleteUser(String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = 0;
        try {
            // También deberías considerar eliminar las denuncias, notificaciones, chats y mensajes asociados a este usuario
            // Esto es un diseño de base de datos más robusto, pero para simplificar, solo se elimina el usuario.
            // Para una eliminación completa, tendrías que hacer:
            // db.delete(TABLE_DENUNCIAS, COL_DENUNCIA_USUARIO_ID + " = ?", new String[]{userId});
            // db.delete(TABLE_NOTIFICATIONS, COL_NOTIFICATION_USER_ID + " = ?", new String[]{userId});
            // etc.

            rowsAffected = db.delete(TABLE_USERS, COL_USER_ID + " = ?", new String[]{userId});
            if (rowsAffected > 0) {
                Log.d("DBLocal", "Usuario eliminado exitosamente con ID: " + userId);
            } else {
                Log.e("DBLocal", "No se encontró usuario para eliminar con ID: " + userId);
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Error al eliminar usuario con ID: " + userId + ": " + e.getMessage());
        } finally {
            db.close();
        }
        return rowsAffected > 0;
    }

    // NUEVO: Método para obtener el ID del usuario logueado (ejemplo, puedes adaptar esto a SharedPreferences)
    // ESTO ES UN EJEMPLO. EN UNA APP REAL, LA SESIÓN DEL USUARIO SE MANEJA CON SharedPreferences O SIMILAR.
    // Esta implementación solo permite un usuario logueado por vez en esta tabla.
    public void setLoggedInUserId(String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Borra cualquier ID de usuario previamente logueado
            db.delete("LoggedInUser", null, null); // "LoggedInUser" sería una tabla auxiliar para esto

            // Inserta el nuevo ID de usuario logueado
            ContentValues values = new ContentValues();
            values.put(COL_LOGGED_IN_USER_ID, userId);
            db.insert("LoggedInUser", null, values); // "LoggedInUser" sería una tabla auxiliar para esto
            db.setTransactionSuccessful();
            Log.d("DBLocal", "Usuario logueado establecido: " + userId);
        } catch (Exception e) {
            Log.e("DBLocal", "Error al establecer usuario logueado: " + e.getMessage());
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    @SuppressLint("Range")
    public String getLoggedInUserId(SettingsActivity settingsActivity) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String userId = null;
        try {
            // Asumiendo que "LoggedInUser" es una tabla con una única fila para el ID
            cursor = db.query("LoggedInUser", new String[]{COL_LOGGED_IN_USER_ID}, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                userId = cursor.getString(cursor.getColumnIndex(COL_LOGGED_IN_USER_ID));
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Error al obtener usuario logueado: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return userId;
    }

    public void clearLoggedInUserId(SettingsActivity settingsActivity) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete("LoggedInUser", null, null);
            Log.d("DBLocal", "Usuario logueado limpiado.");
        } catch (Exception e) {
            Log.e("DBLocal", "Error al limpiar usuario logueado: " + e.getMessage());
        } finally {
            db.close();
        }
    }


    // --- Métodos CRUD para DENUNCIAS ---

    // Método para insertar una nueva denuncia
    public Denuncia insertarDenuncia(Denuncia denuncia) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DENUNCIA_ID, denuncia.getIdDenuncia());
        values.put(COL_DENUNCIA_USUARIO_ID, denuncia.getIdUsuario());
        values.put(COL_DENUNCIA_TITULO, denuncia.getTitulo());
        values.put(COL_DENUNCIA_DESCRIPCION, denuncia.getDescripcion());
        values.put(COL_DENUNCIA_TIPO, denuncia.getTipoDenuncia());
        values.put(COL_DENUNCIA_LATITUD, denuncia.getLatitud());
        values.put(COL_DENUNCIA_LONGITUD, denuncia.getLongitud());
        values.put(COL_DENUNCIA_URL_IMAGEN, denuncia.getUrlImagen());
        values.put(COL_DENUNCIA_FECHA_HORA, denuncia.getFechaHora()); // Guardar como String
        values.put(COL_DENUNCIA_ESTADO, denuncia.getEstado());
        // Serializa la lista de comentarios a JSON String
        values.put(COL_DENUNCIA_COMMENTS, gson.toJson(denuncia.getComments())); // ¡USA GSON!

        long result = -1;
        try {
            result = db.insert(TABLE_DENUNCIAS, null, values);
            if (result != -1) {
                Log.d("DBLocal", "Denuncia insertada exitosamente: " + denuncia.getIdDenuncia());
                return denuncia;
            } else {
                Log.e("DBLocal", "Error al insertar denuncia: " + denuncia.getIdDenuncia());
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Excepción al insertar denuncia: " + e.getMessage());
        } finally {
            db.close();
        }
        return null;
    }

    // Método para obtener una denuncia por ID
    @SuppressLint("Range")
    public Denuncia obtenerDenunciaPorId(String denunciaId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Denuncia denuncia = null;
        try {
            cursor = db.query(TABLE_DENUNCIAS, null, COL_DENUNCIA_ID + " = ?",
                    new String[]{denunciaId}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                String id = cursor.getString(cursor.getColumnIndex(COL_DENUNCIA_ID));
                String idUsuario = cursor.getString(cursor.getColumnIndex(COL_DENUNCIA_USUARIO_ID));
                String titulo = cursor.getString(cursor.getColumnIndex(COL_DENUNCIA_TITULO));
                String descripcion = cursor.getString(cursor.getColumnIndex(COL_DENUNCIA_DESCRIPCION));
                String tipoDenuncia = cursor.getString(cursor.getColumnIndex(COL_DENUNCIA_TIPO));
                double latitud = cursor.getDouble(cursor.getColumnIndex(COL_DENUNCIA_LATITUD));
                double longitud = cursor.getDouble(cursor.getColumnIndex(COL_DENUNCIA_LONGITUD));
                String urlImagen = cursor.getString(cursor.getColumnIndex(COL_DENUNCIA_URL_IMAGEN));
                String fechaHora = cursor.getString(cursor.getColumnIndex(COL_DENUNCIA_FECHA_HORA)); // Leer como String
                String estado = cursor.getString(cursor.getColumnIndex(COL_DENUNCIA_ESTADO));
                String commentsJson = cursor.getString(cursor.getColumnIndex(COL_DENUNCIA_COMMENTS));

                denuncia = new Denuncia(id, idUsuario, titulo, descripcion, tipoDenuncia,
                        latitud, longitud, urlImagen, fechaHora, estado);
                // Deserializa la cadena JSON de comentarios a List<Map<String, Object>>
                if (commentsJson != null && !commentsJson.isEmpty()) {
                    try {
                        List<Map<String, Object>> comments = gson.fromJson(commentsJson, COMMENTS_TYPE);
                        denuncia.setComments(comments); // Asigna la lista deserializada
                    } catch (Exception e) {
                        Log.e("DBLocal", "Error al deserializar comentarios JSON para denuncia " + id + ": " + e.getMessage());
                        denuncia.setComments(new ArrayList<>()); // Asigna una lista vacía en caso de error
                    }
                } else {
                    denuncia.setComments(new ArrayList<>()); // Asegura que no sea null si la columna está vacía
                }
                Log.d("DBLocal", "Denuncia obtenida exitosamente por ID: " + denunciaId);
            } else {
                Log.d("DBLocal", "Denuncia no encontrada para ID: " + denunciaId);
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

    // Método para obtener todas las denuncias
    @SuppressLint("Range")
    public List<Denuncia> obtenerTodasLasDenuncias() {
        List<Denuncia> denunciaList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_DENUNCIAS, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(COL_DENUNCIA_ID));
                    String idUsuario = cursor.getString(cursor.getColumnIndex(COL_DENUNCIA_USUARIO_ID));
                    String titulo = cursor.getString(cursor.getColumnIndex(COL_DENUNCIA_TITULO));
                    String descripcion = cursor.getString(cursor.getColumnIndex(COL_DENUNCIA_DESCRIPCION));
                    String tipoDenuncia = cursor.getString(cursor.getColumnIndex(COL_DENUNCIA_TIPO));
                    double latitud = cursor.getDouble(cursor.getColumnIndex(COL_DENUNCIA_LATITUD));
                    double longitud = cursor.getDouble(cursor.getColumnIndex(COL_DENUNCIA_LONGITUD));
                    String urlImagen = cursor.getString(cursor.getColumnIndex(COL_DENUNCIA_URL_IMAGEN));
                    String fechaHora = cursor.getString(cursor.getColumnIndex(COL_DENUNCIA_FECHA_HORA));
                    String estado = cursor.getString(cursor.getColumnIndex(COL_DENUNCIA_ESTADO));
                    String commentsJson = cursor.getString(cursor.getColumnIndex(COL_DENUNCIA_COMMENTS));

                    Denuncia denuncia = new Denuncia(id, idUsuario, titulo, descripcion, tipoDenuncia,
                            latitud, longitud, urlImagen, fechaHora, estado);
                    // Deserializa la cadena JSON de comentarios a List<Map<String, Object>>
                    if (commentsJson != null && !commentsJson.isEmpty()) {
                        try {
                            List<Map<String, Object>> comments = gson.fromJson(commentsJson, COMMENTS_TYPE);
                            denuncia.setComments(comments); // Asigna la lista deserializada
                        } catch (Exception e) {
                            Log.e("DBLocal", "Error al deserializar comentarios JSON para denuncia " + id + ": " + e.getMessage());
                            denuncia.setComments(new ArrayList<>());
                        }
                    } else {
                        denuncia.setComments(new ArrayList<>());
                    }
                    denunciaList.add(denuncia);
                } while (cursor.moveToNext());
            }
            Log.d("DBLocal", "Se obtuvieron " + denunciaList.size() + " denuncias.");
        } catch (Exception e) {
            Log.e("DBLocal", "Error al obtener todas las denuncias: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return denunciaList;
    }

    /**
     * Elimina una denuncia de la base de datos por su ID.
     * @param denunciaId El ID de la denuncia a eliminar.
     * @return true si la denuncia fue eliminada exitosamente, false en caso contrario.
     */
    public boolean eliminarDenuncia(String denunciaId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = 0;
        try {
            rowsAffected = db.delete(TABLE_DENUNCIAS, COL_DENUNCIA_ID + " = ?",
                    new String[]{denunciaId});
            if (rowsAffected > 0) {
                Log.d("DBLocal", "Denuncia eliminada exitosamente con ID: " + denunciaId);
            } else {
                Log.e("DBLocal", "No se encontró denuncia con ID para eliminar: " + denunciaId);
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Error al eliminar denuncia con ID: " + denunciaId + ": " + e.getMessage());
        } finally {
            db.close();
        }
        return rowsAffected > 0;
    }

    /**
     * Actualiza una denuncia existente en la base de datos.
     * @param denuncia El objeto Denuncia con los datos actualizados.
     * @return El número de filas afectadas (1 si se actualizó, 0 si no se encontró).
     */
    public int actualizarDenuncia(Denuncia denuncia) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DENUNCIA_USUARIO_ID, denuncia.getIdUsuario());
        values.put(COL_DENUNCIA_TITULO, denuncia.getTitulo());
        values.put(COL_DENUNCIA_DESCRIPCION, denuncia.getDescripcion());
        values.put(COL_DENUNCIA_TIPO, denuncia.getTipoDenuncia());
        values.put(COL_DENUNCIA_LATITUD, denuncia.getLatitud());
        values.put(COL_DENUNCIA_LONGITUD, denuncia.getLongitud());
        values.put(COL_DENUNCIA_URL_IMAGEN, denuncia.getUrlImagen());
        values.put(COL_DENUNCIA_FECHA_HORA, denuncia.getFechaHora());
        values.put(COL_DENUNCIA_ESTADO, denuncia.getEstado());
        // Serializa la lista de comentarios a JSON String
        values.put(COL_DENUNCIA_COMMENTS, gson.toJson(denuncia.getComments())); // ¡USA GSON!

        int rowsAffected = 0;
        try {
            rowsAffected = db.update(TABLE_DENUNCIAS, values, COL_DENUNCIA_ID + " = ?",
                    new String[]{denuncia.getIdDenuncia()});
            if (rowsAffected > 0) {
                Log.d("DBLocal", "Denuncia actualizada exitosamente: " + denuncia.getIdDenuncia());
            } else {
                Log.e("DBLocal", "No se encontró denuncia para actualizar con ID: " + denuncia.getIdDenuncia());
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Error al actualizar denuncia: " + e.getMessage());
        } finally {
            db.close();
        }
        return rowsAffected;
    }

    // NUEVO: Método para agregar un comentario a una denuncia existente
    public boolean agregarComentarioADenuncia(String denunciaId, Map<String, Object> newComment) {
        SQLiteDatabase db = this.getWritableDatabase();
        Denuncia denuncia = obtenerDenunciaPorId(denunciaId); // Obtener la denuncia existente
        if (denuncia == null) {
            Log.e("DBLocal", "No se encontró la denuncia para agregar comentario: " + denunciaId);
            return false;
        }

        List<Map<String, Object>> comments = denuncia.getComments();
        if (comments == null) {
            comments = new ArrayList<>();
        }
        comments.add(newComment); // Añadir el nuevo comentario

        // Serializar la lista actualizada de comentarios a JSON
        ContentValues values = new ContentValues();
        values.put(COL_DENUNCIA_COMMENTS, gson.toJson(comments));

        int rowsAffected = 0;
        try {
            rowsAffected = db.update(TABLE_DENUNCIAS, values, COL_DENUNCIA_ID + " = ?",
                    new String[]{denunciaId});
            if (rowsAffected > 0) {
                Log.d("DBLocal", "Comentario agregado a la denuncia " + denunciaId + " exitosamente.");
            } else {
                Log.e("DBLocal", "No se pudo actualizar la denuncia para agregar comentario: " + denunciaId);
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Error al agregar comentario a la denuncia " + denunciaId + ": " + e.getMessage());
        } finally {
            db.close();
        }
        return rowsAffected > 0;
    }

    public boolean addNotification(Notification notification) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NOTIFICATION_ID, notification.getId());
        values.put(COL_NOTIFICATION_USER_ID, notification.getUserId());
        values.put(COL_NOTIFICATION_TYPE, notification.getType());
        values.put(COL_NOTIFICATION_TITLE, notification.getTitle());
        values.put(COL_NOTIFICATION_MESSAGE, notification.getMessage());
        values.put(COL_NOTIFICATION_TIMESTAMP, notification.getTimestamp());
        values.put(COL_NOTIFICATION_READ, notification.isRead() ? 1 : 0);
        values.put(COL_NOTIFICATION_RELATED_ID, notification.getRelatedId());

        long result = -1;
        try {
            result = db.insert(TABLE_NOTIFICATIONS, null, values);
            if (result != -1) {
                Log.d("DBLocal", "Notificación insertada exitosamente: " + notification.getId());
            } else {
                Log.e("DBLocal", "Error al insertar notificación: " + notification.getId());
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Excepción al insertar notificación: " + e.getMessage());
        } finally {
            db.close();
        }
        return result != -1;
    }

    // Método para obtener notificaciones por ID de usuario
    @SuppressLint("Range")
    public List<Notification> getAllNotificationsForUser(String userId) {
        List<Notification> notificationList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_NOTIFICATIONS, null, COL_NOTIFICATION_USER_ID + " = ?",
                    new String[]{userId}, null, null, COL_NOTIFICATION_TIMESTAMP + " DESC"); // Ordenar por fecha descendente

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(COL_NOTIFICATION_ID));
                    String uId = cursor.getString(cursor.getColumnIndex(COL_NOTIFICATION_USER_ID));
                    String type = cursor.getString(cursor.getColumnIndex(COL_NOTIFICATION_TYPE));
                    String title = cursor.getString(cursor.getColumnIndex(COL_NOTIFICATION_TITLE));
                    String message = cursor.getString(cursor.getColumnIndex(COL_NOTIFICATION_MESSAGE));
                    long timestamp = cursor.getLong(cursor.getColumnIndex(COL_NOTIFICATION_TIMESTAMP));
                    boolean read = cursor.getInt(cursor.getColumnIndex(COL_NOTIFICATION_READ)) == 1;
                    String relatedId = cursor.getString(cursor.getColumnIndex(COL_NOTIFICATION_RELATED_ID));

                    Notification notification = new Notification(id, uId, type, title, message, timestamp, read, relatedId);
                    notificationList.add(notification);
                } while (cursor.moveToNext());
            }
            Log.d("DBLocal", "Se obtuvieron " + notificationList.size() + " notificaciones para el usuario: " + userId);
        } catch (Exception e) {
            Log.e("DBLocal", "Error al obtener notificaciones para el usuario " + userId + ": " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return notificationList;
    }

    // Método para actualizar el estado de lectura de una notificación (SÍNCRONO)
    public boolean updateNotificationReadStatus(String notificationId, boolean readStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NOTIFICATION_READ, readStatus ? 1 : 0);

        int rowsAffected = 0;
        try {
            rowsAffected = db.update(TABLE_NOTIFICATIONS, values, COL_NOTIFICATION_ID + " = ?",
                    new String[]{notificationId});
            if (rowsAffected > 0) {
                Log.d("DBLocal", "Estado de lectura de notificación actualizado: " + notificationId + " a " + readStatus);
            } else {
                Log.e("DBLocal", "Notificación no encontrada para actualizar estado de lectura: " + notificationId);
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Error al actualizar estado de lectura de notificación: " + e.getMessage());
        } finally {
            db.close();
        }
        return rowsAffected > 0;
    }


    // --- Métodos para ChatTopic ---

    /**
     * Inserta un nuevo tema de chat en la base de datos.
     * @param chatTopic El objeto ChatTopic a insertar.
     * @return true si el tema fue insertado exitosamente, false en caso contrario.
     */
    public boolean addChatTopic(ChatTopic chatTopic) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CHAT_TOPIC_ID, chatTopic.getId());
        values.put(COL_CHAT_TOPIC_NAME, chatTopic.getName());
        values.put(COL_CHAT_TOPIC_DESCRIPTION, chatTopic.getDescription());
        values.put(COL_CHAT_TOPIC_LAST_MESSAGE, chatTopic.getLastMessage());
        values.put(COL_CHAT_TOPIC_LAST_MESSAGE_TIMESTAMP, chatTopic.getLastMessageTimestamp());
        values.put(COL_CHAT_TOPIC_UNREAD_COUNT, chatTopic.getUnreadCount());

        long result = -1;
        try {
            result = db.insert(TABLE_CHAT_TOPICS, null, values);
            if (result != -1) {
                Log.d("DBLocal", "ChatTopic insertado exitosamente: " + chatTopic.getId());
            } else {
                Log.e("DBLocal", "Error al insertar ChatTopic: " + chatTopic.getId());
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Excepción al insertar ChatTopic: " + e.getMessage());
        } finally {
            db.close();
        }
        return result != -1;
    }

    /**
     * Obtiene un tema de chat por su ID.
     * @param chatTopicId El ID del tema de chat.
     * @return El objeto ChatTopic si se encuentra, o null si no.
     */
    @SuppressLint("Range")
    public ChatTopic getChatTopicById(String chatTopicId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        ChatTopic chatTopic = null;
        try {
            cursor = db.query(TABLE_CHAT_TOPICS, null, COL_CHAT_TOPIC_ID + " = ?",
                    new String[]{chatTopicId}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                String id = cursor.getString(cursor.getColumnIndex(COL_CHAT_TOPIC_ID));
                String name = cursor.getString(cursor.getColumnIndex(COL_CHAT_TOPIC_NAME));
                String description = cursor.getString(cursor.getColumnIndex(COL_CHAT_TOPIC_DESCRIPTION));
                String lastMessage = cursor.getString(cursor.getColumnIndex(COL_CHAT_TOPIC_LAST_MESSAGE));
                long lastMessageTimestamp = cursor.getLong(cursor.getColumnIndex(COL_CHAT_TOPIC_LAST_MESSAGE_TIMESTAMP));
                int unreadCount = cursor.getInt(cursor.getColumnIndex(COL_CHAT_TOPIC_UNREAD_COUNT));

                chatTopic = new ChatTopic(id, name, description, lastMessage, lastMessageTimestamp, unreadCount);
                Log.d("DBLocal", "ChatTopic obtenido exitosamente: " + id);
            } else {
                Log.d("DBLocal", "ChatTopic no encontrado para ID: " + chatTopicId);
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Error al obtener ChatTopic por ID: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return chatTopic;
    }

    /**
     * Actualiza un tema de chat existente en la base de datos.
     * @param chatTopic El objeto ChatTopic con los datos actualizados.
     * @return true si el tema fue actualizado exitosamente, false en caso contrario.
     */
    public boolean updateChatTopic(ChatTopic chatTopic) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CHAT_TOPIC_NAME, chatTopic.getName());
        values.put(COL_CHAT_TOPIC_DESCRIPTION, chatTopic.getDescription());
        values.put(COL_CHAT_TOPIC_LAST_MESSAGE, chatTopic.getLastMessage());
        values.put(COL_CHAT_TOPIC_LAST_MESSAGE_TIMESTAMP, chatTopic.getLastMessageTimestamp());
        values.put(COL_CHAT_TOPIC_UNREAD_COUNT, chatTopic.getUnreadCount());

        int rowsAffected = 0;
        try {
            rowsAffected = db.update(TABLE_CHAT_TOPICS, values, COL_CHAT_TOPIC_ID + " = ?",
                    new String[]{chatTopic.getId()});
            if (rowsAffected > 0) {
                Log.d("DBLocal", "ChatTopic actualizado exitosamente: " + chatTopic.getId());
            } else {
                Log.e("DBLocal", "No se encontró ChatTopic para actualizar con ID: " + chatTopic.getId());
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Error al actualizar ChatTopic: " + e.getMessage());
        } finally {
            db.close();
        }
        return rowsAffected > 0;
    }

    /**
     * Obtiene todos los temas de chat de la base de datos.
     * @return Una lista de objetos ChatTopic.
     */
    @SuppressLint("Range")
    public List<ChatTopic> getAllChatTopics() {
        List<ChatTopic> chatTopicList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_CHAT_TOPICS + " ORDER BY " + COL_CHAT_TOPIC_LAST_MESSAGE_TIMESTAMP + " DESC", null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(COL_CHAT_TOPIC_ID));
                    String name = cursor.getString(cursor.getColumnIndex(COL_CHAT_TOPIC_NAME));
                    String description = cursor.getString(cursor.getColumnIndex(COL_CHAT_TOPIC_DESCRIPTION));
                    String lastMessage = cursor.getString(cursor.getColumnIndex(COL_CHAT_TOPIC_LAST_MESSAGE));
                    long lastMessageTimestamp = cursor.getLong(cursor.getColumnIndex(COL_CHAT_TOPIC_LAST_MESSAGE_TIMESTAMP));
                    int unreadCount = cursor.getInt(cursor.getColumnIndex(COL_CHAT_TOPIC_UNREAD_COUNT));

                    ChatTopic chatTopic = new ChatTopic(id, name, description, lastMessage, lastMessageTimestamp, unreadCount);
                    chatTopicList.add(chatTopic);
                } while (cursor.moveToNext());
            }
            Log.d("DBLocal", "Se obtuvieron " + chatTopicList.size() + " temas de chat.");
        } catch (Exception e) {
            Log.e("DBLocal", "Error al obtener todos los temas de chat: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return chatTopicList;
    }


    // --- Métodos para Message ---

    /**
     * Inserta un nuevo mensaje de chat en la base de datos.
     * @param message El objeto Message a insertar.
     * @return true si el mensaje fue insertado exitosamente, false en caso contrario.
     */
    public boolean addChatMessage(Message message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MESSAGE_ID, message.getMessageId());
        values.put(COL_MESSAGE_TOPIC_ID, message.getChatTopicId());
        values.put(COL_MESSAGE_SENDER_ID, message.getSenderId());
        values.put(COL_MESSAGE_SENDER_NAME, message.getSenderName()); // Usar el nuevo campo senderName
        values.put(COL_MESSAGE_TEXT, message.getText()); // Usar el nuevo campo text
        values.put(COL_MESSAGE_TIMESTAMP, message.getTimestamp());

        long result = -1;
        try {
            result = db.insert(TABLE_MESSAGES, null, values);
            if (result != -1) {
                Log.d("DBLocal", "Mensaje insertado exitosamente: " + message.getMessageId());
            } else {
                Log.e("DBLocal", "Error al insertar mensaje: " + message.getMessageId());
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Excepción al insertar mensaje: " + e.getMessage());
        } finally {
            db.close();
        }
        return result != -1;
    }

    @SuppressLint("Range")
    public List<Message> getChatMessagesForTopic(String chatTopicId) {
        List<Message> messageList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_MESSAGES, null, COL_MESSAGE_TOPIC_ID + " = ?",
                    new String[]{chatTopicId}, null, null, COL_MESSAGE_TIMESTAMP + " ASC"); // Ordenar por fecha ascendente

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String messageId = cursor.getString(cursor.getColumnIndex(COL_MESSAGE_ID));
                    String topicId = cursor.getString(cursor.getColumnIndex(COL_MESSAGE_TOPIC_ID));
                    String senderId = cursor.getString(cursor.getColumnIndex(COL_MESSAGE_SENDER_ID));
                    String senderName = cursor.getString(cursor.getColumnIndex(COL_MESSAGE_SENDER_NAME)); // Leer senderName
                    String text = cursor.getString(cursor.getColumnIndex(COL_MESSAGE_TEXT)); // Leer text
                    long timestamp = cursor.getLong(cursor.getColumnIndex(COL_MESSAGE_TIMESTAMP));

                    // El constructor de Message fue actualizado para incluir senderName y text
                    Message message = new Message(messageId, topicId, senderId, senderName, text, timestamp);
                    messageList.add(message);
                } while (cursor.moveToNext());
            }
            Log.d("DBLocal", "Se obtuvieron " + messageList.size() + " mensajes para el tema: " + chatTopicId);
        } catch (Exception e) {
            Log.e("DBLocal", "Error al obtener mensajes para el tema " + chatTopicId + ": " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return messageList;
    }


    // --- Callbacks Asíncronos ---
    public interface VoidCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface UserCallback {
        void onSuccess(User user);
        void onFailure(Exception e);
    }

    public void addDenunciaAsync(Denuncia denuncia, VoidCallback callback) {
        new Thread(() -> {
            Denuncia insertedDenuncia = insertarDenuncia(denuncia); // Llama al método síncrono
            if (insertedDenuncia != null) {
                callback.onSuccess();
            } else {
                callback.onFailure(new Exception("Error al insertar denuncia."));
            }
        }).start();
    }

    // Asegúrate de que esta versión asíncrona llame a la versión síncrona
    public void updateNotificationReadStatusAsync(String notificationId, boolean readStatus, VoidCallback callback) {
        new Thread(() -> {
            // Llama al método síncrono `updateNotificationReadStatus`
            boolean success = updateNotificationReadStatus(notificationId, readStatus);
            if (success) {
                callback.onSuccess();
            } else {
                callback.onFailure(new Exception("Notificación no encontrada o no se pudo actualizar."));
            }
        }).start();
    }
    public User getUserById(String userId) {
        return obtenerUsuarioPorId(userId);
    }

    public String loginUser(String email, String password) {
        return validateUserCredentials(email, password);
    }

    @SuppressLint("Range")
    public List<Denuncia> obtenerDenunciasPorUsuario(String userId) {
        List<Denuncia> denunciaList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_DENUNCIAS, null, COL_DENUNCIA_USUARIO_ID + " = ?",
                    new String[]{userId}, null, null, COL_DENUNCIA_FECHA_HORA + " DESC"); // Ordena por fecha descendente

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(COL_DENUNCIA_ID));
                    String idUsuario = cursor.getString(cursor.getColumnIndex(COL_DENUNCIA_USUARIO_ID));
                    String titulo = cursor.getString(cursor.getColumnIndex(COL_DENUNCIA_TITULO));
                    String descripcion = cursor.getString(cursor.getColumnIndex(COL_DENUNCIA_DESCRIPCION));
                    String tipoDenuncia = cursor.getString(cursor.getColumnIndex(COL_DENUNCIA_TIPO));
                    double latitud = cursor.getDouble(cursor.getColumnIndex(COL_DENUNCIA_LATITUD));
                    double longitud = cursor.getDouble(cursor.getColumnIndex(COL_DENUNCIA_LONGITUD));
                    String urlImagen = cursor.getString(cursor.getColumnIndex(COL_DENUNCIA_URL_IMAGEN));
                    String fechaHora = cursor.getString(cursor.getColumnIndex(COL_DENUNCIA_FECHA_HORA));
                    String estado = cursor.getString(cursor.getColumnIndex(COL_DENUNCIA_ESTADO));
                    String commentsJson = cursor.getString(cursor.getColumnIndex(COL_DENUNCIA_COMMENTS));

                    Denuncia denuncia = new Denuncia(id, idUsuario, titulo, descripcion, tipoDenuncia,
                            latitud, longitud, urlImagen, fechaHora, estado);

                    if (commentsJson != null && !commentsJson.isEmpty()) {
                        try {
                            List<Map<String, Object>> comments = gson.fromJson(commentsJson, COMMENTS_TYPE);
                            denuncia.setComments(comments);
                        } catch (Exception e) {
                            Log.e("DBLocal", "Error al deserializar comentarios JSON para denuncia " + id + ": " + e.getMessage());
                            denuncia.setComments(new ArrayList<>());
                        }
                    } else {
                        denuncia.setComments(new ArrayList<>());
                    }
                    denunciaList.add(denuncia);
                } while (cursor.moveToNext());
            }
            Log.d("DBLocal", "Se obtuvieron " + denunciaList.size() + " denuncias para el usuario: " + userId);
        } catch (Exception e) {
            Log.e("DBLocal", "Error al obtener denuncias por usuario " + userId + ": " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return denunciaList;
    }

    /**
     * Obtiene el email de un usuario por su ID.
     * Utilizado por SettingsActivity.
     * @param userId El ID del usuario.
     * @return El email del usuario, o null si no se encuentra.
     */
    @SuppressLint("Range")
    public String getUserEmail(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String email = null;
        try {
            cursor = db.query(TABLE_USERS, new String[]{COL_EMAIL}, COL_USER_ID + " = ?",
                    new String[]{userId}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                email = cursor.getString(cursor.getColumnIndex(COL_EMAIL));
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Error al obtener email para usuario " + userId + ": " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return email;
    }

    /**
     * Obtiene el nombre de usuario (username) de un usuario por su ID.
     * Utilizado por SettingsActivity.
     * @param userId El ID del usuario.
     * @return El nombre de usuario, o null si no se encuentra.
     */
    @SuppressLint("Range")
    public String getUserName(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String username = null;
        try {
            cursor = db.query(TABLE_USERS, new String[]{COL_USERNAME}, COL_USER_ID + " = ?",
                    new String[]{userId}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                username = cursor.getString(cursor.getColumnIndex(COL_USERNAME));
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Error al obtener username para usuario " + userId + ": " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return username;
    }

    /**
     * Obtiene el nombre completo (full_name) de un usuario por su ID.
     * Utilizado por SettingsActivity.
     * @param userId El ID del usuario.
     * @return El nombre completo, o null si no se encuentra.
     */
    @SuppressLint("Range")
    public String getUserFullName(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String fullName = null;
        try {
            cursor = db.query(TABLE_USERS, new String[]{COL_FULL_NAME}, COL_USER_ID + " = ?",
                    new String[]{userId}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                fullName = cursor.getString(cursor.getColumnIndex(COL_FULL_NAME));
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Error al obtener full name para usuario " + userId + ": " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return fullName;
    }

    /**
     * Obtiene el número de teléfono (phone) de un usuario por su ID.
     * Utilizado por SettingsActivity.
     * @param userId El ID del usuario.
     * @return El número de teléfono, o null si no se encuentra.
     */
    @SuppressLint("Range")
    public String getUserPhoneNumber(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String phone = null;
        try {
            cursor = db.query(TABLE_USERS, new String[]{COL_PHONE}, COL_USER_ID + " = ?",
                    new String[]{userId}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                phone = cursor.getString(cursor.getColumnIndex(COL_PHONE));
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Error al obtener teléfono para usuario " + userId + ": " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return phone;
    }


    /**
     * Actualiza la contraseña de un usuario después de verificar la contraseña actual.
     * Utilizado por SettingsActivity.
     * @param userId El ID del usuario.
     * @param currentPassword La contraseña actual del usuario.
     * @param newPassword La nueva contraseña a establecer.
     * @return true si la contraseña fue actualizada exitosamente, false si la contraseña actual es incorrecta o hubo un error.
     */
    public boolean updateUserPassword(String userId, String currentPassword, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        boolean updated = false;
        try {
            // Primero, verifica que la currentPassword sea correcta
            cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID},
                    COL_USER_ID + " = ? AND " + COL_PASSWORD + " = ?",
                    new String[]{userId, currentPassword}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                // Contraseña actual es correcta, proceder a actualizar
                ContentValues values = new ContentValues();
                values.put(COL_PASSWORD, newPassword);

                int rowsAffected = db.update(TABLE_USERS, values, COL_USER_ID + " = ?",
                        new String[]{userId});
                updated = (rowsAffected > 0);
                if (updated) {
                    Log.d("DBLocal", "Contraseña de usuario " + userId + " actualizada exitosamente.");
                } else {
                    Log.e("DBLocal", "Error al actualizar la contraseña para el usuario " + userId + ".");
                }
            } else {
                Log.e("DBLocal", "Contraseña actual incorrecta para el usuario " + userId + ".");
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Error al actualizar la contraseña para el usuario " + userId + ": " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return updated;
    }
    // Dentro de la clase DBLocal, después de tus otros métodos CRUD síncronos
// ...

    // Obtener perfil de usuario (ACTUALIZADO para leer todos los campos, incluyendo visibilidad y teléfono)
    @SuppressLint("Range")

    private boolean columnExists(SQLiteDatabase db, String tableName, String columnName) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name"));
                    if (columnName.equalsIgnoreCase(name)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Error checking column existence: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    public void getUserProfileAsync(String userId, UserCallback callback) {
        new Thread(() -> {
            try {
                User user = getUserProfile(userId); // Llama al método síncrono existente
                if (user != null) {
                    callback.onSuccess(user);
                } else {
                    callback.onFailure(new Exception("Usuario no encontrado o no se pudieron cargar los detalles."));
                }
            } catch (Exception e) {
                callback.onFailure(e);
            }
        }).start();
    }

    public void updateUserPasswordAsync(String userId, String currentPassword, String newPassword, VoidCallback callback) {
        new Thread(() -> {
            try {
                boolean success = updateUserPassword(userId, currentPassword, newPassword); // Llama al método síncrono existente
                if (success) {
                    callback.onSuccess();
                } else {
                    callback.onFailure(new Exception("Error al actualizar la contraseña. Contraseña actual incorrecta o fallo en la base de datos."));
                }
            } catch (Exception e) {
                callback.onFailure(e);
            }
        }).start();
    }

    public void updateUserProfileAsync(User user, VoidCallback callback) {
        new Thread(() -> {
            try {
                boolean success = updateUser(user); // Asumiendo que tienes un método 'updateUser' síncrono que actualiza el perfil
                if (success) {
                    callback.onSuccess();
                } else {
                    callback.onFailure(new Exception("Error al actualizar el perfil del usuario."));
                }
            } catch (Exception e) {
                callback.onFailure(e);
            }
        }).start();
    }

    public void deleteUserAsync(String userId, VoidCallback callback) {
        new Thread(() -> {
            try {
                boolean success = deleteUser(userId); // Llama al método síncrono existente
                if (success) {
                    callback.onSuccess();
                } else {
                    callback.onFailure(new Exception("Error al eliminar el usuario."));
                }
            } catch (Exception e) {
                callback.onFailure(e);
            }
        }).start();
    }
}