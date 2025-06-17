package com.example.miprimeraaplicacion;

import android.content.Context; // Necesario para SharedPreferences
import android.content.Intent;
import android.content.SharedPreferences; // Necesario para SharedPreferences
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
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
// REMOVIDO: import com.google.firebase.auth.FirebaseAuth;
// REMOVIDO: import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    // REMOVIDO: private FirebaseAuth mAuth;
    private DBLocal dbLocal;
    // REMOVIDO: private DBFirebase dbFirebase; // Ya no se usa Firebase

    private RecyclerView recyclerViewReports;
    private DenunciaAdapter denunciaAdapter;
    private List<Denuncia> denunciaList;

    private ProgressBar progressBar;
    private Spinner spinnerSortBy;
    private FloatingActionButton fabReportProblem;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // REMOVIDO: mAuth = FirebaseAuth.getInstance();

        // --- INICIO DE LA LÓGICA DE VERIFICACIÓN DE SESIÓN (SOLO DBLocal/SharedPreferences) ---
        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String currentUserIdLocal = sharedPref.getString("current_user_id", null);

        // Si NO hay usuario local, entonces redirigir a Login
        if (currentUserIdLocal == null) {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return; // Terminar onCreate aquí para evitar más ejecución
        }
        // --- FIN DE LA LÓGICA DE VERIFICACIÓN DE SESIÓN ---

        dbLocal = new DBLocal(this);
        // REMOVIDO: dbFirebase = new DBFirebase(this); // Ya no se usa Firebase

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Denuncia Ciudadana");
        }

        recyclerViewReports = findViewById(R.id.recyclerViewReports);
        progressBar = findViewById(R.id.progressBar);
        spinnerSortBy = findViewById(R.id.spinnerSortBy);
        fabReportProblem = findViewById(R.id.fabReportProblem);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        recyclerViewReports.setLayoutManager(new LinearLayoutManager(this));
        denunciaList = new ArrayList<>();
        denunciaAdapter = new DenunciaAdapter(this, denunciaList);
        recyclerViewReports.setAdapter(denunciaAdapter);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortBy.setAdapter(adapter);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
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
            }
            return false;
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        fabReportProblem.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ReportProblemActivity.class);
            startActivity(intent);
        });

        loadReports();
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
            startActivity(new Intent(HomeActivity.this, NotificationsActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadReports() {
        progressBar.setVisibility(View.VISIBLE);
        // Cargar denuncias desde DBLocal
        List<Denuncia> denuncias = dbLocal.obtenerTodasLasDenuncias();

        progressBar.setVisibility(View.GONE);
        denunciaList.clear();
        denunciaList.addAll(denuncias);
        denunciaAdapter.notifyDataSetChanged();

        if (denuncias.isEmpty()) {
            Toast.makeText(HomeActivity.this, "No hay denuncias disponibles localmente.", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("HomeActivity", "Denuncias cargadas desde DBLocal: " + denuncias.size());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar reportes en onResume para reflejar cambios si se regresa de otra actividad
        loadReports();
    }
}