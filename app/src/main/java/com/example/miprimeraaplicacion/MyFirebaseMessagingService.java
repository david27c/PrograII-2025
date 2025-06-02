package com.example.miprimeraaplicacion;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.example.miprimeraaplicacion.R; // Asegúrate de que el R.java es correcto
import com.example.miprimeraaplicacion.activities.Main.HomeActivity; // O la actividad a la que quieres redirigir

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    public static final String CHANNEL_ID = "DenunciaCiudadana_Channel";
    public static final String CHANNEL_NAME = "Notificaciones Generales";
    public static final String CHANNEL_DESCRIPTION = "Notificaciones importantes para la aplicación Denuncia Ciudadana.";

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
        // Envía este token a tu servidor de aplicación si gestionas usuarios.
        // Por ejemplo, asociar el token al ID de usuario en tu base de datos de Firestore
        // o en el backend que utilices para enviar notificaciones dirigidas.
        sendRegistrationToServer(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Manejar la notificación aquí
        // Primero, verificar si la notificación tiene datos de carga útil (payload de datos)
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            // Procesa el payload de datos si es necesario
            handleDataPayload(remoteMessage.getData());
        }

        // Si la notificación contiene un cuerpo de notificación (título y mensaje)
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }
    }

    private void handleDataPayload(Map<String, String> data) {
        // Aquí puedes procesar los datos recibidos.
        // Por ejemplo, si envías un 'reportId' en los datos, puedes abrir una actividad específica.
        String reportId = data.get("reportId");
        if (reportId != null) {
            Log.d(TAG, "Received reportId from data payload: " + reportId);
            // Puedes lanzar una intención para abrir DetailReportActivity con este ID
            // Intent intent = new Intent(this, DetailReportActivity.class);
            // intent.putExtra("reportId", reportId);
            // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            // PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
            // sendNotificationWithIntent("Nuevo reporte", "Se ha actualizado un reporte", pendingIntent);
        } else {
            // Manejar otros tipos de datos
            Log.d(TAG, "Data payload without specific reportId. Data: " + data.toString());
        }
    }


    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, HomeActivity.class); // Redirige a HomeActivity por defecto
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE); // FLAG_IMMUTABLE es necesario para API 31+

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification) // Asegúrate de tener un icono de notificación
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH); // Prioridad alta para que aparezca

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Para Android O (API 26) y superior, se requiere un Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESCRIPTION);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    // Método de ejemplo para enviar el token a tu servidor
    private void sendRegistrationToServer(String token) {
        // Aquí puedes implementar la lógica para guardar el token en Firestore
        // Por ejemplo, en la colección 'users', en el documento del usuario logueado
        // db.collection("users").document(currentUserId).update("fcmToken", token);
        Log.d(TAG, "Sending token to server: " + token);
    }
}