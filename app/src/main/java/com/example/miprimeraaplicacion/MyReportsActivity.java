package com.example.miprimeraaplicacion;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        myDenunciaList = new ArrayList<>();
        denunciaAdapter = new DenunciaAdapter(this, myDenunciaList);
        recyclerViewMyReports.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMyReports.setAdapter(denunciaAdapter);

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

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
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
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.nav_my_reports);

        loadMyReports("Todos");
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
        // Obtener el ID de usuario local
        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String currentUserIdLocal = sharedPref.getString("current_user_id", null);

        // Obtener el usuario de Firebase
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Declarar userIdToUse como final (o efectivamente final)
        final String userIdToUse; // <-- CAMBIO AQUÍ: Declarado como final

        // Determinar qué ID de usuario usar
        if (currentUser != null) {
            userIdToUse = currentUser.getUid(); // Si hay usuario Firebase, usar su UID
        } else if (currentUserIdLocal != null) {
            userIdToUse = currentUserIdLocal; // Si no hay Firebase, pero sí local, usar el local
        } else {
            // Si no hay ningún usuario (ni Firebase ni local), mostrar mensaje y salir
            Toast.makeText(this, "Necesitas iniciar sesión para ver tus reportes.", Toast.LENGTH_LONG).show();
            textViewNoReports.setText("Inicia sesión para ver tus reportes.");
            textViewNoReports.setVisibility(View.VISIBLE);
            recyclerViewMyReports.setVisibility(View.GONE);
            progressBarMyReports.setVisibility(View.GONE);
            return;
        }

        progressBarMyReports.setVisibility(View.VISIBLE);
        textViewNoReports.setVisibility(View.GONE);
        recyclerViewMyReports.setVisibility(View.GONE);

        // Intentar cargar desde Firebase si hay un usuario de Firebase
        if (currentUser != null) {
            dbFirebase.obtenerDenunciasDeUsuario(userIdToUse, statusFilter, new DBFirebase.ListDenunciasCallback() {
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
                    loadReportsFromLocalDb(userIdToUse, statusFilter);
                }
            });
        } else {
            // Si no hay usuario de Firebase, cargar directamente desde la DB local
            loadReportsFromLocalDb(userIdToUse, statusFilter);
        }
    }

    private void loadReportsFromLocalDb(String userId, String statusFilter) {
        List<Denuncia> localDenuncias = dbLocal.obtenerDenunciasPorUsuario(userId);
        myDenunciaList.clear();

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
            textViewNoReports.setText("No tienes reportes (solo locales) para el filtro seleccionado.");
            textViewNoReports.setVisibility(View.VISIBLE);
            recyclerViewMyReports.setVisibility(View.GONE);
        } else {
            textViewNoReports.setVisibility(View.GONE);
            recyclerViewMyReports.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "Denuncias cargadas desde DBLocal: " + myDenunciaList.size());
    }

    public void updateReportStatus(Denuncia denuncia, String newStatus) {
        denuncia.setEstado(newStatus);
        // CAMBIO AQUÍ: `dbLocal.actualizarDenuncia` devuelve `int`, no `boolean`.
        int rowsAffected = dbLocal.actualizarDenuncia(denuncia); // <-- CAMBIO DE TIPO Y NOMBRE
        if (rowsAffected > 0) { // <-- CAMBIO DE CONDICIÓN
            Toast.makeText(MyReportsActivity.this, "Estado actualizado localmente.", Toast.LENGTH_SHORT).show();
            // Asegurarse de que spinnerStatusFilter no sea null antes de usarlo.
            if (spinnerStatusFilter != null && spinnerStatusFilter.getSelectedItem() != null) {
                loadMyReports(spinnerStatusFilter.getSelectedItem().toString());
            } else {
                loadMyReports("Todos"); // Cargar por defecto si el spinner no está listo
            }
        } else {
            Toast.makeText(MyReportsActivity.this, "Error al actualizar estado localmente.", Toast.LENGTH_SHORT).show();
        }
    }
}