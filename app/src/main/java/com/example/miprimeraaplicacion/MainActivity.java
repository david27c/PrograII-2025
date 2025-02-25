package com.example.miprimeraaplicacion;

import android.app.TabActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabHost tabHost = getTabHost();

        // Configuración de la primera pestaña (Cálculo de consumo de agua)
        TabHost.TabSpec spec1 = tabHost.newTabSpec("Consumo");
        spec1.setIndicator("Consumo de Agua");
        spec1.setContent(R.id.tab1);
        tabHost.addTab(spec1);

        // Configuración de la segunda pestaña (Conversor de Área)
        TabHost.TabSpec spec2 = tabHost.newTabSpec("Conversor");
        spec2.setIndicator("Conversor de Área");
        spec2.setContent(R.id.tab2);
        tabHost.addTab(spec2);

        // Cálculo del consumo de agua
        Button calcular = findViewById(R.id.btnCalcular);
        EditText txtMetros = findViewById(R.id.txtMetros);
        TextView txtResultado = findViewById(R.id.txtResultado);

        calcular.setOnClickListener(v -> {
            String valor = txtMetros.getText().toString();
            if (!valor.isEmpty()) {
                int metros = Integer.parseInt(valor);
                double totalPagar = calcularPago(metros);
                txtResultado.setText("Total a pagar: $" + totalPagar);
            } else {
                txtResultado.setText("Ingrese un valor válido.");
            }
        });

        // Conversor de Área con selección de unidades
        Button convertir = findViewById(R.id.btnConvertir);
        EditText txtValor = findViewById(R.id.txtValor);
        Spinner spinnerOrigen = findViewById(R.id.spinnerOrigen);
        Spinner spinnerDestino = findViewById(R.id.spinnerDestino);
        TextView txtConversion = findViewById(R.id.txtConversion);

        convertir.setOnClickListener(v -> {
            String valor = txtValor.getText().toString();
            if (!valor.isEmpty()) {
                double area = Double.parseDouble(valor);
                String unidadOrigen = spinnerOrigen.getSelectedItem().toString();
                String unidadDestino = spinnerDestino.getSelectedItem().toString();
                String resultado = convertirArea(area, unidadOrigen, unidadDestino);
                txtConversion.setText(resultado);
            } else {
                txtConversion.setText("Ingrese un valor válido.");
            }
        });
    }

    private double calcularPago(int metros) {
        double total = 6;
        if (metros > 18 && metros <= 28) {
            total += (metros - 18) * 0.45;
        } else if (metros > 28) {
            total += (10 * 0.45) + ((metros - 28) * 0.65);
        }
        return total;
    }

    private String convertirArea(double valor, String origen, String destino) {
        double factor = obtenerFactorConversion(origen, destino);
        return destino + ": " + (valor * factor);
    }

    private double obtenerFactorConversion(String origen, String destino) {
        // Factores de conversión entre unidades
        double[][] factores = {
                {1, 10.764, 1.431, 1.196, 0.00228, 0.000142, 0.0001}, // Metro Cuadrado
                {0.0929, 1, 0.1337, 0.1111, 0.00021, 0.000013, 0.000009}, // Pie Cuadrado
                {0.6929, 6.9600, 1, 0.8361, 0.017, 0.00108, 0.00074}, // Vara Cuadrada
                {0.8361, 9.0000, 1.1950, 1, 0.020, 0.00127, 0.00088}, // Yarda Cuadrada
                {435.6, 4840.0, 643.0, 535.0, 1, 0.0625, 0.0436}, // Tarea
                {0.0625, 0.555, 0.0735, 0.0625, 23.44, 1, 0.69}, // Manzana
                {10000, 107639, 1360, 1196, 23.44, 1.44, 1} // Hectárea
        };

        int indexOrigen = obtenerIndiceUnidad(origen);
        int indexDestino = obtenerIndiceUnidad(destino);
        return factores[indexOrigen][indexDestino];
    }

    private int obtenerIndiceUnidad(String unidad) {
        switch (unidad) {
            case "Pie Cuadrado": return 1;
            case "Vara Cuadrada": return 2;
            case "Yarda Cuadrada": return 3;
            case "Tareas": return 4;
            case "Manzanas": return 5;
            case "Hectáreas": return 6;
            default: return 0; // Metro Cuadrado
        }
    }
}