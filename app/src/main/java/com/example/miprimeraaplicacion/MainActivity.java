package com.example.miprimeraaplicacion;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    Button btn;
    TextView lblRespuesta;
    EditText txtNum1, txtNum2;
    Spinner spn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = findViewById(R.id.btnCalcular);
        txtNum1 = findViewById(R.id.txtNum1);
        txtNum2 = findViewById(R.id.txtNum2);
        lblRespuesta = findViewById(R.id.lblRespuesta);
        spn = findViewById(R.id.spnOpciones);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtNum1.getText().toString().isEmpty()) {
                    txtNum1.setError("Ingrese un número");
                    return;
                }

                double num1 = Double.parseDouble(txtNum1.getText().toString());
                double num2 = 0;
                if (!txtNum2.getText().toString().isEmpty()) {
                    num2 = Double.parseDouble(txtNum2.getText().toString());
                }

                double respuesta = 0.0;
                String msg = "";

                switch (spn.getSelectedItemPosition()) {
                    case 0:
                        respuesta = num1 + num2;
                        msg = "La suma es: " + respuesta;
                        break;
                    case 1:
                        respuesta = num1 - num2;
                        msg = "La resta es: " + respuesta;
                        break;
                    case 2:
                        respuesta = num1 * num2;
                        msg = "La multiplicación es: " + respuesta;
                        break;
                    case 3:
                        if (num2 == 0) {
                            msg = "Error: División por cero";
                            respuesta = Double.NaN;
                        } else {
                            respuesta = num1 / num2;
                            msg = "La división es: " + respuesta;
                        }
                        break;
                    case 4:
                        respuesta = Math.pow(num1, num2);
                        msg = "La exponenciación es: " + respuesta;
                        break;
                    case 5:
                        respuesta = (num1 * num2) / 100;
                        msg = "El porcentaje es: " + respuesta;
                        break;
                    case 6:
                        respuesta = Math.sqrt(num1);
                        msg = "La raíz cuadrada es: " + respuesta;
                        break;
                    case 7:
                        respuesta = Math.cbrt(num1);
                        msg = "La raíz cúbica es: " + respuesta;
                        break;
                    case 8:
                        if (num1 < 0 && num2 % 2 == 0) {
                            msg = "Error: No se puede calcular raíz de índice par de un número negativo";
                            respuesta = Double.NaN;
                        } else {
                            respuesta = Math.pow(num1, 1.0 / num2);
                            msg = "La raíz de índice " + num2 + " es: " + respuesta;
                        }
                        break;
                    case 9:
                        if (num1 < 0 || num1 != (int) num1) {
                            msg = "Error: El factorial solo se define para enteros positivos";
                            respuesta = Double.NaN;
                        } else {
                            respuesta = factorial((int) num1);
                            msg = "El factorial es: " + respuesta;
                        }
                        break;
                    case 10:
                        respuesta = num1 % num2;
                        msg = "El módulo es: " + respuesta;
                        break;
                    case 11:
                        respuesta = Math.max(num1, num2);
                        msg = "El mayor número es: " + respuesta;
                        break;
                }

                lblRespuesta.setText("Respuesta: " + (Double.isNaN(respuesta) ? "Error" : respuesta));
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private int factorial(int n) {
        if (n == 0 || n == 1) return 1;
        int result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }
}