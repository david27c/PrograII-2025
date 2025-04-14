package com.example.miprimeraaplicacion;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    FloatingActionButton fab;
    Button btn;
    TextView tempVal;
    DB db;
    String accion = "nuevo", idBebida = "", id = "", rev = "";
    ImageView img;
    ArrayList<String> urlFotos = new ArrayList<>();
    Intent tomarFotoIntent;
    utilidades utls;
    detectarInternet di;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        utls = new utilidades();
        img = findViewById(R.id.imgfotoBebida);
        db = new DB(this);
        btn = findViewById(R.id.btnGuardarBebida);
        btn.setOnClickListener(view -> guardarBebida());

        mostrarDatos();
        tomarFoto();

        fab = findViewById(R.id.fabListaBebidas); // Asegúrate de tener este ID en tu layout
        if (fab != null) {
            fab.setOnClickListener(view -> abrirVentanaListaBebidas());
        }
    }

    private void mostrarDatos() {
        try {
            Bundle parametros = getIntent().getExtras();
            if (parametros != null && parametros.containsKey("accion")) {
                accion = parametros.getString("accion");
                mostrarMsg("La accion es: " + accion);
                if (accion.equals("modificar")) {
                    JSONObject datos = new JSONObject(parametros.getString("bebidas"));
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

                    String fotosConcat = datos.getString("foto");
                    String[] rutas = fotosConcat.split("\\|");
                    urlFotos = new ArrayList<>(Arrays.asList(rutas));
                    if (!urlFotos.isEmpty() && new File(urlFotos.get(urlFotos.size() - 1)).exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(urlFotos.get(urlFotos.size() - 1));
                        img.setImageBitmap(bitmap);
                    }
                }
            } else {
                mostrarMsg("No se recibió la acción.");
            }
        } catch (Exception e) {
            mostrarMsg("Error al mostrar datos: " + e.getMessage());
        }
    }

    private void tomarFoto() {
        img.setOnClickListener(view -> checkCameraPermission());
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_IMAGE_CAPTURE);
        } else {
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent() {
        tomarFotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File fotoBebida = null;
        try {
            fotoBebida = crearImagenBebida();
            if (fotoBebida != null) {
                Uri uriFotobebida = FileProvider.getUriForFile(MainActivity.this,
                        getApplicationContext().getPackageName() + ".fileprovider", fotoBebida);
                tomarFotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriFotobebida);
                startActivityForResult(tomarFotoIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                mostrarMsg("No se pudo crear el archivo de imagen.");
            }
        } catch (Exception e) {
            mostrarMsg("Error al crear archivo de imagen: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                mostrarMsg("Permiso de cámara denegado.");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                if (!urlFotos.isEmpty() && new File(urlFotos.get(urlFotos.size() - 1)).exists()) {
                    img.setImageURI(Uri.parse(urlFotos.get(urlFotos.size() - 1)));
                }
            } else {
                mostrarMsg("No se tomó la foto o se canceló.");
            }
        } catch (Exception e) {
            mostrarMsg("Error al procesar resultado de la cámara: " + e.getMessage());
        }
    }

    private File crearImagenBebida() throws Exception {
        String fechaHoraMs = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()),
                fileName = "imagen_" + fechaHoraMs + "_";
        File dirAlmacenamiento = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!dirAlmacenamiento.exists()) {
            dirAlmacenamiento.mkdirs();
        }
        File image = File.createTempFile(fileName, ".jpg", dirAlmacenamiento);
        urlFotos.add(image.getAbsolutePath());
        return image;
    }

    private void mostrarMsg(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    private void abrirVentanaListaBebidas() {
        Intent intent = new Intent(this, lista_bebidas.class);
        startActivity(intent);
    }

    private void guardarBebida() {
        EditText etCodigo = findViewById(R.id.txtCodigo);
        EditText etDescripcion = findViewById(R.id.txtDescripcion);
        EditText etMarca = findViewById(R.id.txtMarca);
        EditText etPresentacion = findViewById(R.id.txtPresentacion);
        EditText etPrecio = findViewById(R.id.txtPrecio);

        String codigo = etCodigo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String marca = etMarca.getText().toString().trim();
        String presentacion = etPresentacion.getText().toString().trim();
        String precio = etPrecio.getText().toString().trim();

        String urlsFotosConcat = TextUtils.join("|", urlFotos);
        String[] datos = {idBebida, codigo, descripcion, marca, presentacion, precio, urlsFotosConcat};
        Bundle parametros = getIntent().getExtras();
        if (parametros != null && parametros.containsKey("accion")) {
            accion = parametros.getString("accion");
        }

        db.administrar_bebidas(accion, datos);
        Toast.makeText(getApplicationContext(), "Registro guardado con éxito.", Toast.LENGTH_LONG).show();
        abrirVentanaListaBebidas();
    }
}