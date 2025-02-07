package com.example.miprimeraaplicacion;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    TabHost tbh;
    Button btn;
    TextView tempVal;
    Spinner spn;
    conversores objConversores = new conversores();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tbh = findViewById(R.id.tbhConversor);
        tbh.setup();

        // Configuración de las pestañas
        tbh.addTab(tbh.newTabSpec("Monedas").setContent(R.id.tabMonedas).setIndicator("MONEDAS", null));
        tbh.addTab(tbh.newTabSpec("Longitud").setContent(R.id.tabLongitud).setIndicator("LONGITUD", null));
        tbh.addTab(tbh.newTabSpec("Tiempo").setContent(R.id.tabTiempo).setIndicator("TIEMPO", null));
        tbh.addTab(tbh.newTabSpec("Almacenamiento").setContent(R.id.tabAlmacenamiento).setIndicator("ALMACENAMIENTO", null));
        tbh.addTab(tbh.newTabSpec("Masa").setContent(R.id.tabMasa).setIndicator("MASA", null));
        tbh.addTab(tbh.newTabSpec("Volumen").setContent(R.id.tabVolumen).setIndicator("VOLUMEN", null));
        tbh.addTab(tbh.newTabSpec("Transferencia de Datos").setContent(R.id.tabTransferencia).setIndicator("TRANSFERENCIA", null));

        btn = findViewById(R.id.btnCalcular);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int opcion = tbh.getCurrentTab();
                spn = findViewById(R.id.spnDeMonedas);
                int de = spn.getSelectedItemPosition();

                spn = findViewById(R.id.spnAMonedas);
                int a = spn.getSelectedItemPosition();

                tempVal = findViewById(R.id.txtCantidad);
                String cantidadText = tempVal.getText().toString();
                if (cantidadText.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Por favor ingrese una cantidad válida.", Toast.LENGTH_SHORT).show();
                    return;
                }

                double cantidad = Double.parseDouble(cantidadText);

                tempVal = findViewById(R.id.lblRespuesta);
                double respuesta = objConversores.convertir(opcion, de, a, cantidad);
                tempVal.setText("Respuesta: " + respuesta);
            }
        });
    }
}

class conversores {
    double[][] valores = {
            {1, 0.98, 7.73, 25.45, 36.78, 508.87, 8.74}, // monedas
            {1, 1000, 0.001, 1000000, 0.1, 0.0016, 0.0001}, // masa
            {1, 0.001, 1000, 0.000001, 0.000264, 0.0353, 0.0000394}, // volumen
            {1, 1000, 0.001, 0.000621371, 0.000539957, 1.09361, 39.3701}, // longitud
            {1, 1024, 0.0000009313, 0.0000000009313, 0.0000000000009313, 0.0000000000000009313, 0.0000000000000000009313}, // almacenamiento
            {1, 60, 3600, 86400, 31536000, 60 * 60, 60 * 60 * 24}, // tiempo
            {1, 1000, 0.000000125, 0.00000125, 8.0 / 1024, 0.000000125, 0.000000125}, // transferencia de datos
    };

    public double convertir(int opcion, int de, int a, double cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }
        return valores[opcion][a] / valores[opcion][de] * cantidad;
    }
}