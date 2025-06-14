package com.example.miprimeraaplicacion;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu; // Importar para el menú de la Toolbar
import android.view.MenuItem;
import android.view.View;
// import android.view.ViewGroup; // Ya no es necesario si NotificationAdapter es externo
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
        setContentView(R.layout.activity_notifications); // Asumiendo que el XML se llama activity_notifications.xml

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Habilita el botón de regreso
            getSupportActionBar().setTitle("Notificaciones"); // Título para la Toolbar
        }

        recyclerViewNotifications = findViewById(R.id.recyclerViewNotifications);
        progressBarNotifications = findViewById(R.id.progressBarNotifications);
        textViewNoNotifications = findViewById(R.id.textViewNoNotifications);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));
        notificationList = new ArrayList<>();
        // Ahora se usa el NotificationAdapter externo
        notificationAdapter = new NotificationAdapter(this, notificationList);
        recyclerViewNotifications.setAdapter(notificationAdapter);

        // Configurar la navegación inferior (SOLO LOS 5 ITEMS PRINCIPALES)
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> { // Usando lambda
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
        // Como esta es la actividad de Notificaciones, no se seleccionará a sí misma en la BottomNavigationView
        // Si llegas a NotificationsActivity desde un BottomNavigationView en otra pantalla,
        // la BottomNavigationView en esta pantalla no necesita tener ningún elemento seleccionado por defecto,
        // o podrías elegir uno de los 5 principales si lo consideras apropiado (ej. nav_home).
        // Por simplicidad, y dado que es la actividad de destino de un icono de Toolbar, no se establecerá un item seleccionado aquí.
        // bottomNavigationView.setSelectedItemId(R.id.nav_notifications); // Esta línea se debe quitar o cambiar

        // Iniciar la escucha de notificaciones en tiempo real
        startListeningForNotifications();
    }

    // *** NUEVOS MÉTODOS PARA EL MENÚ DE LA TOOLBAR (solo para Configuración en esta actividad) ***
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Infla el menú de la Toolbar. Solo queremos "Configuración" aquí.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu); // Asegúrate de tener res/menu/toolbar_menu.xml
        // Puedes ocultar el ítem de notificaciones si quieres, ya que ya estás en la actividad de notificaciones.
        MenuItem notificationsItem = menu.findItem(R.id.action_notifications);
        if (notificationsItem != null) {
            notificationsItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) { // Usamos action_settings de toolbar_menu.xml
            startActivity(new Intent(NotificationsActivity.this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // *** FIN DE NUEVOS MÉTODOS ***

    // Método para manejar el botón de regreso de la Toolbar
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Cierra esta actividad y regresa a la anterior en la pila
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
                    if (snapshots != null) { // Verificar si snapshots no es null
                        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                            Notification notification = doc.toObject(Notification.class);
                            if (notification != null) {
                                notification.setId(doc.getId()); // Es importante guardar el ID del documento
                                newNotifications.add(notification);
                            }
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

    // ==========================================================================================
    // AVISO IMPORTANTE: LAS CLASES 'NotificationAdapter' y 'Notification' QUE ESTABAN AQUÍ HAN SIDO
    // ELIMINADAS. DEBEN EXISTIR COMO ARCHIVOS SEPARADOS EN EL MISMO PAQUETE.
    // LA PRESENCIA DE DEFINICIONES ANIDADAS E INCOMPLETAS AQUÍ CAUSA ERRORES.
    // ==========================================================================================
}