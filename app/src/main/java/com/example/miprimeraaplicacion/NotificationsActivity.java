package com.example.miprimeraaplicacion;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private RecyclerView recyclerViewNotifications;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;

    private ProgressBar progressBarNotifications;
    private TextView textViewNoNotifications;
    private BottomNavigationView bottomNavigationView;

    private ListenerRegistration notificationsListener; // Listener para notificaciones en tiempo real

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerViewNotifications = findViewById(R.id.recyclerViewNotifications);
        progressBarNotifications = findViewById(R.id.progressBarNotifications);
        textViewNoNotifications = findViewById(R.id.textViewNoNotifications);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(this, notificationList);
        recyclerViewNotifications.setAdapter(notificationAdapter);

        // Configurar la navegación inferior
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
                } else if (itemId == R.id.nav_notifications) {
                    // Ya estamos en NotificationsActivity
                    return true;
                } else if (itemId == R.id.nav_settings) {
                    startActivity(new Intent(NotificationsActivity.this, SettingsActivity.class));
                    finish();
                    return true;
                }
                return false;
            }
        });
        // Asegurarse de que el ítem "Notificaciones" esté seleccionado al inicio
        bottomNavigationView.setSelectedItemId(R.id.nav_notifications);

        // Iniciar la escucha de notificaciones en tiempo real
        startListeningForNotifications();
    }

    // Método para manejar el botón de regreso de la Toolbar
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void startListeningForNotifications() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesión para ver tus notificaciones.", Toast.LENGTH_SHORT).show();
            textViewNoNotifications.setText("Inicia sesión para ver tus notificaciones.");
            textViewNoNotifications.setVisibility(View.VISIBLE);
            recyclerViewNotifications.setVisibility(View.GONE);
            return;
        }

        progressBarNotifications.setVisibility(View.VISIBLE);
        textViewNoNotifications.setVisibility(View.GONE);
        recyclerViewNotifications.setVisibility(View.GONE);

        // Escuchar notificaciones en tiempo real para el usuario actual
        // Suponiendo que tienes una colección "notifications" y cada documento tiene un "userId"
        notificationsListener = db.collection("users").document(currentUser.getUid())
                .collection("notifications") // Subcolección de notificaciones para cada usuario
                .orderBy("timestamp", Query.Direction.DESCENDING) // Ordenar por las más recientes
                .addSnapshotListener((snapshots, e) -> {
                    progressBarNotifications.setVisibility(View.GONE);
                    if (e != null) {
                        Toast.makeText(NotificationsActivity.this, "Error al cargar notificaciones: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        textViewNoNotifications.setText("Error al cargar notificaciones.");
                        textViewNoNotifications.setVisibility(View.VISIBLE);
                        return;
                    }

                    List<Notification> newNotifications = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                        Notification notification = doc.toObject(Notification.class);
                        if (notification != null) {
                            notification.setId(doc.getId()); // Es importante guardar el ID del documento
                            newNotifications.add(notification);
                        }
                    }
                    notificationAdapter.updateNotifications(newNotifications);

                    if (newNotifications.isEmpty()) {
                        textViewNoNotifications.setVisibility(View.VISIBLE);
                        recyclerViewNotifications.setVisibility(View.GONE);
                    } else {
                        textViewNoNotifications.setVisibility(View.GONE);
                        recyclerViewNotifications.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (notificationsListener != null) {
            notificationsListener.remove(); // Detener la escucha al salir de la actividad
        }
    }

    private class NotificationAdapter extends RecyclerView.Adapter {
        public NotificationAdapter(NotificationsActivity notificationsActivity, List<Notification> notificationList) {
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }

        public void updateNotifications(List<Notification> newNotifications) {
        }
    }

    private class Notification {
        public void setId(String id) {
            
        }
    }
}