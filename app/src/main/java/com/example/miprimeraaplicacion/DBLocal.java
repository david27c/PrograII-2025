package com.example.miprimeraaplicacion;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DBLocal extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "denuncias_app.db";
    // Incrementa la versión de la base de datos para que onUpgrade se ejecute y recree las tablas
    private static final int DATABASE_VERSION = 6; // CAMBIO: Aumentar la versión de la base de datos

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
    private static final String COL_USER_ID = "user_id";
    private static final String COL_USER_EMAIL = "email";
    private static final String COL_USER_PASSWORD = "password";
    private static final String COL_USER_USERNAME = "username";
    private static final String COL_USER_FULL_NAME = "full_name"; // NUEVA COLUMNA
    private static final String COL_USER_PHONE = "phone";
    private static final String COL_USER_ADDRESS = "address";
    private static final String COL_USER_PROFILE_IMAGE_URL = "profile_image_url";
    private static final String COL_USER_REPORTS_COUNT = "reports_count";
    private static final String COL_USER_SHOW_FULL_NAME_PUBLIC = "show_full_name_public";
    private static final String COL_USER_SHOW_PROFILE_PHOTO_IN_COMMENTS = "show_profile_photo_in_comments"; // NUEVA COLUMNA
    private static final String COL_USER_SHOW_EMAIL_PUBLIC = "show_email_public"; // NUEVA COLUMNA
    private static final String COL_USER_SHOW_PHONE_PUBLIC = "show_phone_public"; // NUEVA COLUMNA


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

    // Sentencia SQL para crear la tabla de USUARIOS (ACTUALIZADA CON NUEVAS COLUMNAS)
    private static final String SQL_CREATE_USERS_TABLE =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COL_USER_ID + " TEXT PRIMARY KEY," +
                    COL_USER_EMAIL + " TEXT UNIQUE," +
                    COL_USER_PASSWORD + " TEXT," +
                    COL_USER_USERNAME + " TEXT DEFAULT ''," +
                    COL_USER_FULL_NAME + " TEXT DEFAULT ''," + // NUEVA COLUMNA
                    COL_USER_PHONE + " TEXT DEFAULT ''," +
                    COL_USER_ADDRESS + " TEXT DEFAULT ''," +
                    COL_USER_PROFILE_IMAGE_URL + " TEXT DEFAULT ''," +
                    COL_USER_REPORTS_COUNT + " INTEGER DEFAULT 0," +
                    COL_USER_SHOW_FULL_NAME_PUBLIC + " INTEGER DEFAULT 0," +
                    COL_USER_SHOW_PROFILE_PHOTO_IN_COMMENTS + " INTEGER DEFAULT 0," + // NUEVA COLUMNA
                    COL_USER_SHOW_EMAIL_PUBLIC + " INTEGER DEFAULT 0," +             // NUEVA COLUMNA
                    COL_USER_SHOW_PHONE_PUBLIC + " INTEGER DEFAULT 0)";              // NUEVA COLUMNA


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
        db.execSQL(SQL_CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("DBLocal", "Actualizando la base de datos de la versión " + oldVersion + " a " + newVersion + ", lo que destruirá todos los datos antiguos.");
        // Para evitar problemas de versiones y para que los nuevos campos se añadan,
        // la solución más sencilla durante el desarrollo es borrar y recrear.
        // En una app en producción, se harían migraciones con ALTER TABLE ADD COLUMN.
        db.execSQL(SQL_DELETE_DENUNCIAS_TABLE);
        db.execSQL(SQL_DELETE_USERS_TABLE);
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
                return denuncia.getIdDenuncia();
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
                    COL_FECHA_HORA + " DESC"
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

                Denuncia denuncia = new Denuncia( // Esta declaración está bien aquí
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
                    COL_FECHA_HORA + " DESC"
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

                Denuncia denuncia = new Denuncia( // Esta declaración está bien aquí
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
        Denuncia denuncia = null; // Declaración inicial

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

                // Asignación a la variable existente, no una nueva declaración
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
     * Inicializa todos los campos del perfil de usuario con valores por defecto.
     * @param email El email del usuario.
     * @param password La contraseña del usuario.
     * @return true si el usuario se registró exitosamente, false si ya existe o hay un error.
     */
    public boolean registerUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        String userId = UUID.randomUUID().toString();

        // Crear un objeto User con los valores iniciales, usando el constructor completo
        User newUser = new User(
                userId,
                email,
                password,
                "", // username
                "", // fullName
                "", // phone
                "", // address
                "", // profileImageUrl
                0,  // reportsCount
                false, // showFullNamePublic
                false, // showProfilePhotoInComments
                false, // showEmailPublic
                false  // showPhonePublic
        );

        ContentValues values = new ContentValues();
        values.put(COL_USER_ID, newUser.getUserId());
        values.put(COL_USER_EMAIL, newUser.getEmail());
        values.put(COL_USER_PASSWORD, newUser.getPassword());
        values.put(COL_USER_USERNAME, newUser.getUsername());
        values.put(COL_USER_FULL_NAME, newUser.getFullName()); // Nuevo campo
        values.put(COL_USER_PHONE, newUser.getPhone());
        values.put(COL_USER_ADDRESS, newUser.getAddress());
        values.put(COL_USER_PROFILE_IMAGE_URL, newUser.getProfileImageUrl());
        values.put(COL_USER_REPORTS_COUNT, newUser.getReportsCount());
        values.put(COL_USER_SHOW_FULL_NAME_PUBLIC, newUser.isShowFullNamePublic() ? 1 : 0);
        values.put(COL_USER_SHOW_PROFILE_PHOTO_IN_COMMENTS, newUser.isShowProfilePhotoInComments() ? 1 : 0); // Nuevo campo
        values.put(COL_USER_SHOW_EMAIL_PUBLIC, newUser.isShowEmailPublic() ? 1 : 0);                     // Nuevo campo
        values.put(COL_USER_SHOW_PHONE_PUBLIC, newUser.isShowPhonePublic() ? 1 : 0);                     // Nuevo campo

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
                COL_USER_PASSWORD + " = ?";
        String[] selectionArgs = { email, password };

        Cursor cursor = null;
        String userId = null;
        try {
            cursor = db.query(
                    TABLE_USERS,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
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

    /**
     * Obtiene el perfil completo de un usuario por su ID.
     * @param userId El ID del usuario.
     * @return Un objeto User con los datos del perfil, o null si no se encuentra.
     */
    public User getUserProfile(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        String[] projection = {
                COL_USER_ID,
                COL_USER_EMAIL,
                COL_USER_PASSWORD,
                COL_USER_USERNAME,
                COL_USER_FULL_NAME, // NUEVO
                COL_USER_PHONE,
                COL_USER_ADDRESS,
                COL_USER_PROFILE_IMAGE_URL,
                COL_USER_REPORTS_COUNT,
                COL_USER_SHOW_FULL_NAME_PUBLIC,
                COL_USER_SHOW_PROFILE_PHOTO_IN_COMMENTS, // NUEVO
                COL_USER_SHOW_EMAIL_PUBLIC,             // NUEVO
                COL_USER_SHOW_PHONE_PUBLIC              // NUEVO
        };

        String selection = COL_USER_ID + " = ?";
        String[] selectionArgs = { userId };

        Cursor cursor = null;
        try {
            cursor = db.query(
                    TABLE_USERS,
                    projection,
                    selection,
                    selectionArgs,
                    null, null, null
            );

            if (cursor.moveToFirst()) {
                String retrievedUserId = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ID));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL));
                String password = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PASSWORD));
                String username = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_USERNAME));
                String fullName = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_FULL_NAME)); // Obtener nuevo campo
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PHONE));
                String address = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ADDRESS));
                String profileImageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PROFILE_IMAGE_URL));
                int reportsCount = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_REPORTS_COUNT));
                boolean showFullNamePublic = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_SHOW_FULL_NAME_PUBLIC)) == 1;
                boolean showProfilePhotoInComments = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_SHOW_PROFILE_PHOTO_IN_COMMENTS)) == 1; // Obtener nuevo campo
                boolean showEmailPublic = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_SHOW_EMAIL_PUBLIC)) == 1;                   // Obtener nuevo campo
                boolean showPhonePublic = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_SHOW_PHONE_PUBLIC)) == 1;                   // Obtener nuevo campo


                user = new User(
                        retrievedUserId, email, password, username, fullName, // Orden y campos correctos
                        phone, address, profileImageUrl, reportsCount,
                        showFullNamePublic, showProfilePhotoInComments, showEmailPublic, showPhonePublic // Campos booleanos
                );
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Error al obtener perfil de usuario: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return user;
    }

    /**
     * Actualiza los campos del perfil de usuario en la base de datos local.
     *
     * @param user El objeto User con los datos a actualizar.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public boolean updateProfile(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // No actualizar password ni userId ni email aquí, ya que generalmente se manejan por separado
        values.put(COL_USER_USERNAME, user.getUsername());
        values.put(COL_USER_FULL_NAME, user.getFullName());
        values.put(COL_USER_PHONE, user.getPhone());
        values.put(COL_USER_ADDRESS, user.getAddress());
        values.put(COL_USER_PROFILE_IMAGE_URL, user.getProfileImageUrl());
        values.put(COL_USER_REPORTS_COUNT, user.getReportsCount());
        values.put(COL_USER_SHOW_FULL_NAME_PUBLIC, user.isShowFullNamePublic() ? 1 : 0);
        values.put(COL_USER_SHOW_PROFILE_PHOTO_IN_COMMENTS, user.isShowProfilePhotoInComments() ? 1 : 0);
        values.put(COL_USER_SHOW_EMAIL_PUBLIC, user.isShowEmailPublic() ? 1 : 0);
        values.put(COL_USER_SHOW_PHONE_PUBLIC, user.isShowPhonePublic() ? 1 : 0);

        String selection = COL_USER_ID + " = ?";
        String[] selectionArgs = {user.getUserId()};

        int rowsAffected = -1;
        try {
            rowsAffected = db.update(TABLE_USERS, values, selection, selectionArgs);
            if (rowsAffected > 0) {
                Log.d("DBLocal", "Perfil de usuario actualizado correctamente: " + user.getUserId());
                return true;
            } else {
                Log.e("DBLocal", "Error al actualizar perfil de usuario o usuario no encontrado.");
                return false;
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Excepción al actualizar perfil de usuario: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }


    /**
     * Obtiene el email de un usuario por su ID.
     * @param userId El ID del usuario.
     * @return El email del usuario, o null si no se encuentra.
     */
    public String getUserEmail(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String email = null;

        String[] projection = { COL_USER_EMAIL };
        String selection = COL_USER_ID + " = ?";
        String[] selectionArgs = { userId };

        Cursor cursor = null;
        try {
            cursor = db.query(
                    TABLE_USERS,
                    projection,
                    selection,
                    selectionArgs,
                    null, null, null
            );

            if (cursor.moveToFirst()) {
                email = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL));
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Error al obtener email de usuario por ID: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return email;
    }

    /**
     * Elimina el documento de datos de un usuario de la base de datos local.
     * Este método se llama antes de eliminar la cuenta.
     * @param userId ID del usuario a eliminar.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     */
    public boolean deleteUser(String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_USER_ID + " = ?";
        String[] selectionArgs = {userId};
        int rowsAffected = -1;
        try {
            rowsAffected = db.delete(TABLE_USERS, selection, selectionArgs);
            if (rowsAffected > 0) {
                Log.d("DBLocal", "Datos de usuario eliminados de DBLocal: " + userId);
                return true;
            } else {
                Log.e("DBLocal", "Error al eliminar datos de usuario de DBLocal o usuario no encontrado.");
                return false;
            }
        } catch (Exception e) {
            Log.e("DBLocal", "Excepción al eliminar datos de usuario de DBLocal: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }
}