package com.example.miprimeraaplicacion;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText etMetrosConsumidos, etAreaValor;
    private TextView tvResultado, tvAreaResultado;
    private Button btnConvertir;
    private Spinner spUnidadOrigen, spUnidadDestino;

    private final double TARIFA_POR_METRO = 0.65; // Tarifa por metro cúbico mayor a 28 metros
    private final double TARIFA_BASE = 6.00; // Tarifa base fija (1-18 metros)
    private final double TARIFA_INTERMEDIA = 0.45; // Tarifa intermedia (19-28 metros)

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configuración del TabHost
        TabHost tabHost = findViewById(R.id.tabHost);
        tabHost.setup();

        // Pestaña 1: Cálculo del pago de agua potable
        TabHost.TabSpec spec1 = tabHost.newTabSpec("Pago Agua");
        spec1.setIndicator("Pago Agua");
        spec1.setContent(R.id.tab1);
        tabHost.addTab(spec1);

        // Pestaña 2: Conversor de Área
        TabHost.TabSpec spec2 = tabHost.newTabSpec("Conversor Área");
        spec2.setIndicator("Conversor Área");
        spec2.setContent(R.id.tab2);
        tabHost.addTab(spec2);

        // Asignación de vistas
        etMetrosConsumidos = findViewById(R.id.etMetrosConsumidos);
        etAreaValor = findViewById(R.id.etAreaValor);
        tvResultado = findViewById(R.id.tvResultado);
        tvAreaResultado = findViewById(R.id.tvAreaResultado);
        Button btnCalcular = findViewById(R.id.btnCalcular);
        btnConvertir = findViewById(R.id.btnConvertir);
        spUnidadOrigen = findViewById(R.id.spUnidadOrigen);
        spUnidadDestino = findViewById(R.id.spUnidadDestino);

        // Configuración de Spinner
        String[] unidades = {"Pie Cuadrado", "Vara Cuadrada", "Yarda Cuadrada", "Metro Cuadrado", "Tareas", "Manzana", "Hectárea"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, unidades);
        spUnidadOrigen.setAdapter(adapter);
        spUnidadDestino.setAdapter(adapter);

        // Listeners de botones
        btnCalcular.setOnClickListener(v -> calcularPago());
        btnConvertir.setOnClickListener(v -> convertirArea());
    }

    private void calcularPago() {
        String metrosStr = etMetrosConsumidos.getText().toString();
        if (!metrosStr.isEmpty()) {
            double metrosConsumidos = Double.parseDouble(metrosStr);
            double totalPagar;

            if (metrosConsumidos >= 1 && metrosConsumidos <= 18) {
                totalPagar = TARIFA_BASE;
            } else if (metrosConsumidos > 18 && metrosConsumidos <= 28) {
                totalPagar = TARIFA_BASE + ((metrosConsumidos - 18) * TARIFA_INTERMEDIA);
            } else {
                totalPagar = TARIFA_BASE + (10 * TARIFA_INTERMEDIA) + ((metrosConsumidos - 28) * TARIFA_POR_METRO);
            }

            tvResultado.setText("Total a pagar: $" + String.format("%.2f", totalPagar));
        } else {
            tvResultado.setText("Ingrese los metros consumidos");
        }
    }

    private void convertirArea() {
        String valorStr = etAreaValor.getText().toString();
        if (!valorStr.isEmpty()) {
            double valor = Double.parseDouble(valorStr);
            String unidadOrigen = spUnidadOrigen.getSelectedItem().toString();
            String unidadDestino = spUnidadDestino.getSelectedItem().toString();
            double factorConversion = obtenerFactorConversion(unidadOrigen, unidadDestino);
            double resultado = valor * factorConversion;

            tvAreaResultado.setText("Resultado: " + String.format("%.2f", resultado) + " " + unidadDestino);
        } else {
            tvAreaResultado.setText("Ingrese un valor");
        }
    }

    private double obtenerFactorConversion(String origen, String destino) {
        // Factores de conversión básicos (referencia en metros cuadrados)
        double metroCuadrado = 1.0;
        double pieCuadrado = 0.092903;
        double varaCuadrada = 0.6987;
        double yardaCuadrada = 0.836127;
        double tareas = 628.86;
        double manzana = 7000.0;
        double hectarea = 10000.0;

        double factorOrigen = 1.0;
        double factorDestino = 1.0;

        switch (origen) {
            case "Pie Cuadrado":
                factorOrigen = pieCuadrado;
                break;
            case "Vara Cuadrada":
                factorOrigen = varaCuadrada;
                break;
            case "Yarda Cuadrada":
                factorOrigen = yardaCuadrada;
                break;
            case "Metro Cuadrado":
                factorOrigen = metroCuadrado;
                break;
            case "Tareas":
                factorOrigen = tareas;
                break;
            case "Manzana":
                factorOrigen = manzana;
                break;
            case "Hectárea":
                factorOrigen = hectarea;
                break;
        }

        switch (destino) {
            case "Pie Cuadrado":
                factorDestino = pieCuadrado;
                break;
            case "Vara Cuadrada":
                factorDestino = varaCuadrada;
                break;
            case "Yarda Cuadrada":
                factorDestino = yardaCuadrada;
                break;
            case "Metro Cuadrado":
                factorDestino = metroCuadrado;
                break;
            case "Tareas":
                factorDestino = tareas;
                break;
            case "Manzana":
                factorDestino = manzana;
                break;
            case "Hectárea":
                factorDestino = hectarea;
                break;
        }

        return factorOrigen / factorDestino;
    }
}
