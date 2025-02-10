package com.example.miprimeraaplicacion;

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
    Spinner spnDe, spnA;
    conversores objConversores = new conversores();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tbh = findViewById(R.id.tbhConversor);
        tbh.setup();

        tbh.addTab(tbh.newTabSpec("Monedas").setContent(R.id.tabMonedas).setIndicator("MONEDAS"));
        tbh.addTab(tbh.newTabSpec("Longitud").setContent(R.id.tabLongitud).setIndicator("LONGITUD"));
        tbh.addTab(tbh.newTabSpec("Tiempo").setContent(R.id.tabTiempo).setIndicator("TIEMPO"));
        tbh.addTab(tbh.newTabSpec("Almacenamiento").setContent(R.id.tabAlmacenamiento).setIndicator("ALMACENAMIENTO"));
        tbh.addTab(tbh.newTabSpec("Masa").setContent(R.id.tabMasa).setIndicator("MASA"));
        tbh.addTab(tbh.newTabSpec("Volumen").setContent(R.id.tabVolumen).setIndicator("VOLUMEN"));
        tbh.addTab(tbh.newTabSpec("Transferencia").setContent(R.id.tabTransferencia).setIndicator("TRANSFERENCIA"));

        btn = findViewById(R.id.btnCalcular);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int opcion = tbh.getCurrentTab();
                double cantidad = 0;

                try {
                    tempVal = findViewById(R.id.txtCantidad);
                    cantidad = Double.parseDouble(tempVal.getText().toString());

                    spnDe = getSpinner(opcion, true);
                    spnA = getSpinner(opcion, false);

                    int de = spnDe.getSelectedItemPosition();
                    int a = spnA.getSelectedItemPosition();

                    tempVal = findViewById(R.id.lblRespuesta);
                    double respuesta = objConversores.convertir(opcion, de, a, cantidad);
                    tempVal.setText("Respuesta: " + respuesta);

                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Ingrese una cantidad válida", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Error en la conversión", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }

    private Spinner getSpinner(int opcion, boolean isDe) {
        int idSpinner;
        switch (opcion) {
            case 0: idSpinner = isDe ? R.id.spnDeMonedas : R.id.spnAMonedas; break;
            case 1: idSpinner = isDe ? R.id.spnDeLongitud : R.id.spnALongitud; break;
            case 2: idSpinner = isDe ? R.id.spnDeTiempo : R.id.spnATiempo; break;
            case 3: idSpinner = isDe ? R.id.spnDeAlmacenamiento : R.id.spnAAlmacenamiento; break;
            case 4: idSpinner = isDe ? R.id.spnDeMasa : R.id.spnAMasa; break;
            case 5: idSpinner = isDe ? R.id.spnDeVolumen : R.id.spnAVolumen; break;
            case 6: idSpinner = isDe ? R.id.spnDeTransferencia : R.id.spnATransferencia; break;
            default: throw new IllegalStateException("Opción no válida");
        }
        return findViewById(idSpinner);
    }
}

class conversores {
    double[][] valores = {
            {1, 0.92, 0.80, 147.50, 0.98, 1.35, 1.50, 17.00}, // Monedas
            {1, 100, 1000, 0.001, 3.28084, 39.3701, 1.09361, 0.000621371}, // Longitud
            {1, 60, 3600, 86400, 604800, 2592000, 31536000}, // Tiempo
            {1, 1024, 1048576, 1073741824, 1099511627776L, 1125899906842624L}, // Almacenamiento
            {1, 1000, 1000000, 0.001, 2.20462, 35.274}, // Masa
            {1, 1000, 1000000, 0.264172, 1.05669, 2.11338, 33.814}, // Volumen
            {1, 1000, 1000000, 1000000000, 8000, 8000000, 8000000000L} // Transferencia
    };

    public double convertir(int opcion, int de, int a, double cantidad) {
        try {
            return valores[opcion][a] / valores[opcion][de] * cantidad;
        } catch (ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }
}