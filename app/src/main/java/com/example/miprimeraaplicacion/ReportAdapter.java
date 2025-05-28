package com.example.miprimeraaplicacion;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.example.miprimeraaplicacion.R;
import com.example.miprimeraaplicacion.models.Report;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private Context context;
    private List<Report> reportList;
    private OnReportClickListener listener;

    public interface OnReportClickListener {
        void onReportClick(Report report);
    }

    public ReportAdapter(Context context, List<Report> reportList, OnReportClickListener listener) {
        this.context = context;
        this.reportList = reportList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_report_card, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reportList.get(position);

        holder.titleTextView.setText(report.getTitle());
        holder.locationTextView.setText(String.format("Ubicación: %.4f, %.4f", report.getLatitude(), report.getLongitude()));
        holder.statusTextView.setText("Estado: " + capitalizeFirstLetter(report.getStatus()));

        // Cargar imagen si existe
        if (report.getMediaUrl() != null && !report.getMediaUrl().isEmpty()) {
            Picasso.get().load(report.getMediaUrl())
                    .placeholder(R.drawable.ic_placeholder_image) // Imagen de carga
                    .error(R.drawable.ic_broken_image) // Imagen si hay error
                    .into(holder.reportImageView);
        } else {
            holder.reportImageView.setImageResource(R.drawable.ic_placeholder_image); // Imagen por defecto
        }

        // Formatear la fecha
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.dateTextView.setText(sdf.format(new Date(report.getTimestamp())));

        // Cambiar color del indicador de estado
        switch (report.getStatus().toLowerCase()) {
            case "pendiente":
                holder.statusColorIndicator.setBackgroundColor(Color.parseColor("#FFC107")); // Amarillo
                break;
            case "en_proceso":
                holder.statusColorIndicator.setBackgroundColor(Color.parseColor("#2196F3")); // Azul
                break;
            case "resuelto":
                holder.statusColorIndicator.setBackgroundColor(Color.parseColor("#4CAF50")); // Verde
                break;
            default:
                holder.statusColorIndicator.setBackgroundColor(Color.parseColor("#9E9E9E")); // Gris
                break;
        }

        holder.itemView.setOnClickListener(v -> listener.onReportClick(report));
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        ImageView reportImageView;
        TextView titleTextView;
        TextView locationTextView;
        TextView statusTextView;
        View statusColorIndicator;
        TextView dateTextView;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            reportImageView = itemView.findViewById(R.id.report_card_image);
            titleTextView = itemView.findViewById(R.id.report_card_title);
            locationTextView = itemView.findViewById(R.id.report_card_location);
            statusTextView = itemView.findViewById(R.id.report_card_status);
            statusColorIndicator = itemView.findViewById(R.id.status_color_indicator);
            dateTextView = itemView.findViewById(R.id.report_card_date);
        }
    }

    // Helper para capitalizar la primera letra de una cadena
    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    private class Report {
        public Object getLatitude() {
        }

        public int getTitle() {
            return 0;
        }
    }
}