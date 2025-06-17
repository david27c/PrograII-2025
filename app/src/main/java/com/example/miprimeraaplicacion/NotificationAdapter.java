package com.example.miprimeraaplicacion;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences; // Importar para SharedPreferences
import android.graphics.Typeface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notificationList;
    private DBLocal dbLocal; // Instancia de DBLocal para actualizar el estado de lectura de la notificación

    public NotificationAdapter(Context context, List<Notification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
        this.dbLocal = new DBLocal(context); // Inicializar DBLocal aquí
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        holder.textViewNotificationTitle.setText(notification.getTitle());
        holder.textViewNotificationMessage.setText(notification.getMessage());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.textViewNotificationTimestamp.setText(sdf.format(new Date(notification.getTimestamp())));

        // Asegúrate de que tengas un color llamado unread_notification_background en res/values/colors.xml
        // Ejemplo: <color name="unread_notification_background">#E0E0E0</color> (gris claro)
        if (!notification.isRead()) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.unread_notification_background));
            holder.textViewNotificationTitle.setTypeface(null, Typeface.BOLD);
            holder.textViewNotificationMessage.setTypeface(null, Typeface.BOLD);
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
            holder.textViewNotificationTitle.setTypeface(null, Typeface.NORMAL);
            holder.textViewNotificationMessage.setTypeface(null, Typeface.NORMAL);
        }

        holder.itemView.setOnClickListener(v -> {
            // Marcar la notificación como leída en la base de datos local
            notification.setRead(true);
            dbLocal.updateNotificationReadStatus(notification.getId(), true); // Usar el ID de la notificación

            // Actualizar la vista de este ítem para reflejar el cambio visual
            notifyItemChanged(position);

            // Redirigir según el relatedId
            if (notification.getRelatedId() != null && !notification.getRelatedId().isEmpty()) {
                // Aquí puedes añadir lógica para diferenciar tipos de relatedId si es necesario
                // Por ejemplo, si es un relatedId de denuncia:
                Intent intent = new Intent(context, DenunciaDetailActivity.class);
                intent.putExtra("denunciaId", notification.getRelatedId()); // Usar "denunciaId" como extra
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Notificación: " + notification.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNotificationTitle, textViewNotificationMessage, textViewNotificationTimestamp;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNotificationTitle = itemView.findViewById(R.id.textViewNotificationTitle);
            textViewNotificationMessage = itemView.findViewById(R.id.textViewNotificationMessage);
            textViewNotificationTimestamp = itemView.findViewById(R.id.textViewNotificationTimestamp);
        }
    }

    public void updateNotifications(List<Notification> newNotifications) {
        notificationList.clear();
        notificationList.addAll(newNotifications);
        notifyDataSetChanged();
    }
}