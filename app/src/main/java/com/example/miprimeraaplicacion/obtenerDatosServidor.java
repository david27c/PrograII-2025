package com.example.miprimeraaplicacion;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class obtenerDatosServidor extends AsyncTask<Void, Void, String> {
    private static final String TAG = "obtenerDatosServidor";
    private Callback respuestaCallback;

    public ThreadLocal<Object> execute(String s) {
        return null;
    }

    public void setCallback(BebidaManager.ServerCallback<String> serverCallback) {
    }

    public interface Callback {
        void onRespuestaRecibida(String respuesta);
        void onFallo(String mensajeError);
    }

    public void setCallback(Callback callback) {
        this.respuestaCallback = callback;
    }

    HttpURLConnection httpURLConnection;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected String doInBackground(Void... voids) {
        StringBuilder respuesta = new StringBuilder();
        try{
            URL url = new URL(utilidades.URL_CONSULTA);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Authorization", "Basic " + utilidades.CREDENCIALES_CODIFICADAS);

            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String linea;
                while((linea = bufferedReader.readLine()) != null){
                    respuesta.append(linea);
                }
            } else {
                return "Error en la petición HTTP: " + responseCode;
            }

        }catch (Exception e){
            Log.e(TAG, "Error al obtener datos del servidor: " + e.getMessage());
            return e.getMessage();
        }
        finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return respuesta.toString();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (respuestaCallback != null) {
            if (s != null && !s.startsWith("Error")) {
                respuestaCallback.onRespuestaRecibida(s);
                try {
                    JSONArray jsonArray = new JSONArray(s);
                    Log.d(TAG, "Respuesta JSON: " + jsonArray.toString(2));
                    // Procesa el JSONArray aquí en la Activity/Fragment que implementa el Callback
                } catch (Exception e) {
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        Log.d(TAG, "Respuesta JSONObject: " + jsonObject.toString(2));
                        // Procesa el JSONObject aquí
                    } catch (Exception ex) {
                        Log.d(TAG, "La respuesta no es JSON, es texto plano: " + s);
                        // Procesa el texto plano aquí
                    }
                }
            } else {
                respuestaCallback.onFallo(s != null ? s : "Error desconocido al obtener datos.");
            }
        } else {
            Log.w(TAG, "Callback no configurado para obtenerDatosServidor.");
        }
    }
}