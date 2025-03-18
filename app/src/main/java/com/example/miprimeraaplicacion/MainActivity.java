package com.example.miprimeraaplicacion;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    Button btn;
    TextView tempVal;
    DB db;
    String accion = "nuevo", idBebida = "";
    ImageView img;
    String CompletaFoto = "";
    Intent tomarFotoIntent;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        img = findViewById(R.id.imgFotoBebida);
        db = new DB(this);
        btn = findViewById(R.id.btnguardarBebida);
        btn.setOnClickListener(view -> guardarBebida());

        mostrarDatos();
        tomarFoto();
    }

    private void mostrarDatos() {
        try {
            Bundle parametros = getIntent().getExtras();
            if (parametros != null) {
                accion = parametros.getString("accion");
                if (accion != null && accion.equals("modificar")) {
                    JSONObject datos = new JSONObject(parametros.getString("bebida"));
                    idBebida = datos.getString("idBebida");

                    tempVal = findViewById(R.id.txtCodigo);
                    tempVal.setText(datos.getString("codigo"));

                    tempVal = findViewById(R.id.txtDescripcion);
                    tempVal.setText(datos.getString("descripcion"));

                    tempVal = findViewById(R.id.txtMarca);
                    tempVal.setText(datos.getString("marca"));

                    tempVal = findViewById(R.id.txtPresentacion);
                    tempVal.setText(datos.getString("presentacion"));

                    tempVal = findViewById(R.id.txtPrecio);
                    tempVal.setText(datos.getString("precio"));

                    CompletaFoto = datos.getString("foto");
                    img.setImageURI(Uri.fromFile(new File(CompletaFoto)));
                }
            }
        } catch (Exception e) {
            mostrarMsg("Error: " + e.getMessage());
        }
    }

    private void tomarFoto() {
        img.setOnClickListener(view -> {
            tomarFotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File fotoBebida;
            try {
                fotoBebida = crearImagenBebida();
                Uri uriFotoBebida = FileProvider.getUriForFile(getApplicationContext(),
                        "com.ugb.miprimeraaplicacion.fileprovider", fotoBebida);
                tomarFotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriFotoBebida);
                startActivityForResult(tomarFotoIntent, 1);
            } catch (Exception e) {
                mostrarMsg("Error: " + e.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == 1 && resultCode == RESULT_OK) {
                img.setImageURI(Uri.fromFile(new File(CompletaFoto)));
            } else {
                mostrarMsg("No se tomó la foto.");
            }
        } catch (Exception e) {
            mostrarMsg("Error: " + e.getMessage());
        }
    }

    private File crearImagenBebida() throws Exception {
        @SuppressLint("SimpleDateFormat") String fechaHoraMs = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()),
                fileName = "imagen_" + fechaHoraMs + "_";
        File dirAlmacenamiento = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "");
        if (!dirAlmacenamiento.exists()) {
            dirAlmacenamiento.mkdirs();
        }
        File image = File.createTempFile(fileName, ".jpg", dirAlmacenamiento);
        CompletaFoto = image.getAbsolutePath();
        return image;
    }

    private void mostrarMsg(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    private void guardarBebida() {
        tempVal = findViewById(R.id.txtCodigo);
        String codigo = tempVal.getText().toString();

        tempVal = findViewById(R.id.txtDescripcion);
        String descripcion = tempVal.getText().toString();

        tempVal = findViewById(R.id.txtMarca);
        String marca = tempVal.getText().toString();

        tempVal = findViewById(R.id.txtPresentacion);
        String presentacion = tempVal.getText().toString();

        tempVal = findViewById(R.id.txtPrecio);
        String precio = tempVal.getText().toString();

        String[] datos = {idBebida, codigo, descripcion, marca, presentacion, precio, CompletaFoto};
        db.administrar_bebidas(accion, datos);
        Toast.makeText(getApplicationContext(), "Registro guardado con exito.", Toast.LENGTH_LONG).show();
        abrirVentana();
    }

    private void abrirVentana() {
        Intent intent = new Intent(this, lista_bebidas.class);
        startActivity(intent);
    }
}