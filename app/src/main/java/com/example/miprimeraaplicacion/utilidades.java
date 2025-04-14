package com.example.miprimeraaplicacion;

import android.os.Build;
import androidx.annotation.RequiresApi;
import java.util.Base64;

@RequiresApi(api = Build.VERSION_CODES.O)
public class utilidades {
    public static final String URL_CONSULTA = "https://192.168.84.204:5984/tienda_android/_design/tienda_android/_view/tienda_android";
    public static final String URL_MTO = "https://192.168.84.204:5984/tienda_android";
    public static final String USER = "admin";
    public static final String PASSWD = "12345";
    public static final String CREDENCIALES_CODIFICADAS = Base64.getEncoder().encodeToString((USER + ":" + PASSWD).getBytes());
    public static String credencialesCodificadas;

    public String generarUnicoId(){
        return java.util.UUID.randomUUID().toString();
    }
}