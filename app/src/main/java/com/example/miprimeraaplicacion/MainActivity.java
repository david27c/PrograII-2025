package com.example.miprimeraaplicacion;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    TextView tempVal;
    Button btn;
    MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tempVal = findViewById(R.id.lblReproductorMusica);
        reproductorMusca();
        btn = findViewById(R.id.btnIniciar);
        btn.setOnClickListener(v -> iniciar());
        btn = findViewById(R.id.btnPausar);
        btn.setOnClickListener(v -> pausar());
        btn = findViewById(R.id.btnParar);
        btn.setOnClickListener(v -> detener());
    }
    void reproductorMusca(){
        MediaPlayer.create(this, R.raw.audio);
        MediaPlayer.create(this, R.raw.audio);
    }
    void iniciar(){
        mediaPlayer.start();
        tempVal.setText("Reproduciendo...");
    }
    void pausar(){
        mediaPlayer.pause();
        tempVal.setText("Pausado...");
    }
    void detener(){
        mediaPlayer.stop();
        tempVal.setText("Detenido...");
        reproductorMusca();
    }
}