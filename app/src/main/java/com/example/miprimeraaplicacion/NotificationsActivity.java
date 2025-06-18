package com.example.miprimeraaplicacion;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences; // Importar para SharedPreferences
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
// REMOVIDO: import com.google.firebase.auth.FirebaseAuth;
// REMOVIDO: import com.google.firebase.auth.FirebaseUser;
// REMOVIDO: import com.google.firebase.firestore.FirebaseFirestore;
// REMOVIDO: import com.google.firebase.firestore.ListenerRegistration;
// REMOVIDO: import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private static final String TAG_NOTIFICATIONS = "NotificationsActivityDebug"; // Nueva etiqueta para logs
    private DBLocal dbLocal; // Solo DBLocal

    private RecyclerView recyclerViewNotifications;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;

    private ProgressBar progressBarNotifications;
    private TextView textViewNoNotifications;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG_NOTIFICATIONS, "onCreate() de NotificationsActivity: Inicio."); // LOG
        setContentView(R.layout.activity_notifications);

        dbLocal = new DBLocal(this); // Inicializar DBLocal
        Log.d(TAG_NOTIFICATIONS, "onCreate(): DBLocal inicializado."); // LOG

        // --- LÓGICA DE VERIFICACIÓN DE SESIÓN ---
        String currentUserId = dbLocal.getLoggedInUserId(this); // Obtener ID de usuario desde DBLocal
        Log.d(TAG_NOTIFICATIONS, "onCreate(): ID de usuario recuperado de DBLocal: " + currentUserId); // LOG

        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.d(TAG_NOTIFICATIONS, "onCreate(): No hay usuario logueado. Redirigiendo a LoginActivity."); // LOG
            Toast.makeText(this, "Debes iniciar sesión para ver tus notificaciones.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(NotificationsActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return; // Terminar onCreate aquí
        }
        // --- FIN DE LÓGICA DE VERIFICACIÓN DE SESIÓN ---

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Notificaciones");
        }
        Log.d(TAG_NOTIFICATIONS, "onCreate(): Toolbar inicializada."); // LOG

        recyclerViewNotifications = findViewById(R.id.recyclerViewNotifications);
        progressBarNotifications = findViewById(R.id.progressBarNotifications);
        textViewNoNotifications = findViewById(R.id.textViewNoNotifications);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        Log.d(TAG_NOTIFICATIONS, "onCreate(): Vistas principales inicializadas (findViewById)."); // LOG

        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(this, notificationList);
        recyclerViewNotifications.setAdapter(notificationAdapter);
        Log.d(TAG_NOTIFICATIONS, "onCreate(): RecyclerView y Adapter configurados."); // LOG


        bottomNavigationView.setOnItemSelectedListener(item -> { // Usa setOnItemSelectedListener para versiones recientes
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(NotificationsActivity.this, HomeActivity.class));
                finish();
                Log.d(TAG_NOTIFICATIONS, "BottomNav: Redirigiendo a HomeActivity."); // LOG
                return true;
            } else if (itemId == R.id.nav_report) {
                startActivity(new Intent(NotificationsActivity.this, ReportProblemActivity.class));
                finish();
                Log.d(TAG_NOTIFICATIONS, "BottomNav: Redirigiendo a ReportProblemActivity."); // LOG
                return true;
            } else if (itemId == R.id.nav_my_reports) {
                startActivity(new Intent(NotificationsActivity.this, MyReportsActivity.class));
                finish();
                Log.d(TAG_NOTIFICATIONS, "BottomNav: Redirigiendo a MyReportsActivity."); // LOG
                return true;
            } else if (itemId == R.id.nav_chat) {
                startActivity(new Intent(NotificationsActivity.this, CommunityChatActivity.class));
                finish();
                Log.d(TAG_NOTIFICATIONS, "BottomNav: Redirigiendo a CommunityChatActivity."); // LOG
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(NotificationsActivity.this, ProfileActivity.class));
                finish();
                Log.d(TAG_NOTIFICATIONS, "BottomNav: Redirigiendo a ProfileActivity."); // LOG
                return true;
            }
            return false;
        });
        //bottomNavigationView.setSelectedItemId(R.id.nav_notifications); // Esta línea generalmente se quita aquí
        // o se selecciona un item principal si esta actividad no es un destino directo de BottomNav
        Log.d(TAG_NOTIFICATIONS, "onCreate(): BottomNavigationView listener configurado."); // LOG


        // Cargar notificaciones inicialmente
        loadNotifications();
        Log.d(TAG_NOTIFICATIONS, "onCreate(): Método loadNotifications() llamado."); // LOG

        Log.d(TAG_NOTIFICATIONS, "onCreate() de NotificationsActivity: Fin."); // LOG
    }

    // ********************************************************************************
    // IMPORTANTE: Asegúrate de que tu método loadNotifications() también use DBLocal
    // para obtener el currentUserId, así:
    // ********************************************************************************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        // Puedes ocultar el ítem de notificaciones si ya estás en la actividad de notificaciones
        MenuItem notificationsItem = menu.findItem(R.id.action_notifications);
        if (notificationsItem != null) {
            notificationsItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(NotificationsActivity.this, SettingsActivity.class));
            return true;
        } else if (id == android.R.id.home) { // Para el botón de regreso de la Toolbar
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadNotifications() {
        // Obtener el ID de usuario local desde DBLocal (CORREGIDO AQUÍ TAMBIÉN)
        String currentUserId = dbLocal.getLoggedInUserId(this); // <--- CORREGIDO

        if (currentUserId == null || currentUserId.isEmpty()) { // Añadir .isEmpty()
            Toast.makeText(this, "Debes iniciar sesión para ver tus notificaciones.", Toast.LENGTH_SHORT).show();
            textViewNoNotifications.setText("Inicia sesión para ver tus notificaciones.");
            textViewNoNotifications.setVisibility(View.VISIBLE);
            recyclerViewNotifications.setVisibility(View.GONE);
            progressBarNotifications.setVisibility(View.GONE);
            Log.d(TAG_NOTIFICATIONS, "loadNotifications(): currentUserId es nulo o vacío. No se cargan notificaciones."); // LOG
            return;
        }

        progressBarNotifications.setVisibility(View.VISIBLE);
        textViewNoNotifications.setVisibility(View.GONE);
        recyclerViewNotifications.setVisibility(View.GONE);
        Log.d(TAG_NOTIFICATIONS, "loadNotifications(): Cargando notificaciones para el usuario: " + currentUserId); // LOG

        // Cargar notificaciones desde DBLocal
        List<Notification> loadedNotifications = dbLocal.getAllNotificationsForUser(currentUserId);

        notificationList.clear();
        notificationList.addAll(loadedNotifications);
        notificationAdapter.updateNotifications(notificationList); // Usar updateNotifications del adapter
        progressBarNotifications.setVisibility(View.GONE);

        if (notificationList.isEmpty()) {
            textViewNoNotifications.setText("No tienes notificaciones.");
            textViewNoNotifications.setVisibility(View.VISIBLE);
            recyclerViewNotifications.setVisibility(View.GONE);
            Log.d(TAG_NOTIFICATIONS, "loadNotifications(): No hay notificaciones. Mostrando mensaje 'No tienes notificaciones'."); // LOG
        } else {
            textViewNoNotifications.setVisibility(View.GONE);
            recyclerViewNotifications.setVisibility(View.VISIBLE);
            Log.d(TAG_NOTIFICATIONS, "loadNotifications(): Notificaciones cargadas desde DBLocal: " + notificationList.size()); // LOG
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG_NOTIFICATIONS, "onResume() de NotificationsActivity llamado."); // LOG
        loadNotifications(); // Recargar notificaciones cada vez que la actividad se vuelve visible
    }
}