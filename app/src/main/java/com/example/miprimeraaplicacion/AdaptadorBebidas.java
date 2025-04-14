package com.example.miprimeraaplicacion;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class AdaptadorBebidas extends BaseAdapter {
    Context context;
    ArrayList<Bebidas> alBebidas;
    Bebidas misBebidas;
    LayoutInflater inflater;

    public AdaptadorBebidas(Context context, ArrayList<Bebidas> alBebidas) {
        this.context = context;
        this.alBebidas = alBebidas;
    }

    @Override
    public int getCount() {
        return alBebidas.size();
    }

    @Override
    public Object getItem(int position) {
        return alBebidas.get(position);
    }
    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("ViewHolder") View itemView = inflater.inflate(R.layout.fotos_bebidas, parent, false);
        try {
            misBebidas = alBebidas.get(position);

            TextView tempVal = itemView.findViewById(R.id.lblCodigoAdaptador);
            tempVal.setText(misBebidas.getIdBebida());

            tempVal = itemView.findViewById(R.id.lblDescripcionAdaptador);
            tempVal.setText(misBebidas.getDescripcion());

            tempVal = itemView.findViewById(R.id.lblMarcaAdaptador);
            tempVal.setText(misBebidas.getMarca());

            tempVal = itemView.findViewById(R.id.lblPresentacionAdaptador);
            tempVal.setText(misBebidas.getPresentacion());

            tempVal = itemView.findViewById(R.id.lblPrecioAdaptador);
            tempVal.setText(String.valueOf(misBebidas.getPrecio()));

            ImageView img = itemView.findViewById(R.id.imgFotoAdaptador);
            String fotoPath = misBebidas.getFoto();
            if (fotoPath != null && !fotoPath.isEmpty()) {
                Bitmap bitmap = BitmapFactory.decodeFile(fotoPath);
                img.setImageBitmap(bitmap);
            }

        } catch (Exception e) {
            Toast.makeText(context, "Error en AdaptadorBebidas: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return itemView;
    }
}
