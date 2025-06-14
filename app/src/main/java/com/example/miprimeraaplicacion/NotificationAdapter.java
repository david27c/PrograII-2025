package com.example.miprimeraaplicacion;

import android.content.Context;
import android.content.Intent;
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

    public NotificationAdapter(Context context, List<Notification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
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

        if (!notification.isRead()) {
            // Asegúrate de que R.color.unread_notification_background esté definido en res/values/colors.xml
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.unread_notification_background));
            holder.textViewNotificationTitle.setTypeface(null, Typeface.BOLD);
            holder.textViewNotificationMessage.setTypeface(null, Typeface.BOLD);
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
            holder.textViewNotificationTitle.setTypeface(null, Typeface.NORMAL);
            holder.textViewNotificationMessage.setTypeface(null, Typeface.NORMAL);
        }

        holder.itemView.setOnClickListener(v -> {
            notification.setRead(true);
            // Actualizar la vista de este ítem para reflejar el cambio visual
            notifyItemChanged(position);

            // Asegurarse de que el SDK sea el correcto para VANILLA_ICE_CREAM (API 34+)
            // Si tu targetSdk es menor, usa Build.VERSION_CODES.R o similar, o quita esta verificación
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // Usamos R (API 30) como ejemplo de un API más común
                if (notification.getRelatedId() != null && !notification.getRelatedId().isEmpty()) {
                    Intent intent = new Intent(context, DenunciaDetailActivity.class);
                    intent.putExtra("reportId", notification.getRelatedId());
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Notificación: " + notification.getTitle(), Toast.LENGTH_SHORT).show();
                }
            } else {
                // Lógica para versiones antiguas de Android
                if (notification.getRelatedId() != null && !notification.getRelatedId().isEmpty()) {
                    Intent intent = new Intent(context, DenunciaDetailActivity.class);
                    intent.putExtra("reportId", notification.getRelatedId());
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Notificación: " + notification.getTitle(), Toast.LENGTH_SHORT).show();
                }
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

    // =======================================================================
    // LA CLASE ANIDADA 'Notification' QUE CAUSABA EL ERROR HA SIDO ELIMINADA.
    // AHORA SE UTILIZA LA CLASE Notification.java EXTERNA.
    // =======================================================================
}