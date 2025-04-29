package com.example.miprimeraaplicacion;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Base64;

@RequiresApi(api = Build.VERSION_CODES.O)
public class utilidades {
    public static final String URL_CONSULTA = url_consulta;
    public static final String CREDENCIALES_CODIFICADAS = credencialesCodificadas;

    static String url_consulta = "http://192.168.80.38:5984/jose/_design/david";
    static String url_mto = "http://192.168.80.38:5984/jose";
    static String user = "admin";
    static String passwd = "12345";
    static String credencialesCodificadas = Base64.getEncoder().encodeToString((user + ":" + passwd).getBytes());
    public String generarUnicoId(){
        return java.util.UUID.randomUUID().toString();
    }
}