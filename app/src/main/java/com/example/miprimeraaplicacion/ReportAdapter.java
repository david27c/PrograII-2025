package com.example.miprimeraaplicacion;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private Context context;
    private List<Report> reportList;

    public ReportAdapter(Context context, List<Report> reportList) {
        this.context = context;
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reportList.get(position);

        holder.textViewReportTitle.setText(report.getType()); // Usamos el tipo como título
        holder.textViewReportLocation.setText("Ubicación: " + report.getLocation());

        // Formatear la fecha
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.textViewReportDate.setText("Fecha: " + sdf.format(report.getTimestamp()));

        holder.textViewReportStatus.setText("Estado: " + report.getStatus());

        // Asignar color al estado
        switch (report.getStatus()) {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            if (report.getImageUrl() != null && !report.getImageUrl().isEmpty()) {
                Picasso.get().load(report.getImageUrl())
                        .placeholder(R.drawable.placeholder_image) // Placeholder mientras carga
                        .error(R.drawable.placeholder_image) // Imagen de error si falla la carga
                        .into(holder.imageViewReportThumbnail);
            } else {
                holder.imageViewReportThumbnail.setImageResource(R.drawable.placeholder_image);
            }
        }

        // Listener para abrir los detalles del reporte al hacer clic
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReportDetailActivity.class);
            intent.putExtra("reportId", report.getId()); // Pasa el ID del reporte
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewReportThumbnail;
        TextView textViewReportTitle, textViewReportLocation, textViewReportStatus, textViewReportDate;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewReportThumbnail = itemView.findViewById(R.id.imageViewReportThumbnail);
            textViewReportTitle = itemView.findViewById(R.id.textViewReportTitle);
            textViewReportLocation = itemView.findViewById(R.id.textViewReportLocation);
            textViewReportStatus = itemView.findViewById(R.id.textViewReportStatus);
            textViewReportDate = itemView.findViewById(R.id.textViewReportDate);
        }
    }

    // Método para actualizar la lista de reportes
    public void updateReports(List<Report> newReports) {
        reportList.clear();
        reportList.addAll(newReports);
        notifyDataSetChanged();
    }

    private class Report {
        private int type;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getImageUrl() {
            return null;
        }

        public boolean getId() {
            return false;
        }

        public String getLocation() {
            String o = null;
            return o;
        }

        public Object getTimestamp() {
            return null;
        }

        public String getStatus() {
            return "";
        }
    }

    private static class Picasso {
        public static System get() {
            return null;
        }
    }
}