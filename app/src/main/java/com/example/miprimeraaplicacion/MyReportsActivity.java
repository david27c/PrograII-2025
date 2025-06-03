package com.example.miprimeraaplicacion;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;

public class MyReportsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private RecyclerView recyclerViewMyReports;
    private ReportAdapter reportAdapter;
    private List<Report> reportList;

    private ProgressBar progressBarMyReports;
    private TextView textViewNoReports;
    private Spinner spinnerFilterReports;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reports);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerViewMyReports = findViewById(R.id.recyclerViewMyReports);
        progressBarMyReports = findViewById(R.id.progressBarMyReports);
        textViewNoReports = findViewById(R.id.textViewNoReports);
        spinnerFilterReports = findViewById(R.id.spinnerFilterReports);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        recyclerViewMyReports.setLayoutManager(new LinearLayoutManager(this));
        reportList = new ArrayList<>();
        reportAdapter = new ReportAdapter(this, reportList);
        recyclerViewMyReports.setAdapter(reportAdapter);

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
        // Asegurarse de que el ítem "Mis Reportes" esté seleccionado al inicio
        bottomNavigationView.setSelectedItemId(R.id.nav_my_reports);


        // Listener para el Spinner de filtros
        spinnerFilterReports.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadMyReports(); // Recargar reportes cada vez que cambia el filtro
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });

        loadMyReports(); // Cargar los reportes al iniciar la actividad
    }

    private void loadMyReports() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesión para ver tus reportes.", Toast.LENGTH_SHORT).show();
            textViewNoReports.setText("Inicia sesión para ver tus reportes.");
            textViewNoReports.setVisibility(View.VISIBLE);
            recyclerViewMyReports.setVisibility(View.GONE);
            return;
        }

        progressBarMyReports.setVisibility(View.VISIBLE);
        textViewNoReports.setVisibility(View.GONE);
        recyclerViewMyReports.setVisibility(View.GONE);

        String userId = currentUser.getUid();
        Query query = db.collection("reports")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING); // Ordenar por los más recientes

        String selectedFilter = spinnerFilterReports.getSelectedItem().toString();
        if (!selectedFilter.equals("Todos")) {
            // Firestore es sensible a mayúsculas/minúsculas, asegura la coincidencia
            query = query.whereEqualTo("status", selectedFilter);
        }

        query.get()
                .addOnCompleteListener(task -> {
                    progressBarMyReports.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        reportList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Report report = document.toObject(Report.class);
                            report.setId(document.getId()); // Es importante guardar el ID del documento
                            reportList.add(report);
                        }
                        reportAdapter.updateReports(reportList); // Actualizar el adaptador
                        if (reportList.isEmpty()) {
                            textViewNoReports.setVisibility(View.VISIBLE);
                            recyclerViewMyReports.setVisibility(View.GONE);
                        } else {
                            textViewNoReports.setVisibility(View.GONE);
                            recyclerViewMyReports.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(MyReportsActivity.this, "Error al cargar reportes: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        textViewNoReports.setText("Error al cargar reportes.");
                        textViewNoReports.setVisibility(View.VISIBLE);
                        recyclerViewMyReports.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyReports(); // Recargar los reportes cada vez que la actividad se hace visible
    }

    private class ReportAdapter extends RecyclerView.Adapter {
        private ViewGroup parent;
        private int viewType;

        public ReportAdapter(MyReportsActivity myReportsActivity, List<Report> reportList) {
            
        }

        @Contract(pure = true)
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            this.parent = parent;
            this.viewType = viewType;
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }

        public void updateReports(List<Report> reportList) {
            
        }
    }

    private class Report {
        public void setId(String id) {
        }
    }
}