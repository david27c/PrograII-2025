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

    private static final String TAG = "NotificationsActivity";

    // REMOVIDO: private FirebaseAuth mAuth;
    // REMOVIDO: private FirebaseFirestore db;
    private DBLocal dbLocal; // Solo DBLocal

    private RecyclerView recyclerViewNotifications;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;

    private ProgressBar progressBarNotifications;
    private TextView textViewNoNotifications;
    private BottomNavigationView bottomNavigationView;

    // REMOVIDO: private ListenerRegistration notificationsListener; // Ya no hay listener en tiempo real con SQLite

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // REMOVIDO: mAuth = FirebaseAuth.getInstance();
        // REMOVIDO: db = FirebaseFirestore.getInstance();
        dbLocal = new DBLocal(this); // Inicializar DBLocal

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Notificaciones");
        }

        recyclerViewNotifications = findViewById(R.id.recyclerViewNotifications);
        progressBarNotifications = findViewById(R.id.progressBarNotifications);
        textViewNoNotifications = findViewById(R.id.textViewNoNotifications);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(this, notificationList);
        recyclerViewNotifications.setAdapter(notificationAdapter);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(NotificationsActivity.this, HomeActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_report) {
                startActivity(new Intent(NotificationsActivity.this, ReportProblemActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_my_reports) {
                startActivity(new Intent(NotificationsActivity.this, MyReportsActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_chat) {
                startActivity(new Intent(NotificationsActivity.this, CommunityChatActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(NotificationsActivity.this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });

        //bottomNavigationView.setSelectedItemId(R.id.nav_notifications); // Esta línea generalmente se quita aquí
        // o se selecciona un item principal si esta actividad no es un destino directo de BottomNav

        // Cargar notificaciones inicialmente
        loadNotifications();
    }

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
        // Obtener el ID de usuario local desde SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String currentUserId = sharedPref.getString("current_user_id", null);

        if (currentUserId == null) {
            Toast.makeText(this, "Debes iniciar sesión para ver tus notificaciones.", Toast.LENGTH_SHORT).show();
            textViewNoNotifications.setText("Inicia sesión para ver tus notificaciones.");
            textViewNoNotifications.setVisibility(View.VISIBLE);
            recyclerViewNotifications.setVisibility(View.GONE);
            progressBarNotifications.setVisibility(View.GONE);
            return;
        }

        progressBarNotifications.setVisibility(View.VISIBLE);
        textViewNoNotifications.setVisibility(View.GONE);
        recyclerViewNotifications.setVisibility(View.GONE);

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
        } else {
            textViewNoNotifications.setVisibility(View.GONE);
            recyclerViewNotifications.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "Notificaciones cargadas desde DBLocal: " + notificationList.size());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications(); // Recargar notificaciones cada vez que la actividad se vuelve visible
    }
}