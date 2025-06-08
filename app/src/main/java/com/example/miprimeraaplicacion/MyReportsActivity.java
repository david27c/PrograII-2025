package com.example.miprimeraaplicacion;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
    private Spinner spinnerStatusFilter;
    private BottomNavigationView bottomNavigationView;

    private FirebaseAuth mAuth;
    private DBFirebase dbFirebase; // Usamos DBFirebase
    private DBLocal dbLocal; // Podemos seguir usando DBLocal para backup

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
        getSupportActionBar().setTitle("Mis Denuncias");

        recyclerViewMyReports = findViewById(R.id.recyclerViewMyReports);
        progressBarMyReports = findViewById(R.id.progressBarMyReports);
        textViewNoReports = findViewById(R.id.textViewNoReports);
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        myDenunciaList = new ArrayList<>();
        denunciaAdapter = new DenunciaAdapter(this, myDenunciaList); // Asegúrate de que este adaptador sea el DenunciaAdapter
        recyclerViewMyReports.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMyReports.setAdapter(denunciaAdapter);

        // Configurar Spinner para filtrar por estado
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.report_status_filter_options, android.R.layout.simple_spinner_item);
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

        // Configurar la navegación inferior
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
                    return true; // Ya estamos en MyReportsActivity
                } else if (itemId == R.id.nav_chat) {
                    startActivity(new Intent(MyReportsActivity.this, CommunityChatActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(MyReportsActivity.this, ProfileActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_notifications) {
                    startActivity(new Intent(MyReportsActivity.this, NotificationsActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_settings) {
                    startActivity(new Intent(MyReportsActivity.this, SettingsActivity.class));
                    finish();
                    return true;
                }
                return false;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.nav_my_reports);

        // Cargar las denuncias al iniciar la actividad
        loadMyReports("Todos"); // Cargar todas las denuncias por defecto
    }

    private void loadMyReports(String statusFilter) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
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

        // Primero, intentar cargar de Firebase
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

    // Adaptador de MyReportsActivity ya no es necesario, usamos DenunciaAdapter

    // Asegúrate de que el archivo `arrays.xml` tenga este array:
    // <resources>
    //     <string-array name="report_status_filter_options">
    //         <item>Todos</item>
    //         <item>Pendiente</item>
    //         <item>En Proceso</item>
    //         <item>Resuelto</item>
    //     </string-array>
    // </resources>

    // Método para manejar la actualización de una denuncia (ej. cambio de estado)
    public void updateReportStatus(Denuncia denuncia, String newStatus) {
        denuncia.setEstado(newStatus);
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