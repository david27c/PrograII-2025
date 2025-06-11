package com.example.miprimeraaplicacion;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu; // Importar para el menú de la Toolbar
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MyReportsActivity extends AppCompatActivity {

    private static final String TAG = "MyReportsActivity";

    private RecyclerView recyclerViewMyReports;
    private DenunciaAdapter denunciaAdapter;
    private List<Denuncia> myDenunciaList;
    private ProgressBar progressBarMyReports;
    private TextView textViewNoReports;
    private Spinner spinnerStatusFilter; // Se usa este nombre en el Java
    private BottomNavigationView bottomNavigationView;

    private FirebaseAuth mAuth;
    private DBFirebase dbFirebase;
    private DBLocal dbLocal;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reports);

        mAuth = FirebaseAuth.getInstance();
        dbFirebase = new DBFirebase(this);
        dbLocal = new DBLocal(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Mis Denuncias");
        }

        recyclerViewMyReports = findViewById(R.id.recyclerViewMyReports);
        progressBarMyReports = findViewById(R.id.progressBarMyReports);
        textViewNoReports = findViewById(R.id.textViewNoReports);
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter); // ID corregido en el XML
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        myDenunciaList = new ArrayList<>();
        denunciaAdapter = new DenunciaAdapter(this, myDenunciaList);
        recyclerViewMyReports.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMyReports.setAdapter(denunciaAdapter);

        // Configurar Spinner para filtrar por estado
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.report_status_filter_options, android.R.layout.simple_spinner_item); // Asegúrate de que este array existe
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusFilter.setAdapter(spinnerAdapter);

        spinnerStatusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedStatus = parent.getItemAtPosition(position).toString();
                loadMyReports(selectedStatus);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });

        // Configurar la navegación inferior (SOLO LOS 5 ITEMS PRINCIPALES)
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> { // Usando lambda
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(MyReportsActivity.this, HomeActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_report) {
                startActivity(new Intent(MyReportsActivity.this, ReportProblemActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_my_reports) {
                // Ya estamos en MyReportsActivity
                return true;
            } else if (itemId == R.id.nav_chat) {
                startActivity(new Intent(MyReportsActivity.this, CommunityChatActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(MyReportsActivity.this, ProfileActivity.class));
                finish();
                return true;
            }
            // Los ítems nav_notifications y nav_settings han sido movidos a la Toolbar
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.nav_my_reports);

        // Cargar las denuncias al iniciar la actividad
        loadMyReports("Todos"); // Cargar todas las denuncias por defecto
    }

    // *** NUEVOS MÉTODOS PARA EL MENÚ DE LA TOOLBAR (igual que en HomeActivity) ***
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Infla el menú de la Toolbar
        getMenuInflater().inflate(R.menu.toolbar_menu, menu); // Asegúrate de tener res/menu/toolbar_menu.xml
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_notifications) { // Usamos action_notifications de toolbar_menu.xml
            startActivity(new Intent(MyReportsActivity.this, NotificationsActivity.class));
            return true;
        } else if (id == R.id.action_settings) { // Usamos action_settings de toolbar_menu.xml
            startActivity(new Intent(MyReportsActivity.this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // *** FIN DE NUEVOS MÉTODOS ***


    private void loadMyReports(String statusFilter) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // Si estás desactivando Firebase Auth, esta verificación siempre será null.
        // Si no tienes un sistema de autenticación local, esta sección te impedirá cargar reportes.
        // Por ahora, lo dejaré asumiendo que Firebase Auth está o estará activo.
        if (currentUser == null) {
            Toast.makeText(this, "Necesitas iniciar sesión para ver tus reportes.", Toast.LENGTH_LONG).show();
            textViewNoReports.setText("Inicia sesión para ver tus reportes.");
            textViewNoReports.setVisibility(View.VISIBLE);
            recyclerViewMyReports.setVisibility(View.GONE);
            return;
        }

        String userId = currentUser.getUid();
        progressBarMyReports.setVisibility(View.VISIBLE);
        textViewNoReports.setVisibility(View.GONE);
        recyclerViewMyReports.setVisibility(View.GONE);

        // Si se está priorizando DBLocal y desactivando Firebase, esta lógica de carga
        // debe ser ajustada para cargar solo de DBLocal.
        // Por ahora, sigue intentando Firebase primero.
        dbFirebase.obtenerDenunciasDeUsuario(userId, statusFilter, new DBFirebase.ListDenunciasCallback() {
            @Override
            public void onSuccess(List<Denuncia> denuncias) {
                myDenunciaList.clear();
                myDenunciaList.addAll(denuncias);
                denunciaAdapter.notifyDataSetChanged();
                progressBarMyReports.setVisibility(View.GONE);

                if (myDenunciaList.isEmpty()) {
                    textViewNoReports.setText("No tienes reportes en la nube para el filtro seleccionado.");
                    textViewNoReports.setVisibility(View.VISIBLE);
                    recyclerViewMyReports.setVisibility(View.GONE);
                } else {
                    textViewNoReports.setVisibility(View.GONE);
                    recyclerViewMyReports.setVisibility(View.VISIBLE);
                }
                Log.d(TAG, "Denuncias cargadas desde Firebase: " + denuncias.size());
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error al cargar denuncias desde Firebase: " + e.getMessage());
                Toast.makeText(MyReportsActivity.this, "Error al cargar reportes de la nube. Intentando cargar localmente.", Toast.LENGTH_LONG).show();

                // Si falla Firebase, intentar cargar desde la DB local
                List<Denuncia> localDenuncias = dbLocal.obtenerDenunciasPorUsuario(userId);
                myDenunciaList.clear();
                // Filtrar localmente si se aplica un filtro de estado
                if (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.equals("Todos")) {
                    for (Denuncia d : localDenuncias) {
                        if (d.getEstado().equals(statusFilter)) {
                            myDenunciaList.add(d);
                        }
                    }
                } else {
                    myDenunciaList.addAll(localDenuncias);
                }
                denunciaAdapter.notifyDataSetChanged();
                progressBarMyReports.setVisibility(View.GONE);

                if (myDenunciaList.isEmpty()) {
                    textViewNoReports.setText("No tienes reportes (ni en la nube ni localmente) para el filtro seleccionado.");
                    textViewNoReports.setVisibility(View.VISIBLE);
                    recyclerViewMyReports.setVisibility(View.GONE);
                } else {
                    textViewNoReports.setVisibility(View.GONE);
                    recyclerViewMyReports.setVisibility(View.VISIBLE);
                }
                Log.d(TAG, "Denuncias cargadas desde DBLocal: " + myDenunciaList.size());
            }
        });
    }

    // Método para manejar la actualización de una denuncia (ej. cambio de estado)
    public void updateReportStatus(Denuncia denuncia, String newStatus) {
        denuncia.setEstado(newStatus);
        // Si se está priorizando DBLocal y desactivando Firebase, esta lógica de actualización
        // debe ser ajustada para guardar solo en DBLocal.
        dbFirebase.guardarDenuncia(denuncia, null, new DBFirebase.DenunciaCallback() {
            @Override
            public void onSuccess(Denuncia denunciaGuardada) {
                // Actualizar localmente también
                dbLocal.actualizarDenuncia(denunciaGuardada);
                Toast.makeText(MyReportsActivity.this, "Estado actualizado en la nube y localmente.", Toast.LENGTH_SHORT).show();
                // Recargar las denuncias para reflejar el cambio
                loadMyReports(spinnerStatusFilter.getSelectedItem().toString());
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MyReportsActivity.this, "Error al actualizar estado en la nube: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error al actualizar estado: " + e.getMessage());
            }
        });
    }
}