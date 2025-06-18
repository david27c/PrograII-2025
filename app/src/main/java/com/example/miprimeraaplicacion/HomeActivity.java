package com.example.miprimeraaplicacion;

import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG_HOME = "HomeActivityDebug"; // <--- Etiqueta para depuración

    private DBLocal dbLocal;

    private RecyclerView recyclerViewReports; // Cambiado de recyclerViewDenuncias para coincidir con tu XML
    private DenunciaAdapter denunciaAdapter;
    private List<Denuncia> denunciaList;

    private ProgressBar progressBar;
    private Spinner spinnerSortBy;
    private FloatingActionButton fabReportProblem;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG_HOME, "onCreate() de HomeActivity: Inicio."); // LOG

        setContentView(R.layout.activity_home);
        Log.d(TAG_HOME, "onCreate(): Layout establecido."); // LOG

        // Inicializar DBLocal al principio del onCreate
        dbLocal = new DBLocal(this);
        Log.d(TAG_HOME, "onCreate(): DBLocal inicializado."); // LOG

        // --- LÓGICA DE VERIFICACIÓN DE SESIÓN CON DBLocal ---
        // Usamos el método centralizado en DBLocal para obtener el ID de usuario.
        String currentUserId = dbLocal.getLoggedInUserId(this);
        Log.d(TAG_HOME, "onCreate(): ID de usuario recuperado de DBLocal: " + currentUserId); // LOG

        // Si NO hay usuario logueado según DBLocal, redirigir a LoginActivity
        if (currentUserId == null || currentUserId.isEmpty()) { // Añadir .isEmpty() por si el ID es una cadena vacía
            Log.d(TAG_HOME, "onCreate(): No hay usuario logueado. Redirigiendo a LoginActivity."); // LOG
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            // Limpia la pila de actividades para que el usuario no pueda volver a HomeActivity con el botón de atrás.
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Finaliza esta HomeActivity
            return; // Terminar onCreate aquí para evitar que se ejecute el resto del código de HomeActivity
        }
        // --- FIN DE LA LÓGICA DE VERIFICACIÓN DE SESIÓN ---

        // Inicialización de la Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Denuncia Ciudadana");
        }
        Log.d(TAG_HOME, "onCreate(): Toolbar inicializada."); // LOG

        // Inicialización de las vistas del layout
        recyclerViewReports = findViewById(R.id.recyclerViewReports); // Asegúrate de que el ID es 'recyclerViewReports'
        progressBar = findViewById(R.id.progressBar);
        spinnerSortBy = findViewById(R.id.spinnerSortBy);
        fabReportProblem = findViewById(R.id.fabReportProblem);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        Log.d(TAG_HOME, "onCreate(): Vistas principales inicializadas (findViewById)."); // LOG

        // Configuración de RecyclerView
        recyclerViewReports.setLayoutManager(new LinearLayoutManager(this));
        denunciaList = new ArrayList<>();
        // Asegúrate de que el constructor de DenunciaAdapter es DenunciaAdapter(Context context, List<Denuncia> list) o DenunciaAdapter(List<Denuncia>, Context).
        // Según tu código anterior, parece que es DenunciaAdapter(Context context, List<Denuncia> list)
        denunciaAdapter = new DenunciaAdapter(this, denunciaList); // Pasando el Context como primer parámetro
        recyclerViewReports.setAdapter(denunciaAdapter);
        Log.d(TAG_HOME, "onCreate(): RecyclerView y Adapter configurados."); // LOG

        // Configuración del Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortBy.setAdapter(adapter);
        Log.d(TAG_HOME, "onCreate(): Spinner configurado."); // LOG

        // Listener para el Floating Action Button
        fabReportProblem.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ReportProblemActivity.class);
            startActivity(intent);
            Log.d(TAG_HOME, "fabReportProblem click: Redirigiendo a ReportProblemActivity."); // LOG
        });
        Log.d(TAG_HOME, "onCreate(): FloatingActionButton listener configurado."); // LOG

        // Listener para BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener(item -> { // Usa setOnItemSelectedListener para versiones recientes
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Toast.makeText(HomeActivity.this, "Estás en Inicio", Toast.LENGTH_SHORT).show();
                Log.d(TAG_HOME, "BottomNav: nav_home seleccionado."); // LOG
                return true;
            } else if (itemId == R.id.nav_report) { // Asumiendo que es nav_report para ReportProblemActivity
                startActivity(new Intent(HomeActivity.this, ReportProblemActivity.class));
                Log.d(TAG_HOME, "BottomNav: Redirigiendo a ReportProblemActivity."); // LOG
                return true;
            } else if (itemId == R.id.nav_my_reports) { // Asumiendo este ID
                startActivity(new Intent(HomeActivity.this, MyReportsActivity.class));
                Log.d(TAG_HOME, "BottomNav: Redirigiendo a MyReportsActivity."); // LOG
                return true;
            } else if (itemId == R.id.nav_chat) { // Asumiendo este ID
                startActivity(new Intent(HomeActivity.this, CommunityChatActivity.class));
                Log.d(TAG_HOME, "BottomNav: Redirigiendo a CommunityChatActivity."); // LOG
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                Log.d(TAG_HOME, "BottomNav: Redirigiendo a ProfileActivity."); // LOG
                return true;
            }
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.nav_home); // Asegura que "Home" esté seleccionado al inicio
        Log.d(TAG_HOME, "onCreate(): BottomNavigationView listener y selección inicial configurados."); // LOG


        // Cargar las denuncias al iniciar la actividad
        loadReports(); // Asegúrate de que este método existe y es correcto
        Log.d(TAG_HOME, "onCreate(): Método loadReports() llamado."); // LOG

        Log.d(TAG_HOME, "onCreate() de HomeActivity: Fin."); // LOG
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
        Log.d(TAG_HOME, "loadReports(): Iniciando carga de denuncias."); // LOG

        // Cargar denuncias desde DBLocal
        List<Denuncia> denuncias = dbLocal.obtenerTodasLasDenuncias();

        progressBar.setVisibility(View.GONE);
        denunciaList.clear();
        denunciaList.addAll(denuncias);
        denunciaAdapter.notifyDataSetChanged();
        Log.d(TAG_HOME, "loadReports(): Denuncias cargadas y adapter notificado. Total: " + denuncias.size()); // LOG

        if (denuncias.isEmpty()) {
            Toast.makeText(HomeActivity.this, "No hay denuncias disponibles localmente.", Toast.LENGTH_SHORT).show();
            Log.d(TAG_HOME, "loadReports(): No hay denuncias disponibles localmente."); // LOG
        } else {
            Log.d(TAG_HOME, "loadReports(): Denuncias cargadas desde DBLocal: " + denuncias.size());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG_HOME, "onResume() de HomeActivity llamado."); // LOG
        // Recargar reportes en onResume para reflejar cambios si se regresa de otra actividad
        loadReports();
    }
}