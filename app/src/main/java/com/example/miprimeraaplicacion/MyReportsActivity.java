package com.example.miprimeraaplicacion;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences; // Ya no se usará directamente para obtener userId
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MyReportsActivity extends AppCompatActivity {

    private static final String TAG_MYREPORTS = "MyReportsActivityDebug"; // <--- Etiqueta unificada para depuración

    private RecyclerView recyclerViewMyReports;
    private DenunciaAdapter denunciaAdapter;
    private List<Denuncia> myDenunciaList;
    private ProgressBar progressBarMyReports;
    private TextView textViewNoReports;
    private Spinner spinnerStatusFilter;
    private BottomNavigationView bottomNavigationView;

    private DBLocal dbLocal; // Instancia de DBLocal

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG_MYREPORTS, "onCreate() de MyReportsActivity: Inicio."); // LOG

        setContentView(R.layout.activity_my_reports);
        Log.d(TAG_MYREPORTS, "onCreate(): Layout establecido."); // LOG

        dbLocal = new DBLocal(this); // Inicializar DBLocal al principio del onCreate
        Log.d(TAG_MYREPORTS, "onCreate(): DBLocal inicializado."); // LOG

        // --- LÓGICA DE VERIFICACIÓN DE SESIÓN CON DBLocal ---
        // Usamos el método centralizado en DBLocal para obtener el ID de usuario.
        String currentUserId = dbLocal.getLoggedInUserId(this);
        Log.d(TAG_MYREPORTS, "onCreate(): ID de usuario recuperado de DBLocal: " + currentUserId); // LOG

        // Si NO hay usuario logueado según DBLocal, redirigir a LoginActivity
        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.d(TAG_MYREPORTS, "onCreate(): No hay usuario logueado. Redirigiendo a LoginActivity."); // LOG
            Toast.makeText(this, "Debes iniciar sesión para ver tus reportes.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(MyReportsActivity.this, LoginActivity.class);
            // Limpia la pila de actividades para que el usuario no pueda volver a MyReportsActivity con el botón de atrás.
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Finaliza esta MyReportsActivity
            return; // Terminar onCreate aquí para evitar que se ejecute el resto del código
        }
        // --- FIN DE LA LÓGICA DE VERIFICACIÓN DE SESIÓN ---

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Mis Denuncias");
        }
        Log.d(TAG_MYREPORTS, "onCreate(): Toolbar inicializada."); // LOG

        recyclerViewMyReports = findViewById(R.id.recyclerViewMyReports);
        progressBarMyReports = findViewById(R.id.progressBarMyReports);
        textViewNoReports = findViewById(R.id.textViewNoReports);
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        Log.d(TAG_MYREPORTS, "onCreate(): Vistas principales inicializadas (findViewById)."); // LOG


        myDenunciaList = new ArrayList<>();
        denunciaAdapter = new DenunciaAdapter(this, myDenunciaList);
        recyclerViewMyReports.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMyReports.setAdapter(denunciaAdapter);
        Log.d(TAG_MYREPORTS, "onCreate(): RecyclerView y Adapter configurados."); // LOG


        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.report_status_filter_options, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusFilter.setAdapter(spinnerAdapter);
        Log.d(TAG_MYREPORTS, "onCreate(): Spinner de filtro configurado."); // LOG


        spinnerStatusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedStatus = parent.getItemAtPosition(position).toString();
                Log.d(TAG_MYREPORTS, "Spinner: Estado seleccionado: " + selectedStatus); // LOG
                loadMyReports(selectedStatus); // Cargar reportes locales
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });
        Log.d(TAG_MYREPORTS, "onCreate(): Spinner listener configurado."); // LOG


        bottomNavigationView.setOnItemSelectedListener(item -> { // Usa setOnItemSelectedListener para versiones recientes
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(MyReportsActivity.this, HomeActivity.class));
                finish();
                Log.d(TAG_MYREPORTS, "BottomNav: Redirigiendo a HomeActivity."); // LOG
                return true;
            } else if (itemId == R.id.nav_report) {
                startActivity(new Intent(MyReportsActivity.this, ReportProblemActivity.class));
                finish();
                Log.d(TAG_MYREPORTS, "BottomNav: Redirigiendo a ReportProblemActivity."); // LOG
                return true;
            } else if (itemId == R.id.nav_my_reports) {
                Log.d(TAG_MYREPORTS, "BottomNav: Ya en MyReportsActivity."); // LOG
                return true;
            } else if (itemId == R.id.nav_chat) {
                startActivity(new Intent(MyReportsActivity.this, CommunityChatActivity.class));
                finish();
                Log.d(TAG_MYREPORTS, "BottomNav: Redirigiendo a CommunityChatActivity."); // LOG
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(MyReportsActivity.this, ProfileActivity.class));
                finish();
                Log.d(TAG_MYREPORTS, "BottomNav: Redirigiendo a ProfileActivity."); // LOG
                return true;
            }
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.nav_my_reports);
        Log.d(TAG_MYREPORTS, "onCreate(): BottomNavigationView listener y selección inicial configurados."); // LOG


        // Cargar todos los reportes inicialmente después de que todo esté configurado
        loadMyReports("Todos");
        Log.d(TAG_MYREPORTS, "onCreate(): Método loadMyReports(\"Todos\") llamado."); // LOG

        Log.d(TAG_MYREPORTS, "onCreate() de MyReportsActivity: Fin."); // LOG
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_notifications) {
            startActivity(new Intent(MyReportsActivity.this, NotificationsActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(MyReportsActivity.this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadMyReports(String statusFilter) {
        Log.d(TAG_MYREPORTS, "loadMyReports(): Iniciando carga de reportes para filtro: " + statusFilter); // LOG

        // Obtener el ID de usuario local desde DBLocal (CORREGIDO)
        String currentUserId = dbLocal.getLoggedInUserId(this);
        Log.d(TAG_MYREPORTS, "loadMyReports(): ID de usuario para cargar reportes: " + currentUserId); // LOG

        if (currentUserId == null || currentUserId.isEmpty()) { // Añadir .isEmpty()
            Toast.makeText(this, "Necesitas iniciar sesión para ver tus reportes.", Toast.LENGTH_LONG).show();
            textViewNoReports.setText("Inicia sesión para ver tus reportes.");
            textViewNoReports.setVisibility(View.VISIBLE);
            recyclerViewMyReports.setVisibility(View.GONE);
            progressBarMyReports.setVisibility(View.GONE);
            Log.d(TAG_MYREPORTS, "loadMyReports(): Usuario no logueado, deteniendo carga de reportes."); // LOG
            return;
        }

        progressBarMyReports.setVisibility(View.VISIBLE);
        textViewNoReports.setVisibility(View.GONE);
        recyclerViewMyReports.setVisibility(View.GONE);

        List<Denuncia> loadedDenuncias;

        if (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.equals("Todos")) {
            // Filtrar por estado y por usuario
            Log.d(TAG_MYREPORTS, "loadMyReports(): Filtrando por estado '" + statusFilter + "' para usuario " + currentUserId); // LOG
            List<Denuncia> allUserDenuncias = dbLocal.obtenerDenunciasPorUsuario(currentUserId);
            loadedDenuncias = new ArrayList<>();
            for (Denuncia d : allUserDenuncias) {
                if (d.getEstado().equals(statusFilter)) {
                    loadedDenuncias.add(d);
                }
            }
        } else {
            // Cargar todas las denuncias del usuario
            Log.d(TAG_MYREPORTS, "loadMyReports(): Cargando todos los reportes para usuario " + currentUserId); // LOG
            loadedDenuncias = dbLocal.obtenerDenunciasPorUsuario(currentUserId);
        }

        myDenunciaList.clear();
        myDenunciaList.addAll(loadedDenuncias);
        denunciaAdapter.notifyDataSetChanged();
        progressBarMyReports.setVisibility(View.GONE);
        Log.d(TAG_MYREPORTS, "loadMyReports(): Denuncias cargadas y adapter notificado. Total: " + myDenunciaList.size()); // LOG


        if (myDenunciaList.isEmpty()) {
            textViewNoReports.setText("No tienes reportes para el filtro seleccionado.");
            textViewNoReports.setVisibility(View.VISIBLE);
            recyclerViewMyReports.setVisibility(View.GONE);
            Log.d(TAG_MYREPORTS, "loadMyReports(): No hay reportes para el filtro seleccionado. Mostrando mensaje."); // LOG
        } else {
            textViewNoReports.setVisibility(View.GONE);
            recyclerViewMyReports.setVisibility(View.VISIBLE);
            Log.d(TAG_MYREPORTS, "loadMyReports(): Reportes mostrados en RecyclerView."); // LOG
        }
    }

    public void updateReportStatus(Denuncia denuncia, String newStatus) {
        denuncia.setEstado(newStatus); // Actualiza el estado en el objeto Denuncia
        Log.d(TAG_MYREPORTS, "updateReportStatus(): Actualizando estado de denuncia " + denuncia.getId() + " a " + newStatus); // LOG

        // Llamada a DBLocal.actualizarDenuncia que devuelve int
        int rowsAffected = dbLocal.actualizarDenuncia(denuncia);

        if (rowsAffected > 0) {
            Toast.makeText(MyReportsActivity.this, "Estado actualizado localmente.", Toast.LENGTH_SHORT).show();
            Log.d(TAG_MYREPORTS, "updateReportStatus(): Estado actualizado exitosamente."); // LOG
            // Recargar la lista para reflejar el cambio y el filtro actual
            if (spinnerStatusFilter != null && spinnerStatusFilter.getSelectedItem() != null) {
                loadMyReports(spinnerStatusFilter.getSelectedItem().toString());
            } else {
                loadMyReports("Todos"); // Cargar por defecto si el spinner no está listo
            }
        } else {
            Toast.makeText(MyReportsActivity.this, "Error al actualizar estado localmente.", Toast.LENGTH_SHORT).show();
            Log.e(TAG_MYREPORTS, "updateReportStatus(): Error al actualizar estado."); // LOG
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG_MYREPORTS, "onResume() de MyReportsActivity llamado."); // LOG
        // Recargar los reportes en onResume para reflejar cualquier cambio
        // (por ejemplo, si se editó una denuncia en DenunciaDetailActivity)
        if (spinnerStatusFilter != null && spinnerStatusFilter.getSelectedItem() != null) {
            loadMyReports(spinnerStatusFilter.getSelectedItem().toString());
        } else {
            loadMyReports("Todos");
        }
    }
}