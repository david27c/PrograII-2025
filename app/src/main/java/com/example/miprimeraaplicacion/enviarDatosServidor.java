package com.example.miprimeraaplicacion;

import android.content.Context;
import android.os.AsyncTask;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

public class enviarDatosServidor extends AsyncTask<String, String, String> {
    Context context;
    String respuesta = "";
    HttpURLConnection httpURLConnection;

    public enviarDatosServidor(Context context) {
        this.context = context;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        // Aquí puedes procesar la respuesta del servidor (s) en el hilo principal
        Log.d("enviarDatosServidor", "Respuesta del servidor: " + s);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected String doInBackground(String... parametros) {
        String jsonResponse = null; // Inicializar a null
        String jsonDatos = parametros[0];
        String metodo = parametros[1];
        String _url = parametros[2];
        BufferedReader bufferedReader = null; // Inicializar a null
        try {
            URL url = new URL(_url);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod(metodo);
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setRequestProperty("Authorization", "Basic " + utilidades.credencialesCodificadas);
            // Enviar los datos al servidor
            Writer writer = new BufferedWriter(new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8"));
            writer.write(jsonDatos);
            writer.close();
            // Obtener/leer la respuesta del servidor
            InputStream inputStream = httpURLConnection.getInputStream();
            if (inputStream == null) {
                return null;
            }
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuffer = new StringBuilder();
            String linea;
            while ((linea = bufferedReader.readLine()) != null) {
                stringBuffer.append(linea);
            }
            if (stringBuffer.length() > 0) {
                jsonResponse = stringBuffer.toString();
            }
        } catch (Exception e) {
            Log.e("enviarDatosServidor", "Error: " + e.getMessage());
            return e.getMessage();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    Log.e("enviarDatosServidor", "Error al cerrar el BufferedReader: " + e.getMessage());
                }
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return jsonResponse;
    }

    public void setCallback(BebidaManager.ServerCallback<String> serverCallback) {
    }
}