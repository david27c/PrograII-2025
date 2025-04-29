package com.example.miprimeraaplicacion;

import static android.app.ProgressDialog.show;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class AdaptadorProductos extends BaseAdapter {
    Context context;
    ArrayList<productos> alProductos;
    ArrayList<productos> alProductosOriginal; // Lista original para restaurar al hacer el filtro
    productos misProductos;
    LayoutInflater inflater;

    public AdaptadorProductos(Context context, ArrayList<productos> alProductos) {
        this.context = context;
        this.alProductos = alProductos;
        this.alProductosOriginal = new ArrayList<>(alProductos); // Copia de la lista original
    }

    @Override
    public int getCount() {
        return alProductos.size();
    }

    @Override
    public Object getItem(int position) {
        return alProductos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("ViewHolder") View itemView = inflater.inflate(R.layout.fotos, parent, false);
        try {
            misProductos = alProductos.get(position);

            TextView tempVal = itemView.findViewById(R.id.lblCodigoAdaptador);
            tempVal.setText(misProductos.getIdProducto());

            tempVal = itemView.findViewById(R.id.lblDescripcionAdaptador);
            tempVal.setText(misProductos.getDescripcion());

            tempVal = itemView.findViewById(R.id.lblMarcaAdaptador);
            tempVal.setText(misProductos.getMarca());

            tempVal = itemView.findViewById(R.id.lblPresentacionAdaptador);
            tempVal.setText(misProductos.getPresentacion());

            tempVal = itemView.findViewById(R.id.lblPrecioAdaptador);
            tempVal.setText(String.valueOf(misProductos.getPrecio()));

            ImageView img = itemView.findViewById(R.id.imgFotoAdaptador);
            String fotoPath = misProductos.getFoto();
            if (fotoPath != null && !fotoPath.isEmpty()) {
                Bitmap bitmap = BitmapFactory.decodeFile(fotoPath);
                img.setImageBitmap(bitmap);
            }

        } catch (Exception e) {
            Toast.makeText(context, "Error en AdaptadorProductos: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return itemView;
    }

    // Implementación del filtro
    public android.widget.Filter getFilter() {
        return new android.widget.Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults results = new FilterResults();
                if (charSequence == null || charSequence.length() == 0) {
                    results.values = alProductosOriginal;
                    results.count = alProductosOriginal.size();
                } else {
                    String filterPattern = charSequence.toString().toLowerCase().trim();
                    ArrayList<productos> filteredList = new ArrayList<>();

                     for (productos item : alProductosOriginal) {
                        if (item.getCodigo().toLowerCase().contains(filterPattern) ||
                                item.getDescripcion().toLowerCase().contains(filterPattern) ||
                                item.getMarca().toLowerCase().contains(filterPattern)) {
                            filteredList.add(item);
                        }
                    }

                    results.values = filteredList;
                    results.count = filteredList.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                alProductos = (ArrayList<productos>) filterResults.values;
                notifyDataSetChanged(); // Actualiza la vista
            }
        };
    }
}