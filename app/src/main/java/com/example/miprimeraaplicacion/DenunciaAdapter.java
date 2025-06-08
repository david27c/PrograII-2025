package com.example.miprimeraaplicacion;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DenunciaAdapter extends RecyclerView.Adapter<DenunciaAdapter.DenunciaViewHolder> {

    private final Context context;
    private final List<Denuncia> denunciaList;

    public DenunciaAdapter(Context context, List<Denuncia> denunciaList) {
        this.context = context;
        this.denunciaList = denunciaList;
    }

    @NonNull
    @Override
    public DenunciaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Ahora infla item_report.xml, que es el que nos proporcionaste
        View view = LayoutInflater.from(context).inflate(R.layout.item_report, parent, false);
        return new DenunciaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DenunciaViewHolder holder, int position) {
        Denuncia denuncia = denunciaList.get(position);

        // Mapeo de campos de Denuncia a los TextViews del layout item_report.xml
        // Los IDs del ViewHolder ahora coinciden con los de item_report.xml
        holder.textViewReportTitle.setText(denuncia.getTitulo());
        holder.textViewReportLocation.setText(String.format(Locale.getDefault(), "Ubicación: Lat: %.4f, Lon: %.4f", denuncia.getLatitud(), denuncia.getLongitud()));
        holder.textViewReportStatus.setText("Estado: " + denuncia.getEstado());

        // Formatear la fecha
        if (denuncia.getFechaHora() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.textViewReportDate.setText("Fecha: " + sdf.format(denuncia.getFechaHora()));
        } else {
            holder.textViewReportDate.setText("Fecha: N/A");
        }

        // Asignar color al estado
        switch (denuncia.getEstado()) {
            case "Resuelto":
                holder.textViewReportStatus.setTextColor(Color.parseColor("#4CAF50")); // Verde
                break;
            case "En Proceso":
                holder.textViewReportStatus.setTextColor(Color.parseColor("#FFC107")); // Naranja
                break;
            case "Pendiente":
            default:
                holder.textViewReportStatus.setTextColor(Color.parseColor("#F44336")); // Rojo
                break;
        }

        // Cargar imagen con Picasso
        if (denuncia.getUrlImagen() != null && !denuncia.getUrlImagen().isEmpty()) {
            Picasso.get().load(denuncia.getUrlImagen())
                    .placeholder(R.drawable.placeholder_image) // Usa tu drawable existente
                    .error(R.drawable.placeholder_image) // Usa tu drawable existente para errores también
                    .into(holder.imageViewReportThumbnail);
            holder.imageViewReportThumbnail.setVisibility(View.VISIBLE);
        } else {
            holder.imageViewReportThumbnail.setVisibility(View.GONE); // Ocultar si no hay imagen
            // Opcional: holder.imageViewReportThumbnail.setImageResource(R.drawable.placeholder_image); // Mostrar placeholder genérico si quieres
        }

        // Listener para abrir los detalles de la denuncia
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DenunciaDetailActivity.class);
            intent.putExtra("denunciaId", denuncia.getIdDenuncia());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return denunciaList.size();
    }

    // ViewHolder que ahora coincide con los IDs de item_report.xml
    public static class DenunciaViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewReportThumbnail;
        TextView textViewReportTitle, textViewReportLocation, textViewReportStatus, textViewReportDate;

        public DenunciaViewHolder(@NonNull View itemView) {
            super(itemView);
            // Referencias a los IDs de item_report.xml
            imageViewReportThumbnail = itemView.findViewById(R.id.imageViewReportThumbnail);
            textViewReportTitle = itemView.findViewById(R.id.textViewReportTitle);
            textViewReportLocation = itemView.findViewById(R.id.textViewReportLocation);
            textViewReportStatus = itemView.findViewById(R.id.textViewReportStatus);
            textViewReportDate = itemView.findViewById(R.id.textViewReportDate);
        }
    }

    public void updateDenuncias(List<Denuncia> newDenuncias) {
        denunciaList.clear();
        denunciaList.addAll(newDenuncias);
        notifyDataSetChanged();
    }
}