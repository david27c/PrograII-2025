package com.example.miprimeraaplicacion;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

public class BebidaManager {

    private static final String TAG = "BebidaManager";
    private final Context context;
    private final DB dbHelper;
    private final detectarInternet detectorInternet;
    private final String COUCHDB_URL = "TU_URL_COUCHDB";
    private final String COUCHDB_DATABASE = "tu_basededatos_couchdb";

    public interface ServerCallback<T> {
        void onResponse(T response);
        void onError(String message);
    }

    public BebidaManager(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = new DB(this.context);
        this.detectorInternet = new detectarInternet(this.context);
    }

    public interface BebidaCallback {
        void onSuccess(List<Bebidas> bebidas);
        void onError(String mensaje);
    }

    public interface SingleBebidaCallback {
        void onSuccess(Bebidas bebida);
        void onError(String mensaje);
    }

    public interface CRUDCallback {
        void onSuccess(String mensaje);
        void onError(String mensaje);
    }

    public void listarBebidas(final BebidaCallback callback) {
        if (detectorInternet.hayConexionInternet()) {
            obtenerBebidasDesdeCouchDB(callback);
        } else {
            obtenerBebidasDesdeSQLite(callback);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void obtenerBebidasDesdeCouchDB(final BebidaCallback callback) {
        new AsyncTask<Void, Void, List<Bebidas>>() {
            @Override
            protected List<Bebidas> doInBackground(Void... voids) {
                List<Bebidas> bebidas = new ArrayList<>();
                obtenerDatosServidor datosServidor = new obtenerDatosServidor();
                datosServidor.setCallback(new ServerCallback<String>() {
                    @Override
                    public void onResponse(String respuesta) {
                        try {
                            JSONObject jsonObject = new JSONObject(respuesta);
                            JSONArray jsonArray = jsonObject.getJSONArray("rows");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject bebidaObject = jsonArray.getJSONObject(i).getJSONObject("value");
                                Bebidas bebida = parseJsonBebida(bebidaObject);
                                bebidas.add(bebida);
                                guardarBebidaEnSQLite(bebida);
                            }
                            callback.onSuccess(bebidas);
                        } catch (Exception e) {
                            Log.e(TAG, "Error al procesar datos de CouchDB: " + e.getMessage());
                            obtenerBebidasDesdeSQLiteInterno(callback);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        Log.e(TAG, "Error al obtener datos de CouchDB: " + message);
                        obtenerBebidasDesdeSQLiteInterno(callback);
                    }
                });
                try {
                    datosServidor.execute(COUCHDB_URL + "/" + COUCHDB_DATABASE + "/_all_docs?include_docs=true");
                    } catch (Exception e) {
                    Log.e(TAG, "Error al ejecutar la tarea de obtención de CouchDB: " + e.getMessage());
                    obtenerBebidasDesdeSQLiteInterno(callback);
                }
                return null;
            }

            @Override
            protected void onPostExecute(List<Bebidas> result) {
               }
        }.execute();
    }

    private void guardarBebidaEnSQLite(Bebidas bebida) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = crearContentValuesDesdeBebida(bebida);
        long newRowId = db.insert(DB.TABLA_BEBIDAS, null, values);
        db.close();
        if (newRowId == -1) {
            Log.e(TAG, "Error al guardar bebida desde CouchDB en SQLite: " + bebida.getIdBebida());
        }
    }

    private void obtenerBebidasDesdeSQLite(final BebidaCallback callback) {
        obtenerBebidasDesdeSQLiteInterno(callback);
    }

    private void obtenerBebidasDesdeSQLiteInterno(final BebidaCallback callback) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Bebidas> bebidas = new ArrayList<>();
        Cursor cursor = db.query(DB.TABLA_BEBIDAS, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Bebidas bebida = crearBebidaDesdeCursor(cursor);
                bebidas.add(bebida);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        callback.onSuccess(bebidas);
    }

    public void obtenerBebidaPorId(String idBebida, final SingleBebidaCallback callback) {
        if (detectorInternet.hayConexionInternet()) {
            obtenerBebidaDesdeCouchDBPorId(idBebida, callback);
        } else {
            obtenerBebidaDesdeSQLitePorId(idBebida, callback);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void obtenerBebidaDesdeCouchDBPorId(String idBebida, final SingleBebidaCallback callback) {
        new AsyncTask<String, Void, Bebidas>() {
            @Override
            protected Bebidas doInBackground(String... strings) {
                String bebidaId = strings[0];
                obtenerDatosServidor datosServidor = new obtenerDatosServidor();
                datosServidor.setCallback(new ServerCallback<String>() {
                    @Override
                    public void onResponse(String respuesta) {
                        try {
                            JSONObject jsonObject = new JSONObject(respuesta);
                            Bebidas bebida = parseJsonBebida(jsonObject);
                            guardarBebidaEnSQLite(bebida);
                            callback.onSuccess(bebida);
                        } catch (Exception e) {
                            Log.e(TAG, "Error al procesar datos de CouchDB para ID " + bebidaId + ": " + e.getMessage());
                            obtenerBebidaDesdeSQLitePorIdInterno(bebidaId, callback);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        Log.e(TAG, "Error al obtener bebida con ID " + bebidaId + " desde CouchDB: " + message);
                        obtenerBebidaDesdeSQLitePorIdInterno(bebidaId, callback);
                    }
                });
                try {
                    datosServidor.execute(COUCHDB_URL + "/" + COUCHDB_DATABASE + "/" + bebidaId);
                    // No se llama a .get() aquí
                } catch (Exception e) {
                    Log.e(TAG, "Error al ejecutar la tarea de obtención de CouchDB por ID: " + e.getMessage());
                    obtenerBebidaDesdeSQLitePorIdInterno(bebidaId, callback);
                }
                return null;
            }
        }.execute(idBebida);
    }

    private void obtenerBebidaDesdeSQLitePorId(String idBebida, final SingleBebidaCallback callback) {
        obtenerBebidaDesdeSQLitePorIdInterno(idBebida, callback);
    }

    private void obtenerBebidaDesdeSQLitePorIdInterno(String idBebida, final SingleBebidaCallback callback) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Bebidas bebida = null;
        Cursor cursor = db.query(DB.TABLA_BEBIDAS, null, DB.COL_ID_BEBIDA + "=?", new String[]{idBebida}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            bebida = crearBebidaDesdeCursor(cursor);
            cursor.close();
        }
        db.close();
        if (bebida != null) {
            callback.onSuccess(bebida);
        } else {
            callback.onError("Bebida con ID " + idBebida + " no encontrada localmente.");
        }
    }

    public void agregarBebida(final Bebidas bebida, final CRUDCallback callback) {
        if (detectorInternet.hayConexionInternet()) {
            agregarBebidaEnCouchDB(bebida, callback);
        } else {
            agregarBebidaEnSQLite(bebida, callback);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void agregarBebidaEnCouchDB(final Bebidas bebida, final CRUDCallback callback) {
        AsyncTask<Bebidas, Void, String> execute = new AsyncTask<Bebidas, Void, String>() {
            @SuppressLint("WrongThread")
            @Override
            protected String doInBackground(Bebidas... bebidas) {
                Bebidas nuevaBebida = bebidas[0];
                JSONObject jsonBebida = convertirBebidaAJSONObject(nuevaBebida, false);
                enviarDatosServidor enviarDatos = new enviarDatosServidor(context);
                final AtomicReference<String> couchDbResponse = new AtomicReference<>();
                final AtomicReference<String> errorMessage = new AtomicReference<>();

                enviarDatos.setCallback(new ServerCallback<String>() {
                    @Override
                    public void onResponse(String respuesta) {
                        couchDbResponse.set(respuesta);
                    }

                    @Override
                    public void onError(String message) {
                        errorMessage.set(message);
                    }
                });

                enviarDatos.execute(jsonBebida.toString(), "POST", COUCHDB_URL + "/" + COUCHDB_DATABASE);
                String response = couchDbResponse.get();
                if (response != null) {
                    return response;
                } else if (errorMessage.get() != null) {
                    try {
                        throw new ExecutionException(errorMessage.get(), null);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        throw new ExecutionException("Error desconocido al agregar bebida a CouchDB", null);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            protected void onPostExecute(String respuesta) {
                if (respuesta != null) {
                    try {
                        JSONObject jsonRespuesta = new JSONObject(respuesta);
                        if (jsonRespuesta.getBoolean("ok")) {
                            String nuevoId = jsonRespuesta.getString("id");
                            bebida.setIdBebida(nuevoId);
                            guardarBebidaEnSQLite(bebida);
                            callback.onSuccess("Bebida agregada exitosamente.");
                        } else {
                            callback.onError("Error al agregar bebida en CouchDB: " + respuesta);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al procesar respuesta de CouchDB al agregar: " + e.getMessage());
                        callback.onError("Error al agregar bebida.");
                    }
                } else {
                    callback.onError("Error de conexión al agregar bebida.");
                }
            }
        }.execute(bebida);
    }

    private void agregarBebidaEnSQLite(final Bebidas bebida, final CRUDCallback callback) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = crearContentValuesDesdeBebida(bebida);
        long newRowId = db.insert(DB.TABLA_BEBIDAS, null, values);
        db.close();
        if (newRowId != -1) {
            bebida.setIdBebida(String.valueOf(newRowId));
            callback.onSuccess("Bebida agregada localmente.");
            if (detectorInternet.hayConexionInternet()) {
                sincronizarBebidaConCouchDB(bebida, "POST");
            }
        } else {
            callback.onError("Error al agregar bebida localmente.");
        }
    }

    public void actualizarBebida(final Bebidas bebida, final CRUDCallback callback) {
        if (detectorInternet.hayConexionInternet()) {
            actualizarBebidaEnCouchDB(bebida, callback);
        } else {
            actualizarBebidaEnSQLite(bebida, callback);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void actualizarBebidaEnCouchDB(final Bebidas bebida, final CRUDCallback callback) {
        new AsyncTask<Bebidas, Void, String>() {
            @SuppressLint("WrongThread")
            @Override
            protected String doInBackground(Bebidas... bebidas) {
                Bebidas bebidaActualizar = bebidas[0];
                JSONObject jsonBebida = convertirBebidaAJSONObject(bebidaActualizar, true);
                enviarDatosServidor enviarDatos = new enviarDatosServidor(context);
                final AtomicReference<String> couchDbResponse = new AtomicReference<>();
                final AtomicReference<String> errorMessage = new AtomicReference<>();

                enviarDatos.setCallback(new ServerCallback<String>() {
                    @Override
                    public void onResponse(String respuesta) {
                        couchDbResponse.set(respuesta);
                    }

                    @Override
                    public void onError(String message) {
                        errorMessage.set(message);
                    }
                });

                String url = COUCHDB_URL + "/" + COUCHDB_DATABASE + "/" + bebidaActualizar.getIdBebida();
                try {
                    enviarDatos.execute(jsonBebida.toString(), "PUT", url);
                    String response = couchDbResponse.get();
                    if (response != null) {
                        return response;
                    } else if (errorMessage.get() != null) {
                        throw new ExecutionException(errorMessage.get(), null);
                    } else {
                        throw new ExecutionException("Error desconocido al actualizar bebida en CouchDB", null);
                    }
                } catch (ExecutionException e) {
                    Log.e(TAG, "Error al actualizar bebida en CouchDB: " + e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String respuesta) {
                if (respuesta != null) {
                    try {
                        JSONObject jsonRespuesta = new JSONObject(respuesta);
                        if (jsonRespuesta.getBoolean("ok")) {
                            actualizarBebidaEnSQLite(bebida, new CRUDCallback() {
                                @Override
                                public void onSuccess(String mensaje) {
                                    callback.onSuccess("Bebida actualizada exitosamente.");
                                }

                                @Override
                                public void onError(String mensaje) {
                                    callback.onError("Bebida actualizada en CouchDB, pero error local: " + mensaje);
                                }
                            });
                        } else {
                            callback.onError("Error al actualizar bebida en CouchDB: " + respuesta);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al procesar respuesta de CouchDB al actualizar: " + e.getMessage());
                        callback.onError("Error al actualizar bebida.");
                    }
                } else {
                    callback.onError("Error de conexión al actualizar bebida.");
                }
            }
        }.execute(bebida);
    }

    private void actualizarBebidaEnSQLite(final Bebidas bebida, final CRUDCallback callback) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = crearContentValuesDesdeBebida(bebida);
        int rowsAffected = db.update(DB.TABLA_BEBIDAS, values, DB.COL_ID_BEBIDA + "=?", new String[]{bebida.getIdBebida()});
        db.close();
        if (rowsAffected > 0) {
            callback.onSuccess("Bebida actualizada localmente.");
            if (detectorInternet.hayConexionInternet()) {
                sincronizarBebidaConCouchDB(bebida, "PUT");
            }
        } else {
            callback.onError("Error al actualizar bebida localmente.");
        }
    }

    public void eliminarBebida(final String idBebida, final CRUDCallback callback) {
        if (detectorInternet.hayConexionInternet()) {
            eliminarBebidaEnCouchDB(idBebida, callback);
        } else {
            eliminarBebidaEnSQLite(idBebida, callback);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void eliminarBebidaEnCouchDB(final String idBebida, final CRUDCallback callback) {
        new AsyncTask<String, Void, String>() {
            @SuppressLint("WrongThread")
            @Override
            protected String doInBackground(String... strings) {
                String bebidaIdEliminar = strings[0];
                enviarDatosServidor enviarDatos = new enviarDatosServidor(context);
                final AtomicReference<String> couchDbResponse = new AtomicReference<>();
                final AtomicReference<String> errorMessage = new AtomicReference<>();

                enviarDatos.setCallback(new ServerCallback<String>() {
                    @Override
                    public void onResponse(String respuesta) {couchDbResponse.set(respuesta);
                    }

                    @Override
                    public void onError(String message) {
                        errorMessage.set(message);
                    }
                });
                String url = COUCHDB_URL + "/" + COUCHDB_DATABASE + "/" + bebidaIdEliminar;
                try {
                    enviarDatos.execute(null, "DELETE", url);
                    String response = couchDbResponse.get();
                    if (response != null) {
                        return response;
                    } else if (errorMessage.get() != null) {
                        throw new ExecutionException(errorMessage.get(), null);
                    } else {
                        throw new ExecutionException("Error desconocido al eliminar bebida de CouchDB", null);
                    }
                } catch (ExecutionException e) {
                    Log.e(TAG, "Error al eliminar bebida de CouchDB: " + e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String respuesta) {
                if (respuesta != null) {
                    try {
                        JSONObject jsonRespuesta = new JSONObject(respuesta);
                        if (jsonRespuesta.getBoolean("ok")) {
                            eliminarBebidaEnSQLite(idBebida, new CRUDCallback() {
                                @Override
                                public void onSuccess(String mensaje) {
                                    callback.onSuccess("Bebida eliminada exitosamente.");
                                }

                                @Override
                                public void onError(String mensaje) {
                                    callback.onError("Bebida eliminada en CouchDB, pero error local: " + mensaje);
                                }
                            });
                        } else {
                            callback.onError("Error al eliminar bebida de CouchDB: " + respuesta);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al procesar respuesta de CouchDB al eliminar: " + e.getMessage());
                        callback.onError("Error al eliminar bebida.");
                    }
                } else {
                    callback.onError("Error de conexión al eliminar bebida.");
                }
            }
        }.execute(idBebida);
    }

    private void eliminarBebidaEnSQLite(final String idBebida, final CRUDCallback callback) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = db.delete(DB.TABLA_BEBIDAS, DB.COL_ID_BEBIDA + "=?", new String[]{idBebida});
        db.close();
        if (rowsAffected > 0) {
            callback.onSuccess("Bebida eliminada localmente.");
            if (detectorInternet.hayConexionInternet()) {
                Log.i(TAG, "Eliminación local exitosa. Sincronización con CouchDB no implementada completamente aquí.");
            }
        } else {
            callback.onError("Error al eliminar bebida localmente.");
        }
    }

    private Bebidas crearBebidaDesdeCursor(Cursor cursor) {
        String idBebida = cursor.getString(cursor.getColumnIndexOrThrow(DB.COL_ID_BEBIDA));
        String codigo = cursor.getString(cursor.getColumnIndexOrThrow(DB.COL_CODIGO));
        String descripcion = cursor.getString(cursor.getColumnIndexOrThrow(DB.COL_DESCRIPCION));
        String marca = cursor.getString(cursor.getColumnIndexOrThrow(DB.COL_MARCA));
        String presentacion = cursor.getString(cursor.getColumnIndexOrThrow(DB.COL_PRESENTACION));
        double precio = cursor.getDouble(cursor.getColumnIndexOrThrow(DB.COL_PRECIO));
        String fotosCadena = cursor.getString(cursor.getColumnIndexOrThrow(DB.COL_URL_FOTOS));
        List<String> fotos = new ArrayList<>();
        if (fotosCadena != null && !fotosCadena.isEmpty()) {
            fotos.addAll(java.util.Arrays.asList(fotosCadena.split(";")));
        }
        return new Bebidas(idBebida, codigo, descripcion, marca, presentacion, String.valueOf(precio), (ArrayList<String>) fotos);
    }

    private ContentValues crearContentValuesDesdeBebida(Bebidas bebida) {
        ContentValues values = new ContentValues();
        values.put(DB.COL_ID_BEBIDA, bebida.getIdBebida());
        values.put(DB.COL_CODIGO, bebida.getCodigo());
        values.put(DB.COL_DESCRIPCION, bebida.getDescripcion());
        values.put(DB.COL_MARCA, bebida.getMarca());
        values.put(DB.COL_PRESENTACION, bebida.getPresentacion());
        values.put(DB.COL_PRECIO, Double.parseDouble(bebida.getPrecio()));
        String fotosCadena = android.text.TextUtils.join(";", bebida.getUrlFotos());
        values.put(DB.COL_URL_FOTOS, fotosCadena);
        return values;
    }

    private Bebidas parseJsonBebida(JSONObject jsonObject) {
        String id = jsonObject.optString("_id");
        String codigo = jsonObject.optString("codigo");
        String descripcion = jsonObject.optString("descripcion");
        String marca = jsonObject.optString("marca");
        String presentacion = jsonObject.optString("presentacion");
        String precio = String.valueOf(jsonObject.optDouble("precio"));
        JSONArray fotosJsonArray = jsonObject.optJSONArray("fotos");
        List<String> fotos = new ArrayList<>();
        if (fotosJsonArray != null) {
            for (int i = 0; i < fotosJsonArray.length(); i++) {
                fotos.add(fotosJsonArray.optString(i));
            }
        }
        Bebidas bebida = new Bebidas(id, codigo, descripcion, marca, presentacion, precio, (ArrayList<String>) fotos);
        String rev = jsonObject.optString("_rev");
        bebida.set_rev(rev);
        return bebida;
    }

    private JSONObject convertirBebidaAJSONObject(Bebidas bebida, boolean incluirRevision) {
        JSONObject jsonObject = new JSONObject();
        try {
            if (incluirRevision) {
                bebida.get_rev();
            }
            jsonObject.put("codigo", bebida.getCodigo());
            jsonObject.put("descripcion", bebida.getDescripcion());
            jsonObject.put("marca", bebida.getMarca());
            jsonObject.put("presentacion", bebida.getPresentacion());
            jsonObject.put("precio", Double.parseDouble(bebida.getPrecio()));
            JSONArray fotosJsonArray = new JSONArray(bebida.getUrlFotos());
            jsonObject.put("fotos", fotosJsonArray);
        } catch (Exception e) {
            Log.e(TAG, "Error al convertir Bebida a JSONObject: " + e.getMessage());
        }
        return jsonObject;
    }

    @SuppressLint("StaticFieldLeak")
    private void sincronizarBebidaConCouchDB(final Bebidas bebida, final String method) {
        new AsyncTask<Void, Void, String>() {
            @SuppressLint("WrongThread")
            @Override
            protected String doInBackground(Void... voids) {
                if (method.equals("PUT")) {
                    bebida.get_rev();
                }
                JSONObject jsonBebida = convertirBebidaAJSONObject(bebida, false);
                enviarDatosServidor enviarDatos = new enviarDatosServidor(context);
                final AtomicReference<String> couchDbResponse = new AtomicReference<>();
                final AtomicReference<String> errorMessage = new AtomicReference<>();

                enviarDatos.setCallback(new ServerCallback<String>() {
                    @Override
                    public void onResponse(String respuesta) {
                        couchDbResponse.set(respuesta);
                    }

                    @Override
                    public void onError(String message) {
                        errorMessage.set(message);
                    }
                });
                String url = COUCHDB_URL + "/" + COUCHDB_DATABASE;
                if (method.equals("PUT") && bebida.getIdBebida() != null) {
                    url += "/" + bebida.getIdBebida();
                }
                try {
                    enviarDatos.execute(jsonBebida.toString(), method, url);
                    String response = couchDbResponse.get();
                    if (response != null) {
                        return response;
                    } else if (errorMessage.get() != null) {
                        throw new ExecutionException(errorMessage.get(), null);
                    } else {
                        throw new ExecutionException("Error desconocido al sincronizar con CouchDB", null);
                    }
                } catch (ExecutionException e) {
                    Log.e(TAG, "Error al sincronizar bebida con CouchDB (" + method + "): " + e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String respuesta) {
                if (respuesta != null) {
                    try {
                        JSONObject jsonRespuesta = new JSONObject(respuesta);
                        if (jsonRespuesta.getBoolean("ok")) {
                            Log.i(TAG, "Sincronización con CouchDB (" + method + ") exitosa para ID: " + bebida.getIdBebida());
                            if (method.equals("POST") && bebida.getIdBebida() == null) {
                                String nuevoId = jsonRespuesta.getString("id");
                                bebida.setIdBebida(nuevoId);
                                guardarBebidaEnSQLite(bebida);
                            } else if (method.equals("PUT")) {
                                String nuevaRev = jsonRespuesta.getString("rev");
                                bebida.set_rev(nuevaRev);
                                actualizarBebidaEnSQLite(bebida, new CRUDCallback() {
                                    @Override
                                    public void onSuccess(String mensaje) {
                                        Log.i(TAG, "Actualización de _rev local exitosa.");
                                    }

                                    @Override
                                    public void onError(String mensaje) {
                                        Log.e(TAG, "Error al actualizar _rev local: " + mensaje);
                                    }
                                });
                            }
                        } else {
                            Log.e(TAG, "Error al sincronizar con CouchDB (" + method + "): " + respuesta);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al procesar respuesta de sincronización de CouchDB (" + method + "): " + e.getMessage());
                    }
                } else {
                    Log.e(TAG, "Error de conexión al sincronizar con CouchDB (" + method + ").");
                }
            }
        }.execute();
    }
}