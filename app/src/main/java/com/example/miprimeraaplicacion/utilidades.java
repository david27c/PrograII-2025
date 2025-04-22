package com.example.miprimeraaplicacion;

import java.util.Base64;

public class utilidades {
    static String url_consulta = "http:// 192.168.84.58:5984/jose/_design/david";
    static String url_mto = "http:// 192.168.84.58:5984/jose";
    static String user = "admin";
    static String passwd = "12345";
    static String credencialesCodificadas = Base64.getEncoder().encodeToString((user + ":" + passwd).getBytes());
    public String generarUnicoId(){
        return java.util.UUID.randomUUID().toString();
    }
}