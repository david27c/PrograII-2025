package com.example.miprimeraaplicacion;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    FloatingActionButton fab;
    Button btn;
    TextView tempVal;
    DB db;
    String accion = "nuevo", idBebida = "";
    ImageView img;
    String urlCompletaFoto = "";
    Intent tomarFotoIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        img = findViewById(R.id.imgfotoBebida);
        db = new DB(this);
        btn = findViewById(R.id.btnGuardarBebida);
        btn.setOnClickListener(view -> guardarBebida());

        mostrarDatos();
        tomarFoto();

    }

        private void mostrarDatos() {
        try {
            Bundle parametros = getIntent().getExtras();
            accion = parametros.getString("accion");
           mostrarMsg("La accion es: "+accion);
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

                    urlCompletaFoto = datos.getString("foto");
                    Bitmap bitmap = BitmapFactory.decodeFile(urlCompletaFoto);
                    img.setImageBitmap(bitmap);


                }
        } catch (Exception e) {
            mostrarMsg("Error: "+e.getMessage());
        }
    }


    private void tomarFoto(){
        img.setOnClickListener(view->{
            tomarFotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File fotoBebida = null;
            try{
                 fotoBebida = crearImagenBebida();
               if(fotoBebida!=null ){
                        Uri uriFotobebida = FileProvider.getUriForFile(MainActivity.this,
                        "com.example.miprimeraaplicacion.fileprovider", fotoBebida);
                    tomarFotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriFotobebida);
                    startActivityForResult(tomarFotoIntent, 1);
                }else{
                    mostrarMsg("Nose pudo crear la imagen.");
                }
            }catch (Exception e){
                mostrarMsg("Error: "+e.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try{
            if( requestCode==1 && resultCode==RESULT_OK ){
                //Bitmap imagenBitmap = BitmapFactory.decodeFile(urlCompletaFoto);
                img.setImageURI(Uri.parse(urlCompletaFoto));
            }else{
                mostrarMsg("No se tomo la foto.");
            }
        }catch (Exception e){
            mostrarMsg("Error: "+e.getMessage());
        }
    }

    private File crearImagenBebida() throws Exception{
        String fechaHoraMs = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()),
                fileName = "imagen_"+ fechaHoraMs+"_";
        File dirAlmacenamiento = getExternalFilesDir(Environment.DIRECTORY_DCIM);
        if( dirAlmacenamiento.exists()==false ){
            dirAlmacenamiento.mkdir();
        }
        File image = File.createTempFile(fileName, ".jpg", dirAlmacenamiento);
        urlCompletaFoto = image.getAbsolutePath();
        return image;
    }
    private void mostrarMsg(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
    private void abrirVentana(){
        Intent intent = new Intent(this, lista_bebidas.class);
        startActivity(intent);
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

        String[] datos = {idBebida, codigo, descripcion, marca, presentacion, precio, urlCompletaFoto};
        Bundle parametros = getIntent().getExtras();
        accion = parametros.getString("accion");

        db.administrar_bebidas(accion, datos);
        Toast.makeText(getApplicationContext(), "Registro guardado con exito.", Toast.LENGTH_LONG).show();
        abrirVentana();
    }
}