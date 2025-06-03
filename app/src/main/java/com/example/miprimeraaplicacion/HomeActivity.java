package com.example.miprimeraaplicacion;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private RecyclerView recyclerViewReports;
    // private ReportAdapter reportAdapter; // Lo crearás más adelante
    private List<Report> reportList; // Clase Report la definirás después

    private ProgressBar progressBar;
    private Spinner spinnerSortBy;
    private FloatingActionButton fabReportProblem;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Configura la Toolbar como ActionBar

        recyclerViewReports = findViewById(R.id.recyclerViewReports);
        progressBar = findViewById(R.id.progressBar);
        spinnerSortBy = findViewById(R.id.spinnerSortBy);
        fabReportProblem = findViewById(R.id.fabReportProblem);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        recyclerViewReports.setLayoutManager(new LinearLayoutManager(this));
        reportList = new ArrayList<>();
        // reportAdapter = new ReportAdapter(this, reportList); // Crea tu adaptador más tarde
        // recyclerViewReports.setAdapter(reportAdapter);

        // Configurar la navegación inferior
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    // Ya estamos en HomeActivity, no hacer nada o refrescar
                    return true;
                } else if (itemId == R.id.nav_report) {
                    startActivity(new Intent(HomeActivity.this, ReportProblemActivity.class));
                    return true;
                } else if (itemId == R.id.nav_my_reports) {
                    startActivity(new Intent(HomeActivity.this, MyReportsActivity.class));
                    return true;
                } else if (itemId == R.id.nav_chat) {
                    startActivity(new Intent(HomeActivity.this, CommunityChatActivity.class));
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                    return true;
                } else if (itemId == R.id.nav_notifications) {
                    startActivity(new Intent(HomeActivity.this, NotificationsActivity.class));
                    return true;
                } else if (itemId == R.id.nav_settings) {
                    startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
                    return true;
                }
                return false;
            }
        });

        // Asegurarse de que el ítem "Inicio" esté seleccionado al inicio
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        // Listener para el botón flotante
        fabReportProblem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, ReportProblemActivity.class);
                startActivity(intent);
            }
        });

        loadReports(); // Cargar los reportes al iniciar
    }

    private void loadReports() {
        progressBar.setVisibility(View.VISIBLE);
        // Aquí iría la lógica para cargar reportes de Firestore
        // Por ahora, un Toast y ocultar la barra de progreso
        Toast.makeText(this, "Cargando reportes...", Toast.LENGTH_SHORT).show();

        // Simula la carga de datos
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        // Aquí deberías añadir tus reportes a reportList
                        // reportAdapter.notifyDataSetChanged();
                    }
                },
                2000 // 2 segundos de simulación
        );
    }

    // Clase de ejemplo para un Reporte (la definirás mejor después)
    public static class Report {
        // Campos de tu reporte, por ejemplo:
        public String title;
        public String description;
        public String location;
        public String status;
        // ... otros campos como fecha, URL de imagen, etc.

        public Report(String title, String description, String location, String status) {
            this.title = title;
            this.description = description;
            this.location = location;
            this.status = status;
        }
    }
}