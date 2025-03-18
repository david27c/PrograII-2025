package com.example.miprimeraaplicacion;

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
    Bebidas miBebida;
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
        ViewHolder holder;
        if (convertView == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.fotos_bebidas, parent, false);
            holder = new ViewHolder();
            holder.lblCodigoAdaptador = convertView.findViewById(R.id.lblCodigoAdaptador);
            holder.lblDescripcionAdaptador = convertView.findViewById(R.id.lblDescripcionAdaptador);
            holder.lblMarcaAdaptador = convertView.findViewById(R.id.lblMarcaAdaptador);
            holder.lblPresentacionAdaptador = convertView.findViewById(R.id.lblPresentacionAdaptador);
            holder.lblPrecioAdaptador = convertView.findViewById(R.id.lblPrecioAdaptador);
            holder.imgFotoAdaptador = convertView.findViewById(R.id.imgFotoAdaptador);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        try {
            miBebida = alBebidas.get(position);

            holder.lblCodigoAdaptador.setText(miBebida.getCodigo());
            holder.lblDescripcionAdaptador.setText(miBebida.getDescripcion());
            holder.lblMarcaAdaptador.setText(miBebida.getMarca());
            holder.lblPresentacionAdaptador.setText(miBebida.getPresentacion());
            holder.lblPrecioAdaptador.setText(miBebida.getPrecio());

            Bitmap bitmap = BitmapFactory.decodeFile(miBebida.getFoto());
            if (bitmap != null) {
                holder.imgFotoAdaptador.setImageBitmap(bitmap);
            } else {
                holder.imgFotoAdaptador.setImageResource(R.drawable.placeholder_image);
                Toast.makeText(context, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return convertView;
    }

    static class ViewHolder {
        TextView lblCodigoAdaptador;
        TextView lblDescripcionAdaptador;
        TextView lblMarcaAdaptador;
        TextView lblPresentacionAdaptador;
        TextView lblPrecioAdaptador;
        ImageView imgFotoAdaptador;
    }
}