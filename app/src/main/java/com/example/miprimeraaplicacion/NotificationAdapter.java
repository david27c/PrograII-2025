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
            notifyItemChanged(position);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
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

    private class Notification {
        public int getTitle() {
            return 0;
        }

        public int getMessage() {
            return 0;
        }

        public long getTimestamp() {
            return 0;
        }

        public boolean isRead() {
            return false;
        }

        public void setRead(boolean b) {
        }

        public CharSequence getRelatedId() {
            return null;
        }
    }
}