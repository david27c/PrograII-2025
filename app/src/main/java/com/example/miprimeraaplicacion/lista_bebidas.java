package com.example.miprimeraaplicacion;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class lista_bebidas extends Activity {
    Bundle parametros = new Bundle();
    ListView ltsBebidas;
    Cursor cBebidas;
    DB db;
    final ArrayList<Bebidas> alBebidas = new ArrayList<>();
    final ArrayList<Bebidas> alBebidasCopia = new ArrayList<>();
    JSONArray jsonArray;
    JSONObject jsonObject;
    Bebidas miBebida;
    FloatingActionButton fab;
    int posicion = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_bebidas);

        parametros.putString("accion", "nuevo");
        db = new DB(this);

        fab = findViewById(R.id.fabAgregarBebida);
        fab.setOnClickListener(view -> abriVentana());

        ltsBebidas = findViewById(R.id.ltsBebidas);
        registerForContextMenu(ltsBebidas);

        obtenerDatosBebidas();
        buscarBebidas();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mimenu, menu);

        try {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            posicion = info.position;
            menu.setHeaderTitle(jsonArray.getJSONObject(posicion).getString("codigo"));
        } catch (Exception e) {
            mostrarMsg("Error: " + e.getMessage());
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        try {
            if (item.getItemId() == R.id.mnxNuevo) {
                abriVentana();
            } else if (item.getItemId() == R.id.mnxModificar) {
                parametros.putString("accion", "modificar");
                parametros.putString("bebidas", jsonArray.getJSONObject(posicion).toString());
                abriVentana();
            } else if (item.getItemId() == R.id.mnxEliminar) {
                eliminarBebida();
            }
            return true;
        } catch (Exception e) {
            mostrarMsg("Error: " + e.getMessage());
            return super.onContextItemSelected(item);
        }
    }

    private void eliminarBebida() {
        try {
            String codigo = jsonArray.getJSONObject(posicion).getString("codigo");
            AlertDialog.Builder confirmacion = new AlertDialog.Builder(this);
            confirmacion.setTitle("¿Está seguro de eliminar la bebida?");
            confirmacion.setMessage(codigo);
            confirmacion.setPositiveButton("Sí", (dialog, which) -> {
                try {
                    String respuesta = db.administrar_bebidas("eliminar", new String[]{jsonArray.getJSONObject(posicion).getString("idBebida")});
                    if (respuesta.equals("ok")) {
                        obtenerDatosBebidas();
                        mostrarMsg("Bebida eliminada con éxito");
                    } else {
                        mostrarMsg("Error: " + respuesta);
                    }
                } catch (Exception e) {
                    mostrarMsg("Error: " + e.getMessage());
                }
            });
            confirmacion.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
            confirmacion.create().show();
        } catch (Exception e) {
            mostrarMsg("Error: " + e.getMessage());
        }
    }

    private void abriVentana() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtras(parametros);
        startActivity(intent);
    }

    private void obtenerDatosBebidas() {
        try {
            cBebidas = db.lista_bebidas();
            if (cBebidas.moveToFirst()) {
                jsonArray = new JSONArray();
                do {
                    jsonObject = new JSONObject();
                    jsonObject.put("idBebida", cBebidas.getString(0));
                    jsonObject.put("codigo", cBebidas.getString(1));
                    jsonObject.put("descripcion", cBebidas.getString(2));
                    jsonObject.put("marca", cBebidas.getString(3));
                    jsonObject.put("presentacion", cBebidas.getString(4));
                    jsonObject.put("precio", cBebidas.getString(5));
                    jsonObject.put("foto", cBebidas.getString(6));
                    jsonArray.put(jsonObject);
                } while (cBebidas.moveToNext());
                mostrarDatosBebidas();
            } else {
                mostrarMsg("No hay bebidas registradas.");
                abriVentana();
            }
        } catch (Exception e) {
            mostrarMsg("Error: " + e.getMessage());
        }
    }

    private void mostrarDatosBebidas() {
        try {
            if (jsonArray.length() > 0) {
                alBebidas.clear();
                alBebidasCopia.clear();

                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    miBebida = new Bebidas(
                            jsonObject.getString("idBebida"),
                            jsonObject.getString("codigo"),
                            jsonObject.getString("descripcion"),
                            jsonObject.getString("marca"),
                            jsonObject.getString("presentacion"),
                            jsonObject.getString("precio"),
                            jsonObject.getString("foto")
                    );
                    alBebidas.add(miBebida);
                }
                alBebidasCopia.addAll(alBebidas);
                ltsBebidas.setAdapter(new AdaptadorBebidas(this, alBebidas));
            }
        } catch (Exception e) {
            mostrarMsg("Error: " + e.getMessage());
        }
    }

    private void mostrarMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void buscarBebidas() {
        EditText txtBuscar = findViewById(R.id.txtBuscar);
        txtBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                try {
                    alBebidas.clear();
                    for (Bebidas bebida : alBebidasCopia) {
                        if (bebida.getCodigo().toLowerCase().contains(charSequence.toString().toLowerCase())) {
                            alBebidas.add(bebida);
                        }
                    }
                    ltsBebidas.setAdapter(new AdaptadorBebidas(lista_bebidas.this, alBebidas));
                } catch (Exception e) {
                    mostrarMsg("Error: " + e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }
}