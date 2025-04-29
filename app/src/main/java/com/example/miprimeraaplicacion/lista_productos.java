package com.example.miprimeraaplicacion;

import static java.util.Locale.filter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class lista_productos extends Activity {
    Bundle parametros = new Bundle();
    ListView ltsproductos;
    Cursor cproductos;
    Cursor cproductosAuxiliar;
    DB db;
    final ArrayList<productos> alproductos = new ArrayList<productos>();
    final ArrayList<productos> alProductosCopia = new ArrayList<productos>();
    JSONArray jsonArray;
    JSONArray jsonArrayAuxiliar;
    JSONObject jsonObject;
    JSONObject jsonObjectAuxiliar;
    productos misProductos;
    FloatingActionButton fab;
    int posicion = 0;
    obtenerDatosServidor datosServidor;
    detectarInternet di;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_productos);

        parametros.putString("accion", "nuevo");
        db = new DB(this);

        fab = findViewById(R.id.fabAgregarProductos);
        fab.setOnClickListener(view -> abriVentana());
        listarDatos();
        DatosLocalRemoto();

        datosNube();
        listarDatos();
        buscarproductos();
    }

    private void DatosLocalRemoto() {
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mimenu, menu);
        try {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            posicion = info.position;
            di = new detectarInternet(this);
            if (di.hayConexionInternet()) {
                menu.setHeaderTitle(jsonArray.getJSONObject(posicion).getJSONObject("value").getString("codigo"));
            } else {
                menu.setHeaderTitle(jsonArray.getJSONObject(posicion).getString("codigo"));
            }

        } catch (Exception e) {
            mostrarMsg("Error:  1asada" + e.getMessage());
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        try {
            if (item.getItemId() == R.id.mnxNuevo) {
                abriVentana();
            } else if (item.getItemId() == R.id.mnxModificar) {
                di = new detectarInternet(this);
                if (di.hayConexionInternet()) {
                    parametros.putString("accion", "modificar");
                    parametros.putString("productos", jsonArray.getJSONObject(posicion).getJSONObject("value").toString());
                    abriVentana();
                } else {
                    parametros.putString("accion", "modificar");
                    parametros.putString("productos", jsonArray.getJSONObject(posicion).toString());
                    abriVentana();
                }
            } else if (item.getItemId() == R.id.mnxEliminar) {
                eliminarProducto();
            }
            return true;
        } catch (Exception e) {
            mostrarMsg("Error:  2" + e.getMessage());
            return super.onContextItemSelected(item);
        }
    }

    private void eliminarProducto() {
        try {
            String codigo;
            String idProducto;
            di = new detectarInternet(this);
            if (di.hayConexionInternet()) {
                codigo = jsonArray.getJSONObject(posicion).getJSONObject("value").getString("codigo");
                idProducto = jsonArray.getJSONObject(posicion).getJSONObject("value").getString("idProducto");
            } else {
                codigo = jsonArray.getJSONObject(posicion).getString("codigo");
                idProducto = jsonArray.getJSONObject(posicion).getString("idProducto");
            }

            AlertDialog.Builder confirmacion = new AlertDialog.Builder(this);
            confirmacion.setTitle("Esta seguro de eliminar a: ");
            confirmacion.setMessage(codigo);
            confirmacion.setPositiveButton("Si", (dialog, which) -> {
                try {
                    if (di.hayConexionInternet()) {
                        JSONObject datosproductos = new JSONObject();
                        String _id = jsonArray.getJSONObject(posicion).getJSONObject("value").getString("_id");
                        String _rev = jsonArray.getJSONObject(posicion).getJSONObject("value").getString("_rev");
                        String url = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            url = utilidades.url_mto + "/" + _id + "?rev=" + _rev;
                        }
                        enviarDatosServidor objEnviarDatosServidor = new enviarDatosServidor(this);
                        String respuesta = objEnviarDatosServidor.execute(datosproductos.toString(), "DELETE", url).get();
                        JSONObject respuestaJSON = new JSONObject(respuesta);

                        String respuestalocal = db.administrar_productos("eliminar", new String[]{idProducto});

                        if (respuestaJSON.getBoolean("ok") && respuestalocal.equals("ok")) {
                            listarDatos();
                            mostrarMsg("Registro eliminado con exito");
                        } else {
                            mostrarMsg("Error:  3" + respuesta);
                        }
                    } else {
                        db.administrarActualizados("modificar", "verdadero", idProducto);
                        String respuestalocal = db.administrar_productos("eliminar", new String[]{idProducto});

                        if (respuestalocal.equals("ok")) {
                            listarDatos();
                            mostrarMsg("Registro eliminado con exito");
                        } else {
                            mostrarMsg("Error:  3" + respuestalocal);
                        }
                    }
                } catch (Exception e) {
                    mostrarMsg("Error:  5" + e.getMessage());
                }
            });
            confirmacion.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
            confirmacion.create().show();
        } catch (Exception e) {
            mostrarMsg("Error:  6" + e.getMessage());
        }
    }

    private void abriVentana() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtras(parametros);
        startActivity(intent);
    }

    private void listarDatos() {
        try {
            di = new detectarInternet(this);
            if (di.hayConexionInternet()) {
                datosServidor = new obtenerDatosServidor();
                String respuesta = datosServidor.execute().get();
                jsonObject = new JSONObject(respuesta);
                jsonArray = jsonObject.getJSONArray("rows");

                cproductosAuxiliar = db.lista_productosActializados();

                if (cproductosAuxiliar.moveToFirst()) {
                    jsonArrayAuxiliar = new JSONArray();
                    String falso = "falso";

                    if (Objects.equals(cproductosAuxiliar.getString(1), falso)) {
                        mostrarDatosproductos();
                    }
                }
            } else {
                obtenerDatosproductos();
            }
        } catch (Exception e) {
            mostrarMsg("Error:  7" + e.getMessage());
        }
    }

    private void obtenerDatosproductos() {
        try {
            cproductos = db.lista_productos();

            if (cproductos.moveToFirst()) {
                jsonArray = new JSONArray();
                do {
                    jsonObject = new JSONObject();
                    jsonObject.put("idProducto", cproductos.getString(0));
                    jsonObject.put("codigo", cproductos.getString(1));
                    jsonObject.put("descripcion", cproductos.getString(2));
                    jsonObject.put("marca", cproductos.getString(3));
                    jsonObject.put("presentacion", cproductos.getString(4));
                    jsonObject.put("precio", cproductos.getString(5));
                    jsonObject.put("foto", cproductos.getString(6));
                    jsonObject.put("foto1", cproductos.getString(7));
                    jsonObject.put("foto2", cproductos.getString(8));

                    jsonArray.put(jsonObject);
                } while (cproductos.moveToNext());

                mostrarDatosproductos();
            } else {
                mostrarMsg("No hay productos registrados.");
                abriVentana();
            }
        } catch (Exception e) {
            mostrarMsg("Error:  8" + e.getMessage());
        }
    }

    private void mostrarDatosproductos() {
        try {
            if (jsonArray.length() >= 1) {
                ltsproductos = findViewById(R.id.ltsProductos);
                alproductos.clear();
                alProductosCopia.clear();
                di = new detectarInternet(this);
                cproductosAuxiliar = db.lista_productosActializados();

                boolean respuesta = di.hayConexionInternet();
                boolean results = true;

                if (cproductosAuxiliar.moveToFirst()) {
                    String verdadero = "verdadero";
                    if (Objects.equals(cproductosAuxiliar.getString(1), verdadero)) {
                        results = false;
                    }
                }

                for (int i = 0; i < jsonArray.length(); i++) {
                    if (respuesta && results) {
                        jsonObject = jsonArray.getJSONObject(i).getJSONObject("value");
                    } else {
                        jsonObject = jsonArray.getJSONObject(i);
                    }

                    misProductos = new productos(
                            jsonObject.getString("idProducto"),
                            jsonObject.getString("codigo"),
                            jsonObject.getString("descripcion"),
                            jsonObject.getString("marca"),
                            jsonObject.getString("presentacion"),
                            jsonObject.getString("precio"),
                            jsonObject.getString("foto"),
                            jsonObject.getString("foto1"),
                            jsonObject.getString("foto2")
                    );
                    alproductos.add(misProductos);
                }
                alProductosCopia.addAll(alproductos);
                ltsproductos.setAdapter(new AdaptadorProductos(this, alproductos));
                registerForContextMenu(ltsproductos);
            } else {
                mostrarMsg("No hay productos registrados.");
                abriVentana();
            }
        } catch (Exception e) {
            mostrarMsg("Error:  9" + e.getMessage());
        }
    }

    private void buscarproductos() {
        TextView tempVal = findViewById(R.id.txtBuscarProductos);
        tempVal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                ((AdaptadorProductos) ltsproductos.getAdapter()).getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void datosNube() {
        try {
            if (di.hayConexionInternet()) {
                db.administrarActualizados("modificar", "falso", "0");
            }
        } catch (Exception e) {
            mostrarMsg("Error:  10" + e.getMessage());
        }
    }

    private void mostrarMsg(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }
}