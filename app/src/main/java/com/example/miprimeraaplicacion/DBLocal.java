// DBLocal.java
package com.example.miprimeraaplicacion;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DBLocal extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "denuncias_app.db";
    private static final int DATABASE_VERSION = 15; // ¡INCREMENTAR LA VERSIÓN DE LA BASE DE DATOS!

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

    // Nombres de la tabla y columnas para DENUNCIAS
    private static final String TABLE_DENUNCIAS = "denuncias";
    private static final String COL_ID_DENUNCIA = "id_denuncia";
    private static final String COL_ID_USUARIO = "id_usuario";
    private static final String COL_TITULO = "titulo";
    private static final String COL_DESCRIPCION = "descripcion";
    private static final String COL_LATITUD = "latitud";
    private static final String COL_LONGITUD = "longitud";
    private static final String COL_FECHA = "fecha";
    private static final String COL_ESTADO = "estado"; // Nuevo campo: pendiente, en proceso, resuelta
    private static final String COL_TIPO_DENUNCIA = "tipo_denuncia";
    private static final String COL_IMAGEN_URL = "imagen_url";

    // Nombres de la tabla y columnas para CHAT_TOPICS
    private static final String TABLE_CHAT_TOPICS = "chat_topics";
    private static final String COL_CHAT_TOPIC_ID = "topic_id";
    private static final String COL_CHAT_TOPIC_NAME = "topic_name";
    private static final String COL_CHAT_TOPIC_DESCRIPTION = "topic_description";
    private static final String COL_CHAT_LAST_MESSAGE = "last_message";
    private static final String COL_CHAT_LAST_MESSAGE_TIMESTAMP = "last_message_timestamp";
    private static final String COL_CHAT_UNREAD_COUNT = "unread_count";

    // Nombres de la tabla y columnas para CHAT_MESSAGES
    private static final String TABLE_CHAT_MESSAGES = "chat_messages";
    private static final String COL_MESSAGE_ID = "message_id";
    private static final String COL_MESSAGE_TOPIC_ID = "topic_id";
    private static final String COL_MESSAGE_SENDER_ID = "sender_id";
    private static final String COL_MESSAGE_SENDER_NAME = "sender_name";
    private static final String COL_MESSAGE_TEXT = "message_text";
    private static final String COL_MESSAGE_TIMESTAMP = "message_timestamp";

    // Nombres de la tabla y columnas para NOTIFICATIONS
    private static final String TABLE_NOTIFICATIONS = "notifications";
    private static final String COL_NOTIFICATION_ID = "notification_id";
    private static final String COL_NOTIFICATION_USER_ID = "user_id";
    private static final String COL_NOTIFICATION_TITLE = "title";
    private static final String COL_NOTIFICATION_MESSAGE = "message";
    private static final String COL_NOTIFICATION_TIMESTAMP = "timestamp";
    private static final String COL_NOTIFICATION_READ = "read_status";

    private Context context;
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_LOGGED_IN_USER_ID = "loggedInUserId";

    public DBLocal(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabla de usuarios
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
                + COL_SHOW_FULL_NAME_PUBLIC + " INTEGER DEFAULT 0,"
                + COL_SHOW_PROFILE_PHOTO_IN_COMMENTS + " INTEGER DEFAULT 0,"
                + COL_SHOW_EMAIL_PUBLIC + " INTEGER DEFAULT 0,"
                + COL_SHOW_PHONE_PUBLIC + " INTEGER DEFAULT 0"
                + ")";
        db.execSQL(CREATE_USERS_TABLE);
        Log.d("DBLocal", "Tabla USUARIOS creada.");

        // Tabla de denuncias
        String CREATE_DENUNCIAS_TABLE = "CREATE TABLE " + TABLE_DENUNCIAS + "("
                + COL_ID_DENUNCIA + " TEXT PRIMARY KEY,"
                + COL_ID_USUARIO + " TEXT,"
                + COL_TITULO + " TEXT,"
                + COL_DESCRIPCION + " TEXT,"
                + COL_LATITUD + " REAL,"
                + COL_LONGITUD + " REAL,"
                + COL_FECHA + " INTEGER,"
                + COL_ESTADO + " TEXT,"
                + COL_TIPO_DENUNCIA + " TEXT,"
                + COL_IMAGEN_URL + " TEXT"
                + ")";
        db.execSQL(CREATE_DENUNCIAS_TABLE);
        Log.d("DBLocal", "Tabla DENUNCIAS creada.");

        // Tabla de temas de chat
        String CREATE_CHAT_TOPICS_TABLE = "CREATE TABLE " + TABLE_CHAT_TOPICS + "("
                + COL_CHAT_TOPIC_ID + " TEXT PRIMARY KEY,"
                + COL_CHAT_TOPIC_NAME + " TEXT,"
                + COL_CHAT_TOPIC_DESCRIPTION + " TEXT,"
                + COL_CHAT_LAST_MESSAGE + " TEXT,"
                + COL_CHAT_LAST_MESSAGE_TIMESTAMP + " INTEGER,"
                + COL_CHAT_UNREAD_COUNT + " INTEGER"
                + ")";
        db.execSQL(CREATE_CHAT_TOPICS_TABLE);
        Log.d("DBLocal", "Tabla CHAT_TOPICS creada.");

        // Tabla de mensajes de chat
        String CREATE_CHAT_MESSAGES_TABLE = "CREATE TABLE " + TABLE_CHAT_MESSAGES + "("
                + COL_MESSAGE_ID + " TEXT PRIMARY KEY,"
                + COL_MESSAGE_TOPIC_ID + " TEXT,"
                + COL_MESSAGE_SENDER_ID + " TEXT,"
                + COL_MESSAGE_SENDER_NAME + " TEXT,"
                + COL_MESSAGE_TEXT + " TEXT,"
                + COL_MESSAGE_TIMESTAMP + " INTEGER"
                + ")";
        db.execSQL(CREATE_CHAT_MESSAGES_TABLE);
        Log.d("DBLocal", "Tabla CHAT_MESSAGES creada.");

        // Tabla de notificaciones
        String CREATE_NOTIFICATIONS_TABLE = "CREATE TABLE " + TABLE_NOTIFICATIONS + "("
                + COL_NOTIFICATION_ID + " TEXT PRIMARY KEY,"
                + COL_NOTIFICATION_USER_ID + " TEXT,"
                + COL_NOTIFICATION_TITLE + " TEXT,"
                + COL_NOTIFICATION_MESSAGE + " TEXT,"
                + COL_NOTIFICATION_TIMESTAMP + " INTEGER,"
                + COL_NOTIFICATION_READ + " INTEGER" // 0 for false, 1 for true
                + ")";
        db.execSQL(CREATE_NOTIFICATIONS_TABLE);
        Log.d("DBLocal", "Tabla NOTIFICATIONS creada.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DBLocal", "onUpgrade called: oldVersion=" + oldVersion + ", newVersion=" + newVersion);
        // Borrar tablas antiguas si existen y crear nuevas (simplificado para desarrollo)
        if (oldVersion < 11) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DENUNCIAS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            // Recrear tablas si es necesario
            onCreate(db);
        }
        if (oldVersion < 12) {
            // Añadir tabla de notificaciones
            String CREATE_NOTIFICATIONS_TABLE = "CREATE TABLE " + TABLE_NOTIFICATIONS + "("
                    + COL_NOTIFICATION_ID + " TEXT PRIMARY KEY,"
                    + COL_NOTIFICATION_USER_ID + " TEXT,"
                    + COL_NOTIFICATION_TITLE + " TEXT,"
                    + COL_NOTIFICATION_MESSAGE + " TEXT,"
                    + COL_NOTIFICATION_TIMESTAMP + " INTEGER,"
                    + COL_NOTIFICATION_READ + " INTEGER"
                    + ")";
            db.execSQL(CREATE_NOTIFICATIONS_TABLE);
            Log.d("DBLocal", "Tabla NOTIFICATIONS añadida en onUpgrade (v12).");
        }
        if (oldVersion < 13) {
            // Añadir campos relacionados con el perfil en la tabla de usuarios
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_FULL_NAME + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_PHONE + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_ADDRESS + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_PROFILE_IMAGE_URL + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_REPORTS_COUNT + " INTEGER DEFAULT 0");
            Log.d("DBLocal", "Campos de perfil añadidos a la tabla USUARIOS en onUpgrade (v13).");
        }
        if (oldVersion < 14) {
            // Añadir tablas de chat_topics y chat_messages
            String CREATE_CHAT_TOPICS_TABLE = "CREATE TABLE " + TABLE_CHAT_TOPICS + "("
                    + COL_CHAT_TOPIC_ID + " TEXT PRIMARY KEY,"
                    + COL_CHAT_TOPIC_NAME + " TEXT,"
                    + COL_CHAT_TOPIC_DESCRIPTION + " TEXT,"
                    + COL_CHAT_LAST_MESSAGE + " TEXT,"
                    + COL_CHAT_LAST_MESSAGE_TIMESTAMP + " INTEGER,"
                    + COL_CHAT_UNREAD_COUNT + " INTEGER"
                    + ")";
            db.execSQL(CREATE_CHAT_TOPICS_TABLE);
            Log.d("DBLocal", "Tabla CHAT_TOPICS creada en onUpgrade (v14).");

            String CREATE_CHAT_MESSAGES_TABLE = "CREATE TABLE " + TABLE_CHAT_MESSAGES + "("
                    + COL_MESSAGE_ID + " TEXT PRIMARY KEY,"
                    + COL_MESSAGE_TOPIC_ID + " TEXT,"
                    + COL_MESSAGE_SENDER_ID + " TEXT,"
                    + COL_MESSAGE_SENDER_NAME + " TEXT,"
                    + COL_MESSAGE_TEXT + " TEXT,"
                    + COL_MESSAGE_TIMESTAMP + " INTEGER"
                    + ")";
            db.execSQL(CREATE_CHAT_MESSAGES_TABLE);
            Log.d("DBLocal", "Tabla CHAT_MESSAGES creada en onUpgrade (v14).");

            // Añadir campos de privacidad al usuario
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_SHOW_FULL_NAME_PUBLIC + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_SHOW_PROFILE_PHOTO_IN_COMMENTS + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_SHOW_EMAIL_PUBLIC + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_SHOW_PHONE_PUBLIC + " INTEGER DEFAULT 0");
            Log.d("DBLocal", "Campos de privacidad añadidos a la tabla USUARIOS en onUpgrade (v14).");
        }
        if (oldVersion < 15) {
            // No hay cambios de esquema adicionales para la versión 15,
            // pero mantenemos el incremento para forzar onUpgrade si algo falló antes.
            Log.d("DBLocal", "Actualización a la versión 15 completada.");
        }
    }


    // --- Métodos de USUARIO ---
    public void addUser(User newUser, VoidCallback callback) {
        new Thread(() -> {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_USER_ID, newUser.getUserId());
            values.put(COL_EMAIL, newUser.getEmail());
            values.put(COL_PASSWORD, newUser.getPassword());
            values.put(COL_USERNAME, newUser.getUsername());
            values.put(COL_FULL_NAME, newUser.getFullName());
            values.put(COL_PHONE, newUser.getPhone());
            values.put(COL_ADDRESS, newUser.getAddress());
            values.put(COL_PROFILE_IMAGE_URL, newUser.getProfileImageUrl());
            values.put(COL_REPORTS_COUNT, newUser.getReportsCount());
            values.put(COL_SHOW_FULL_NAME_PUBLIC, newUser.isShowFullNamePublic() ? 1 : 0);
            values.put(COL_SHOW_PROFILE_PHOTO_IN_COMMENTS, newUser.isShowProfilePhotoInComments() ? 1 : 0);
            values.put(COL_SHOW_EMAIL_PUBLIC, newUser.isShowEmailPublic() ? 1 : 0);
            values.put(COL_SHOW_PHONE_PUBLIC, newUser.isShowPhonePublic() ? 1 : 0);

            try {
                long result = db.insert(TABLE_USERS, null, values);
                if (result != -1) {
                    Log.d("DBLocal", "Usuario agregado: " + newUser.getUsername());
                    callback.onSuccess();
                } else {
                    Log.e("DBLocal", "Error al agregar usuario: " + newUser.getUsername());
                    callback.onFailure(new Exception("Error al agregar usuario."));
                }
            } catch (Exception e) {
                Log.e("DBLocal", "Excepción al agregar usuario: " + e.getMessage());
                callback.onFailure(e);
            } finally {
                db.close();
            }
        }).start();
    }

    public void loginUser(String email, String password, UserCallback callback) {
        new Thread(() -> {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = null;
            User user = null;
            try {
                String selection = COL_EMAIL + " = ? AND " + COL_PASSWORD + " = ?";
                String[] selectionArgs = {email, password};
                cursor = db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ID));
                    @SuppressLint("Range") String username = cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME));
                    @SuppressLint("Range") String fullName = cursor.getString(cursor.getColumnIndexOrThrow(COL_FULL_NAME));
                    @SuppressLint("Range") String phone = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE));
                    @SuppressLint("Range") String address = cursor.getString(cursor.getColumnIndexOrThrow(COL_ADDRESS));
                    @SuppressLint("Range") String profileImageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROFILE_IMAGE_URL));
                    @SuppressLint("Range") int reportsCount = cursor.getInt(cursor.getColumnIndexOrThrow(COL_REPORTS_COUNT));
                    @SuppressLint("Range") boolean showFullNamePublic = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SHOW_FULL_NAME_PUBLIC)) == 1;
                    @SuppressLint("Range") boolean showProfilePhotoInComments = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SHOW_PROFILE_PHOTO_IN_COMMENTS)) == 1;
                    @SuppressLint("Range") boolean showEmailPublic = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SHOW_EMAIL_PUBLIC)) == 1;
                    @SuppressLint("Range") boolean showPhonePublic = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SHOW_PHONE_PUBLIC)) == 1;

                    user = new User(id, email, password, username, fullName, phone, address, profileImageUrl, reportsCount, showFullNamePublic, showProfilePhotoInComments, showEmailPublic, showPhonePublic);
                    Log.d("DBLocal", "Usuario logeado: " + username);
                    saveLoggedInUserId(id); // Save user ID to SharedPreferences
                    callback.onSuccess(user);
                } else {
                    Log.d("DBLocal", "Credenciales inválidas para email: " + email);
                    callback.onFailure(new Exception("Credenciales inválidas."));
                }
            } catch (Exception e) {
                Log.e("DBLocal", "Error al intentar login: " + e.getMessage());
                callback.onFailure(e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                db.close();
            }
        }).start();
    }

    public void getUserProfile(String userId, UserCallback callback) {
        new Thread(() -> {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = null;
            User user = null;
            try {
                String selection = COL_USER_ID + " = ?";
                String[] selectionArgs = {userId};
                cursor = db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ID));
                    @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL));
                    @SuppressLint("Range") String password = cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD));
                    @SuppressLint("Range") String username = cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME));
                    @SuppressLint("Range") String fullName = cursor.getString(cursor.getColumnIndexOrThrow(COL_FULL_NAME));
                    @SuppressLint("Range") String phone = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE));
                    @SuppressLint("Range") String address = cursor.getString(cursor.getColumnIndexOrThrow(COL_ADDRESS));
                    @SuppressLint("Range") String profileImageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROFILE_IMAGE_URL));
                    @SuppressLint("Range") int reportsCount = cursor.getInt(cursor.getColumnIndexOrThrow(COL_REPORTS_COUNT));
                    @SuppressLint("Range") boolean showFullNamePublic = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SHOW_FULL_NAME_PUBLIC)) == 1;
                    @SuppressLint("Range") boolean showProfilePhotoInComments = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SHOW_PROFILE_PHOTO_IN_COMMENTS)) == 1;
                    @SuppressLint("Range") boolean showEmailPublic = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SHOW_EMAIL_PUBLIC)) == 1;
                    @SuppressLint("Range") boolean showPhonePublic = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SHOW_PHONE_PUBLIC)) == 1;

                    user = new User(id, email, password, username, fullName, phone, address, profileImageUrl, reportsCount, showFullNamePublic, showProfilePhotoInComments, showEmailPublic, showPhonePublic);
                    Log.d("DBLocal", "Perfil de usuario obtenido: " + username);
                    callback.onSuccess(user);
                } else {
                    Log.d("DBLocal", "Usuario no encontrado con ID: " + userId);
                    callback.onFailure(new Exception("Usuario no encontrado."));
                }
            } catch (Exception e) {
                Log.e("DBLocal", "Error al obtener perfil de usuario: " + e.getMessage());
                callback.onFailure(e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                db.close();
            }
        }).start();
    }

    public void updateUser(User user, VoidCallback callback) {
        new Thread(() -> {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_EMAIL, user.getEmail());
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

            String selection = COL_USER_ID + " = ?";
            String[] selectionArgs = {user.getUserId()};
            int rowsAffected = 0;
            try {
                rowsAffected = db.update(TABLE_USERS, values, selection, selectionArgs);
                if (rowsAffected > 0) {
                    Log.d("DBLocal", "Usuario actualizado: " + user.getUsername());
                    callback.onSuccess();
                } else {
                    Log.e("DBLocal", "Error al actualizar usuario o usuario no encontrado: " + user.getUsername());
                    callback.onFailure(new Exception("Usuario no encontrado o no se pudo actualizar."));
                }
            } catch (Exception e) {
                Log.e("DBLocal", "Excepción al actualizar usuario: " + e.getMessage());
                callback.onFailure(e);
            } finally {
                db.close();
            }
        }).start();
    }

    public void updateUserPassword(String userId, String newPassword, VoidCallback callback) {
        new Thread(() -> {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_PASSWORD, newPassword);

            String selection = COL_USER_ID + " = ?";
            String[] selectionArgs = {userId};
            int rowsAffected = 0;
            try {
                rowsAffected = db.update(TABLE_USERS, values, selection, selectionArgs);
                if (rowsAffected > 0) {
                    Log.d("DBLocal", "Contraseña de usuario actualizada para ID: " + userId);
                    callback.onSuccess();
                } else {
                    Log.e("DBLocal", "Error al actualizar contraseña o usuario no encontrado: " + userId);
                    callback.onFailure(new Exception("Contraseña no actualizada o usuario no encontrado."));
                }
            } catch (Exception e) {
                Log.e("DBLocal", "Excepción al actualizar contraseña: " + e.getMessage());
                callback.onFailure(e);
            } finally {
                db.close();
            }
        }).start();
    }


    public void deleteUser(String userId, VoidCallback callback) {
        new Thread(() -> {
            SQLiteDatabase db = this.getWritableDatabase();
            String selection = COL_USER_ID + " = ?";
            String[] selectionArgs = {userId};
            int rowsAffected = 0;
            try {
                rowsAffected = db.delete(TABLE_USERS, selection, selectionArgs);
                if (rowsAffected > 0) {
                    Log.d("DBLocal", "Usuario eliminado: " + userId);
                    callback.onSuccess();
                } else {
                    Log.e("DBLocal", "Error al eliminar usuario o usuario no encontrado: " + userId);
                    callback.onFailure(new Exception("Usuario no encontrado o no se pudo eliminar."));
                }
            } catch (Exception e) {
                Log.e("DBLocal", "Excepción al eliminar usuario: " + e.getMessage());
                callback.onFailure(e);
            } finally {
                db.close();
            }
        }).start();
    }

    // --- Métodos de Sesión (SharedPreferences) ---
    public void saveLoggedInUserId(String userId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_LOGGED_IN_USER_ID, userId);
        editor.apply();
        Log.d("DBLocal", "ID de usuario logeado guardado: " + userId);
    }

    public String getLoggedInUserId() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String userId = prefs.getString(KEY_LOGGED_IN_USER_ID, null);
        Log.d("DBLocal", "ID de usuario logeado obtenido: " + (userId != null ? userId : "null"));
        return userId;
    }

    public void clearLoggedInUserId() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_LOGGED_IN_USER_ID);
        editor.apply();
        Log.d("DBLocal", "ID de usuario logeado limpiado.");
    }

    // Métodos para obtener detalles específicos del usuario (pueden ser útiles)
    public void getUserEmail(String userId, StringCallback callback) {
        getUserProfile(userId, new UserCallback() {
            @Override
            public void onSuccess(User user) {
                callback.onSuccess(user.getEmail());
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void getUserName(String userId, StringCallback callback) {
        getUserProfile(userId, new UserCallback() {
            @Override
            public void onSuccess(User user) {
                callback.onSuccess(user.getUsername());
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void getUserFullName(String userId, StringCallback callback) {
        getUserProfile(userId, new UserCallback() {
            @Override
            public void onSuccess(User user) {
                callback.onSuccess(user.getFullName());
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void getUserPhoneNumber(String userId, StringCallback callback) {
        getUserProfile(userId, new UserCallback() {
            @Override
            public void onSuccess(User user) {
                callback.onSuccess(user.getPhone());
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }


    // --- Métodos de DENUNCIA ---
    public void addDenuncia(Denuncia denuncia, VoidCallback callback) {
        new Thread(() -> {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_ID_DENUNCIA, denuncia.getIdDenuncia());
            values.put(COL_ID_USUARIO, denuncia.getIdUsuario());
            values.put(COL_TITULO, denuncia.getTitulo());
            values.put(COL_DESCRIPCION, denuncia.getDescripcion());
            values.put(COL_LATITUD, denuncia.getLatitud());
            values.put(COL_LONGITUD, denuncia.getLongitud());
            values.put(COL_FECHA, denuncia.getFecha());
            values.put(COL_ESTADO, denuncia.getEstado());
            values.put(COL_TIPO_DENUNCIA, denuncia.getTipoDenuncia());
            values.put(COL_IMAGEN_URL, denuncia.getUrlImagen());

            try {
                long result = db.insert(TABLE_DENUNCIAS, null, values);
                if (result != -1) {
                    Log.d("DBLocal", "Denuncia agregada: " + denuncia.getTitulo());
                    callback.onSuccess();
                } else {
                    Log.e("DBLocal", "Error al agregar denuncia: " + denuncia.getTitulo());
                    callback.onFailure(new Exception("Error al agregar denuncia."));
                }
            } catch (Exception e) {
                Log.e("DBLocal", "Excepción al agregar denuncia: " + e.getMessage());
                callback.onFailure(e);
            } finally {
                db.close();
            }
        }).start();
    }


    public void getDenuncias(ListCallback<Denuncia> callback) {
        new Thread(() -> {
            List<Denuncia> denuncias = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = null;
            try {
                cursor = db.query(TABLE_DENUNCIAS, null, null, null, null, null, COL_FECHA + " DESC");

                while (cursor.moveToNext()) {
                    @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndexOrThrow(COL_ID_DENUNCIA));
                    @SuppressLint("Range") String userId = cursor.getString(cursor.getColumnIndexOrThrow(COL_ID_USUARIO));
                    @SuppressLint("Range") String titulo = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITULO));
                    @SuppressLint("Range") String descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPCION));
                    @SuppressLint("Range") double latitud = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LATITUD));
                    @SuppressLint("Range") double longitud = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LONGITUD));
                    @SuppressLint("Range") long fecha = cursor.getLong(cursor.getColumnIndexOrThrow(COL_FECHA));
                    @SuppressLint("Range") String estado = cursor.getString(cursor.getColumnIndexOrThrow(COL_ESTADO));
                    @SuppressLint("Range") String tipo = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIPO_DENUNCIA));
                    @SuppressLint("Range") String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGEN_URL));

                    denuncias.add(new Denuncia(id, userId, titulo, descripcion, latitud, longitud, fecha, estado, tipo, imageUrl));
                }
                callback.onSuccess(denuncias);
            } catch (Exception e) {
                Log.e("DBLocal", "Error al obtener todas las denuncias: " + e.getMessage());
                callback.onFailure(e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                db.close();
            }
        }).start();
    }

    public void getDenunciasByUserId(String userId, ListCallback<Denuncia> callback) {
        new Thread(() -> {
            List<Denuncia> userDenuncias = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = null;
            try {
                String selection = COL_ID_USUARIO + " = ?";
                String[] selectionArgs = {userId};
                cursor = db.query(TABLE_DENUNCIAS, null, selection, selectionArgs, null, null, COL_FECHA + " DESC");

                while (cursor.moveToNext()) {
                    @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndexOrThrow(COL_ID_DENUNCIA));
                    @SuppressLint("Range") String uId = cursor.getString(cursor.getColumnIndexOrThrow(COL_ID_USUARIO));
                    @SuppressLint("Range") String titulo = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITULO));
                    @SuppressLint("Range") String descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPCION));
                    @SuppressLint("Range") double latitud = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LATITUD));
                    @SuppressLint("Range") double longitud = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LONGITUD));
                    @SuppressLint("Range") long fecha = cursor.getLong(cursor.getColumnIndexOrThrow(COL_FECHA));
                    @SuppressLint("Range") String estado = cursor.getString(cursor.getColumnIndexOrThrow(COL_ESTADO));
                    @SuppressLint("Range") String tipo = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIPO_DENUNCIA));
                    @SuppressLint("Range") String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGEN_URL));

                    userDenuncias.add(new Denuncia(id, uId, titulo, descripcion, latitud, longitud, fecha, estado, tipo, imageUrl));
                }
                callback.onSuccess(userDenuncias);
            } catch (Exception e) {
                Log.e("DBLocal", "Error al obtener denuncias para usuario " + userId + ": " + e.getMessage());
                callback.onFailure(e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                db.close();
            }
        }).start();
    }

    public void updateDenuncia(Denuncia denuncia, VoidCallback callback) {
        new Thread(() -> {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_TITULO, denuncia.getTitulo());
            values.put(COL_DESCRIPCION, denuncia.getDescripcion());
            values.put(COL_LATITUD, denuncia.getLatitud());
            values.put(COL_LONGITUD, denuncia.getLongitud());
            values.put(COL_FECHA, denuncia.getFecha());
            values.put(COL_ESTADO, denuncia.getEstado());
            values.put(COL_TIPO_DENUNCIA, denuncia.getTipoDenuncia());
            values.put(COL_IMAGEN_URL, denuncia.getUrlImagen());

            String selection = COL_ID_DENUNCIA + " = ?";
            String[] selectionArgs = {denuncia.getIdDenuncia()};
            int rowsAffected = 0;
            try {
                rowsAffected = db.update(TABLE_DENUNCIAS, values, selection, selectionArgs);
                if (rowsAffected > 0) {
                    Log.d("DBLocal", "Denuncia actualizada: " + denuncia.getTitulo());
                    callback.onSuccess();
                } else {
                    Log.e("DBLocal", "Error al actualizar denuncia o no encontrada: " + denuncia.getTitulo());
                    callback.onFailure(new Exception("Denuncia no encontrada o no se pudo actualizar."));
                }
            } catch (Exception e) {
                Log.e("DBLocal", "Excepción al actualizar denuncia: " + e.getMessage());
                callback.onFailure(e);
            } finally {
                db.close();
            }
        }).start();
    }

    // --- Métodos de CHAT ---

    public void addChatTopic(ChatTopic topic, VoidCallback callback) {
        new Thread(() -> {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_CHAT_TOPIC_ID, topic.getId());
            values.put(COL_CHAT_TOPIC_NAME, topic.getName());
            values.put(COL_CHAT_TOPIC_DESCRIPTION, topic.getDescription());
            values.put(COL_CHAT_LAST_MESSAGE, topic.getLastMessage());
            values.put(COL_CHAT_LAST_MESSAGE_TIMESTAMP, topic.getLastMessageTimestamp());
            values.put(COL_CHAT_UNREAD_COUNT, topic.getUnreadCount());

            try {
                long result = db.insert(TABLE_CHAT_TOPICS, null, values);
                if (result != -1) {
                    Log.d("DBLocal", "Tema de chat agregado: " + topic.getName());
                    callback.onSuccess();
                } else {
                    Log.e("DBLocal", "Error al agregar tema de chat: " + topic.getName());
                    callback.onFailure(new Exception("Error al agregar tema de chat."));
                }
            } catch (Exception e) {
                Log.e("DBLocal", "Excepción al agregar tema de chat: " + e.getMessage());
                callback.onFailure(e);
            } finally {
                db.close();
            }
        }).start();
    }

    public void getChatTopics(ListCallback<ChatTopic> callback) {
        new Thread(() -> {
            List<ChatTopic> topics = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = null;
            try {
                cursor = db.query(TABLE_CHAT_TOPICS, null, null, null, null, null, COL_CHAT_LAST_MESSAGE_TIMESTAMP + " DESC");

                while (cursor.moveToNext()) {
                    @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndexOrThrow(COL_CHAT_TOPIC_ID));
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_CHAT_TOPIC_NAME));
                    @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndexOrThrow(COL_CHAT_TOPIC_DESCRIPTION));
                    @SuppressLint("Range") String lastMessage = cursor.getString(cursor.getColumnIndexOrThrow(COL_CHAT_LAST_MESSAGE));
                    @SuppressLint("Range") long lastMessageTimestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CHAT_LAST_MESSAGE_TIMESTAMP));
                    @SuppressLint("Range") int unreadCount = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CHAT_UNREAD_COUNT));
                    topics.add(new ChatTopic(id, name, description, lastMessage, lastMessageTimestamp, unreadCount));
                }
                callback.onSuccess(topics);
            } catch (Exception e) {
                Log.e("DBLocal", "Error al obtener temas de chat: " + e.getMessage());
                callback.onFailure(e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                db.close();
            }
        }).start();
    }

    public void updateChatTopic(ChatTopic topic, VoidCallback callback) {
        new Thread(() -> {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_CHAT_TOPIC_NAME, topic.getName());
            values.put(COL_CHAT_TOPIC_DESCRIPTION, topic.getDescription());
            values.put(COL_CHAT_LAST_MESSAGE, topic.getLastMessage());
            values.put(COL_CHAT_LAST_MESSAGE_TIMESTAMP, topic.getLastMessageTimestamp());
            values.put(COL_CHAT_UNREAD_COUNT, topic.getUnreadCount());

            String selection = COL_CHAT_TOPIC_ID + " = ?";
            String[] selectionArgs = {topic.getId()};
            int rowsAffected = 0;
            try {
                rowsAffected = db.update(TABLE_CHAT_TOPICS, values, selection, selectionArgs);
                if (rowsAffected > 0) {
                    Log.d("DBLocal", "Tema de chat actualizado: " + topic.getName());
                    callback.onSuccess();
                } else {
                    Log.e("DBLocal", "Error al actualizar tema de chat o no encontrado: " + topic.getName());
                    callback.onFailure(new Exception("Tema de chat no encontrado o no se pudo actualizar."));
                }
            } catch (Exception e) {
                Log.e("DBLocal", "Excepción al actualizar tema de chat: " + e.getMessage());
                callback.onFailure(e);
            } finally {
                db.close();
            }
        }).start();
    }


    public void addChatMessage(Message message, VoidCallback callback) {
        new Thread(() -> {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_MESSAGE_ID, message.getId());
            values.put(COL_MESSAGE_TOPIC_ID, message.getChatTopicId());
            values.put(COL_MESSAGE_SENDER_ID, message.getSenderId());
            values.put(COL_MESSAGE_SENDER_NAME, message.getSenderName());
            values.put(COL_MESSAGE_TEXT, message.getText());
            values.put(COL_MESSAGE_TIMESTAMP, message.getTimestamp());

            try {
                long result = db.insert(TABLE_CHAT_MESSAGES, null, values);
                if (result != -1) {
                    Log.d("DBLocal", "Mensaje de chat agregado: " + message.getText());
                    callback.onSuccess();
                } else {
                    Log.e("DBLocal", "Error al agregar mensaje de chat: " + message.getText());
                    callback.onFailure(new Exception("Error al agregar mensaje de chat."));
                }
            } catch (Exception e) {
                Log.e("DBLocal", "Excepción al agregar mensaje de chat: " + e.getMessage());
                callback.onFailure(e);
            } finally {
                db.close();
            }
        }).start();
    }

    public void getChatMessagesForTopic(String topicId, ListCallback<Message> callback) {
        new Thread(() -> {
            List<Message> messages = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = null;
            try {
                String selection = COL_MESSAGE_TOPIC_ID + " = ?";
                String[] selectionArgs = {topicId};
                String[] projection = {
                        COL_MESSAGE_ID,
                        COL_MESSAGE_TOPIC_ID,
                        COL_MESSAGE_SENDER_ID,
                        COL_MESSAGE_SENDER_NAME,
                        COL_MESSAGE_TEXT,
                        COL_MESSAGE_TIMESTAMP
                };
                cursor = db.query(
                        TABLE_CHAT_MESSAGES,
                        projection,
                        selection,
                        selectionArgs,
                        null, null,
                        COL_MESSAGE_TIMESTAMP + " ASC" // Ordenar por fecha ascendente
                );

                while (cursor.moveToNext()) {
                    @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndexOrThrow(COL_MESSAGE_ID));
                    @SuppressLint("Range") String chatTopicId = cursor.getString(cursor.getColumnIndexOrThrow(COL_MESSAGE_TOPIC_ID));
                    @SuppressLint("Range") String senderId = cursor.getString(cursor.getColumnIndexOrThrow(COL_MESSAGE_SENDER_ID));
                    @SuppressLint("Range") String senderName = cursor.getString(cursor.getColumnIndexOrThrow(COL_MESSAGE_SENDER_NAME));
                    @SuppressLint("Range") String text = cursor.getString(cursor.getColumnIndexOrThrow(COL_MESSAGE_TEXT));
                    @SuppressLint("Range") long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COL_MESSAGE_TIMESTAMP));

                    messages.add(new Message(id, chatTopicId, senderId, senderName, text, timestamp));
                }
                callback.onSuccess(messages);
            } catch (Exception e) {
                Log.e("DBLocal", "Error al obtener mensajes para tema " + topicId + ": " + e.getMessage());
                callback.onFailure(e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                db.close();
            }
        }).start();
    }

    // --- Métodos de NOTIFICACIONES ---

    public void addNotification(Notification notification, VoidCallback callback) {
        new Thread(() -> {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_NOTIFICATION_ID, notification.getId());
            values.put(COL_NOTIFICATION_USER_ID, notification.getUserId());
            values.put(COL_NOTIFICATION_TITLE, notification.getTitle());
            values.put(COL_NOTIFICATION_MESSAGE, notification.getMessage());
            values.put(COL_NOTIFICATION_TIMESTAMP, notification.getTimestamp());
            values.put(COL_NOTIFICATION_READ, notification.isRead() ? 1 : 0);

            try {
                long result = db.insert(TABLE_NOTIFICATIONS, null, values);
                if (result != -1) {
                    Log.d("DBLocal", "Notificación agregada: " + notification.getTitle());
                    callback.onSuccess();
                } else {
                    Log.e("DBLocal", "Error al agregar notificación: " + notification.getTitle());
                    callback.onFailure(new Exception("Error al agregar notificación."));
                }
            } catch (Exception e) {
                Log.e("DBLocal", "Excepción al agregar notificación: " + e.getMessage());
                callback.onFailure(e);
            } finally {
                db.close();
            }
        }).start();
    }

    public void getNotificationsForUser(String userId, ListCallback<Notification> callback) {
        new Thread(() -> {
            List<Notification> notifications = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = null;
            try {
                String selection = COL_NOTIFICATION_USER_ID + " = ?";
                String[] selectionArgs = {userId};
                cursor = db.query(TABLE_NOTIFICATIONS, null, selection, selectionArgs, null, null, COL_NOTIFICATION_TIMESTAMP + " DESC");

                while (cursor.moveToNext()) {
                    @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTIFICATION_ID));
                    @SuppressLint("Range") String uId = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTIFICATION_USER_ID));
                    @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTIFICATION_TITLE));
                    @SuppressLint("Range") String message = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTIFICATION_MESSAGE));
                    @SuppressLint("Range") long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COL_NOTIFICATION_TIMESTAMP));
                    @SuppressLint("Range") boolean readStatus = cursor.getInt(cursor.getColumnIndexOrThrow(COL_NOTIFICATION_READ)) == 1;

                    notifications.add(new Notification(id, uId, title, message, timestamp, readStatus));
                }
                callback.onSuccess(notifications);
            } catch (Exception e) {
                Log.e("DBLocal", "Error al obtener notificaciones para usuario: " + e.getMessage());
                callback.onFailure(e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                db.close();
            }
        }).start();
    }

    public void updateNotificationReadStatus(String notificationId, boolean readStatus, VoidCallback callback) {
        new Thread(() -> {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_NOTIFICATION_READ, readStatus ? 1 : 0);

            String selection = COL_NOTIFICATION_ID + " = ?";
            String[] selectionArgs = {notificationId};
            int rowsAffected = 0;
            try {
                rowsAffected = db.update(TABLE_NOTIFICATIONS, values, selection, selectionArgs);
                if (rowsAffected > 0) {
                    Log.d("DBLocal", "Estado de lectura de notificación actualizado: " + notificationId);
                    callback.onSuccess();
                } else {
                    Log.e("DBLocal", "Error al actualizar estado de lectura de notificación o no encontrada: " + notificationId);
                    callback.onFailure(new Exception("Notificación no encontrada o no se pudo actualizar."));
                }
            } catch (Exception e) {
                Log.e("DBLocal", "Excepción al actualizar estado de lectura de notificación: " + e.getMessage());
                callback.onFailure(e);
            } finally {
                db.close();
            }
        }).start();
    }

    // --- Callback Interfaces ---
    public interface VoidCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface UserCallback {
        void onSuccess(User user);
        void onFailure(Exception e);
    }

    public interface ListCallback<T> {
        void onSuccess(List<T> list);
        void onFailure(Exception e);
    }

    public interface StringCallback {
        void onSuccess(String result);
        void onFailure(Exception e);
    }
}