package com.example.miprimeraaplicacion;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class lista_bebidas extends AppCompatActivity {
    Bundle parametros = new Bundle();
    ListView ltsBebidas;
    Cursor cBebidas;
    DB db;
    public final ArrayList<Bebidas> alBebidas = new ArrayList<>();
    final ArrayList<Bebidas> alBebidasCopia = new ArrayList<>();
    JSONArray jsonArray;
    JSONObject jsonObject;
    Bebidas misBebidas;
    FloatingActionButton fab;
    obtenerDatosServidor datosServidor;
    detectarInternet di;
    int posicion = 0;
    EditText txtBuscarBebidas;

    // Elementos de la interfaz para crear/editar
    EditText etCodigoCrearEditar;
    EditText etDescripcionCrearEditar;
    EditText etMarcaCrearEditar;
    EditText etPresentacionCrearEditar;
    EditText etPrecioCrearEditar;
    Button btnAbrirGaleriaCrearEditar;
    LinearLayout layoutFotosSeleccionadasCrearEditar;
    Button btnGuardarCrearEditar;
    ArrayList<String> selectedImagePaths = new ArrayList<>();
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 102;
    private String bebidaIdEditando = null;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_bebidas);

        parametros.putString("accion", "nuevo");
        db = new DB(this);

        fab = findViewById(R.id.fabAgregarBebida);
        fab.setOnClickListener(view -> mostrarFormularioCrear());

        ltsBebidas = findViewById(R.id.ltsBebidas);
        txtBuscarBebidas = findViewById(R.id.txtBuscarBebidas);

        etCodigoCrearEditar = findViewById(R.id.etCodigoCrearEditar);
        etDescripcionCrearEditar = findViewById(R.id.etDescripcionCrearEditar);
        etMarcaCrearEditar = findViewById(R.id.etMarcaCrearEditar);
        etPresentacionCrearEditar = findViewById(R.id.etPresentacionCrearEditar);
        etPrecioCrearEditar = findViewById(R.id.etPrecioCrearEditar);
        btnAbrirGaleriaCrearEditar = findViewById(R.id.btnAbrirGaleriaCrearEditar);
        layoutFotosSeleccionadasCrearEditar = findViewById(R.id.layoutFotosSeleccionadasCrearEditar);
        btnGuardarCrearEditar = findViewById(R.id.btnGuardarCrearEditar);

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new androidx.activity.result.ActivityResultCallback<androidx.activity.result.ActivityResult>() {
                    @Override
                    public void onActivityResult(androidx.activity.result.ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                if (data.getClipData() != null) {
                                    int clipDataCount = data.getClipData().getItemCount();
                                    for (int i = 0; i < clipDataCount; i++) {
                                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                                        String path = getPathFromUri(imageUri);
                                        if (path != null) {
                                            selectedImagePaths.add(path);
                                            Log.d("Galeria", "Foto seleccionada (ClipData): " + path);
                                            mostrarMiniaturasCrearEditar();
                                        }
                                    }
                                } else if (data.getData() != null) {
                                    Uri imageUri = data.getData();
                                    String path = getPathFromUri(imageUri);
                                    if (path != null) {
                                        selectedImagePaths.add(path);
                                        Log.d("Galeria", "Foto seleccionada (Single): " + path);
                                        mostrarMiniaturasCrearEditar();
                                    }
                                }
                                mostrarMsg("Se seleccionaron " + selectedImagePaths.size() + " fotos.");
                            } else {
                                mostrarMsg("No se seleccionó ninguna imagen.");
                            }
                        } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                            mostrarMsg("Selección de imagen cancelada.");
                        }
                    }
                });

        if (btnAbrirGaleriaCrearEditar != null) {
            btnAbrirGaleriaCrearEditar.setOnClickListener(v -> checkPermissionsAndOpenGallery());
        }

        if (btnGuardarCrearEditar != null) {
            btnGuardarCrearEditar.setOnClickListener(v -> guardarBebida());
        }

        // Muevo la llamada a ocultarFormularioCrearEditar() al final del onCreate()
        listarBebidas();
        buscarBebida();
        ocultarFormularioCrearEditar();
    }

    private void listarBebidas() {
        try {
            di = new detectarInternet(this);
            if (di.hayConexionInternet()) { //online
                datosServidor = new obtenerDatosServidor();
                datosServidor.setCallback(new obtenerDatosServidor.Callback() {
                    @Override
                    public void onRespuestaRecibida(String respuesta) {
                        try {
                            jsonObject = new JSONObject(respuesta);
                            jsonArray = jsonObject.getJSONArray("rows");
                            mostrarDatosBebida(true); // Indica que los datos son online
                        } catch (Exception e) {
                            mostrarMsg("Error al procesar datos del servidor: " + e.getMessage());
                            obtenerDatosBebidaLocal(); // Fallback a datos locales en caso de error de procesamiento
                        }
                    }

                    @Override
                    public void onFallo(String mensajeError) {
                        mostrarMsg("Error al obtener datos del servidor: " + mensajeError);
                        obtenerDatosBebidaLocal(); // Fallback a datos locales en caso de fallo de la red
                    }
                });
                datosServidor.execute();
            } else { //offline
                obtenerDatosBebidaLocal();
                mostrarDatosBebida(false); // Indica que los datos son offline
            }
        } catch (Exception e) {
            mostrarMsg("Error al listar bebidas: " + e.getMessage());
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mimenu, menu);
        try {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            posicion = info.position;
            JSONObject bebidaValue;
            if (di.hayConexionInternet() && jsonArray != null && jsonArray.length() > posicion) {
                bebidaValue = jsonArray.getJSONObject(posicion).getJSONObject("value");
            } else if (!di.hayConexionInternet() && alBebidas.size() > posicion) {
                bebidaValue = convertirBebidaAJSONObject(alBebidas.get(posicion));
            } else {
                return; // Evitar errores si la posición es inválida
            }
            menu.setHeaderTitle(bebidaValue.getString("codigo"));
        } catch (Exception e) {
            mostrarMsg("Error en el menú contextual: " + e.getMessage());
        }
    }

    private JSONObject convertirBebidaAJSONObject(Bebidas bebida) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("idBebida", bebida.getIdBebida());
            jsonObject.put("codigo", bebida.getCodigo());
            jsonObject.put("descripcion", bebida.getDescripcion());
            jsonObject.put("marca", bebida.getMarca());
            jsonObject.put("presentacion", bebida.getPresentacion());
            jsonObject.put("precio", bebida.getPrecio());
            JSONArray fotosArray = new JSONArray(bebida.getFotos());
            jsonObject.put("fotos", fotosArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        try {
            JSONObject bebidaValue = null;
            if (di.hayConexionInternet() && jsonArray != null && jsonArray.length() > posicion) {
                bebidaValue = jsonArray.getJSONObject(posicion).getJSONObject("value");
            } else if (!di.hayConexionInternet() && alBebidas.size() > posicion) {
                bebidaValue = convertirBebidaAJSONObject(alBebidas.get(posicion));
            } else {
                return super.onContextItemSelected(item);
            }

            if (item.getItemId() == R.id.mnxNuevo) {
                mostrarFormularioCrear();
            } else if (item.getItemId() == R.id.mnxModificar) {
                mostrarFormularioEditar(bebidaValue.toString());
            } else if (item.getItemId() == R.id.mnxEliminar) {
                eliminarBebida(bebidaValue.getString("idBebida"), bebidaValue.optString("_id"), bebidaValue.optString("_rev"));
            }
            return true;
        } catch (Exception e) {
            mostrarMsg("Error al seleccionar item del menú: " + e.getMessage());
            return super.onContextItemSelected(item);
        }
    }

    private void eliminarBebida(String idBebidaLocal, String _idServidor, String _revServidor) {
        AlertDialog.Builder confirmacion = new AlertDialog.Builder(this);
        confirmacion.setTitle("¿Seguro que desea eliminar?");
        confirmacion.setMessage("Código: " + idBebidaLocal);
        confirmacion.setPositiveButton("Sí", (dialog, which) -> {
            try {
                if (di.hayConexionInternet() && _idServidor != null && !_idServidor.isEmpty()) { //online
                    String url = utilidades.URL_MTO + "/" + _idServidor + "?rev=" + _revServidor;
                    enviarDatosServidor objEnviarDatosServidor = new enviarDatosServidor(this);
                    String respuesta = objEnviarDatosServidor.execute(new JSONObject().toString(), "DELETE", url).get();
                    if (respuesta != null) {
                        JSONObject respuestaJSON = new JSONObject(respuesta);
                        if (respuestaJSON.getBoolean("ok")) {
                            listarBebidas();
                            mostrarMsg("Bebida eliminada del servidor.");
                        } else {
                            mostrarMsg("Error al eliminar del servidor: " + respuesta);
                        }
                    } else {
                        mostrarMsg("Error de conexión al eliminar del servidor.");
                    }
                }
                String respuestaLocal = db.administrar_bebidas("eliminar", new String[]{idBebidaLocal});
                if (respuestaLocal.equals("ok")) {
                    listarBebidas();
                    mostrarMsg("Bebida eliminada localmente.");
                } else {
                    mostrarMsg("Error al eliminar localmente: " + respuestaLocal);
                }
            } catch (Exception e) {
                mostrarMsg("Error al eliminar bebida: " + e.getMessage());
            }
        });
        confirmacion.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        confirmacion.create().show();
    }

    private void mostrarFormularioCrear() {
        bebidaIdEditando = null;
        limpiarFormularioCrearEditar();
        mostrarElementosCrearEditar();
    }

    private void mostrarFormularioEditar(String bebidaJson) {
        mostrarElementosCrearEditar();
        try {
            JSONObject bebida = new JSONObject(bebidaJson);
            bebidaIdEditando = bebida.getString("idBebida");
            etCodigoCrearEditar.setText(bebida.getString("codigo"));
            etDescripcionCrearEditar.setText(bebida.getString("descripcion"));
            etMarcaCrearEditar.setText(bebida.getString("marca"));
            etPresentacionCrearEditar.setText(bebida.getString("presentacion"));
            etPrecioCrearEditar.setText(bebida.getString("precio"));
            selectedImagePaths.clear();
            JSONArray fotosArray = bebida.optJSONArray("fotos");
            if (fotosArray != null) {
                for (int i = 0; i < fotosArray.length(); i++) {
                    selectedImagePaths.add(fotosArray.getString(i));
                }
                mostrarMiniaturasCrearEditar();
            }
        } catch (Exception e) {
            mostrarMsg("Error al cargar datos para editar.");
        }
    }

    private void ocultarFormularioCrearEditar() {
        if (etCodigoCrearEditar != null) etCodigoCrearEditar.setVisibility(View.GONE);
        if (etDescripcionCrearEditar != null) etDescripcionCrearEditar.setVisibility(View.GONE);
        if (etMarcaCrearEditar != null) etMarcaCrearEditar.setVisibility(View.GONE);
        if (etPresentacionCrearEditar != null) etPresentacionCrearEditar.setVisibility(View.GONE);
        if (etPrecioCrearEditar != null) etPrecioCrearEditar.setVisibility(View.GONE);
        if (btnAbrirGaleriaCrearEditar != null) btnAbrirGaleriaCrearEditar.setVisibility(View.GONE);
        if (layoutFotosSeleccionadasCrearEditar != null) layoutFotosSeleccionadasCrearEditar.setVisibility(View.GONE);
        if (btnGuardarCrearEditar != null) btnGuardarCrearEditar.setVisibility(View.GONE);
    }

    private void mostrarElementosCrearEditar() {
        if (etCodigoCrearEditar != null) etCodigoCrearEditar.setVisibility(View.VISIBLE);
        if (etDescripcionCrearEditar != null) etDescripcionCrearEditar.setVisibility(View.VISIBLE);
        if (etMarcaCrearEditar != null) etMarcaCrearEditar.setVisibility(View.VISIBLE);
        if (etPresentacionCrearEditar != null) etPresentacionCrearEditar.setVisibility(View.VISIBLE);
        if (etPrecioCrearEditar != null) etPrecioCrearEditar.setVisibility(View.VISIBLE);
        if (btnAbrirGaleriaCrearEditar != null) btnAbrirGaleriaCrearEditar.setVisibility(View.VISIBLE);
        if (layoutFotosSeleccionadasCrearEditar != null) layoutFotosSeleccionadasCrearEditar.setVisibility(View.VISIBLE);
        if (btnGuardarCrearEditar != null) btnGuardarCrearEditar.setVisibility(View.VISIBLE);
    }

    private void limpiarFormularioCrearEditar() {
        if (etCodigoCrearEditar != null) etCodigoCrearEditar.setText("");
        if (etDescripcionCrearEditar != null) etDescripcionCrearEditar.setText("");
        if (etMarcaCrearEditar != null) etMarcaCrearEditar.setText("");
        if (etPresentacionCrearEditar != null) etPresentacionCrearEditar.setText("");
        if (etPrecioCrearEditar != null) etPrecioCrearEditar.setText("");
        selectedImagePaths.clear();
        mostrarMiniaturasCrearEditar();
    }

    private void guardarBebida() {
        String codigo = (etCodigoCrearEditar != null) ? etCodigoCrearEditar.getText().toString().trim() : "";
        String descripcion = (etDescripcionCrearEditar != null) ? etDescripcionCrearEditar.getText().toString().trim() : "";
        String marca = (etMarcaCrearEditar != null) ? etMarcaCrearEditar.getText().toString().trim() : "";
        String presentacion = presentacion();
        String precio = precio();
        String fotos = String.join(";", selectedImagePaths);

        if (!codigo.isEmpty() && !descripcion.isEmpty() && !marca.isEmpty() && !presentacion().isEmpty() && !precio().isEmpty()) {
            String respuesta;
            if (bebidaIdEditando != null) {
                // Modificar
                String[] datos = {bebidaIdEditando, codigo, descripcion, marca, presentacion, precio, fotos};
                respuesta = db.administrar_bebidas("modificar", datos);
                if (respuesta.equals("ok")) {
                    mostrarMsg("Bebida modificada.");
                } else {
                    mostrarMsg("Error al modificar: " + respuesta);
                }
            } else {
                // Crear nuevo
                String[] datos = {null, codigo, descripcion, marca, presentacion, precio, fotos};
                respuesta = db.administrar_bebidas("nuevo", datos);
                if (respuesta.equals("ok")) {
                    mostrarMsg("Nueva bebida guardada.");
                } else {
                    mostrarMsg("Error al guardar: " + respuesta);
                }
            }
            listarBebidas();
            ocultarFormularioCrearEditar();
        } else {
            mostrarMsg("Por favor, complete todos los campos.");
        }
    }

    private void mostrarMiniaturasCrearEditar() {
        if (layoutFotosSeleccionadasCrearEditar != null) {
            layoutFotosSeleccionadasCrearEditar.removeAllViews();
            for (String path : selectedImagePaths) {
                mostrarMiniaturaEnLayout(layoutFotosSeleccionadasCrearEditar, path);
            }
        }
    }

    private void mostrarMiniaturaEnLayout(LinearLayout layout, String path) {
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 100);
        params.setMargins(0, 0, 8, 0);
        imageView.setLayoutParams(params);
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        imageView.setImageBitmap(bitmap);
        layout.addView(imageView);
    }

    private void checkPermissionsAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_READ_EXTERNAL_STORAGE);
                return;
            }
        }
        openGallery();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull
    int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                mostrarMsg("Permiso de lectura de almacenamiento necesario.");
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imagePickerLauncher.launch(intent);
    }

    private String getPathFromUri(Uri uri) {
        String filePath = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            filePath = cursor.getString(columnIndex);
            cursor.close();
        }
        return filePath;
    }

    private void obtenerDatosBebidaLocal() {
        try {
            cBebidas = db.lista_bebidas();
            alBebidas.clear();
            if (cBebidas != null && cBebidas.moveToFirst()) {
                do {
                    String idBebida = cBebidas.getString(cBebidas.getColumnIndexOrThrow("idBebida"));
                    String codigo = cBebidas.getString(cBebidas.getColumnIndexOrThrow("codigo"));
                    String descripcion = cBebidas.getString(cBebidas.getColumnIndexOrThrow("descripcion"));
                    String marca = cBebidas.getString(cBebidas.getColumnIndexOrThrow("marca"));
                    String presentacion = cBebidas.getString(cBebidas.getColumnIndexOrThrow("presentacion"));
                    String precio = String.valueOf(cBebidas.getDouble(cBebidas.getColumnIndexOrThrow("precio")));
                    String fotosCadena = cBebidas.getString(cBebidas.getColumnIndexOrThrow("urlFotos"));
                    ArrayList<String> listaFotos = new ArrayList<>(Arrays.asList(fotosCadena.split(";")));
                    if (fotosCadena.isEmpty()) listaFotos.clear(); // Evitar lista con un solo string vacío

                    misBebidas = new Bebidas(idBebida, codigo, descripcion, marca, presentacion, precio, listaFotos);
                    alBebidas.add(misBebidas);
                } while (cBebidas.moveToNext());
            } else {
                mostrarMsg("No hay bebidas registradas localmente. Puede agregar una.");
            }
            if (cBebidas != null) {
                cBebidas.close();
            }
            alBebidasCopia.clear();
            alBebidasCopia.addAll(alBebidas);
            ltsBebidas.setAdapter(new AdaptadorBebidas(this, alBebidas));
            registerForContextMenu(ltsBebidas);
        } catch (Exception e) {
            mostrarMsg("Error al obtener datos locales: " + e.getMessage());
        }
    }

    private void mostrarDatosBebida(boolean online) {
        try {
            alBebidas.clear();
            if (online && jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject bebidaObject = jsonArray.getJSONObject(i).getJSONObject("value");
                    String idBebida = bebidaObject.getString("idBebida");
                    String codigo = bebidaObject.getString("codigo");
                    String descripcion = bebidaObject.getString("descripcion");
                    String marca = bebidaObject.getString("marca");
                    String presentacion = bebidaObject.getString("presentacion");
                    String precio = String.valueOf(bebidaObject.getDouble("precio"));
                    JSONArray fotosJsonArray = bebidaObject.optJSONArray("fotos");
                    ArrayList<String> listaFotos = new ArrayList<>();
                    if (fotosJsonArray != null) {
                        for (int j = 0; j < fotosJsonArray.length(); j++) {
                            listaFotos.add(fotosJsonArray.getString(j));
                        }
                    }
                    misBebidas = new Bebidas(idBebida, codigo, descripcion, marca, presentacion, precio, listaFotos);
                    alBebidas.add(misBebidas);
                }
            } else if (!online) {
                // Los datos offline ya se cargaron en obtenerDatosBebidaLocal
            }

            alBebidasCopia.clear();
            alBebidasCopia.addAll(alBebidas);
            ltsBebidas.setAdapter(new AdaptadorBebidas(this, alBebidas));
            registerForContextMenu(ltsBebidas);

        } catch (Exception e) {
            mostrarMsg("Error al mostrar datos de bebida: " + e.getMessage());
        }
    }

    private void buscarBebida() {
        txtBuscarBebidas.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                alBebidas.clear();
                String buscar = s.toString().trim().toLowerCase();
                if (buscar.isEmpty()) {
                    alBebidas.addAll(alBebidasCopia);
                } else {
                    for (Bebidas item : alBebidasCopia) {
                        if (item.getCodigo().toLowerCase().contains(buscar) ||
                                item.getDescripcion().toLowerCase().contains(buscar) ||
                                item.getMarca().toLowerCase().contains(buscar) ||
                                item.getPresentacion().toLowerCase().contains(buscar)) {
                            alBebidas.add(item);
                        }
                    }
                }
                ltsBebidas.setAdapter(new AdaptadorBebidas(getApplicationContext(), alBebidas));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private String presentacion() {
        return (etPresentacionCrearEditar != null) ? etPresentacionCrearEditar.getText().toString().trim() : "";
    }

    private String precio() {
        return (etPrecioCrearEditar != null) ? etPrecioCrearEditar.getText().toString().trim() : "";
    }

    private void mostrarMsg(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
}