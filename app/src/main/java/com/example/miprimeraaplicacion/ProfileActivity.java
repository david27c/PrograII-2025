package com.example.miprimeraaplicacion;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context; // Importar Context para SharedPreferences
import android.content.Intent;
import android.content.SharedPreferences; // Importar SharedPreferences
import android.net.Uri;
import android.os.Bundle;
import android.util.Log; // Importar Log para depuración
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "ProfileActivity"; // Para mensajes de Log

    private FirebaseAuth mAuth; // Mantener para futuras conexiones Firebase
    private DBFirebase dbFirebase; // Mantener para futuras conexiones Firebase
    private DBLocal dbLocal; // Necesario para obtener datos de perfil localmente

    private CircleImageView imageViewProfile;
    private TextView textViewUsername, textViewEmail, textViewPhone, textViewAddress, textViewReportCount;
    private Button buttonEditProfile, buttonLogout;
    private ProgressBar progressBarProfile;
    private BottomNavigationView bottomNavigationView;

    private Uri imageUri;

    // Declarar userIdToUse a nivel de clase para que sea accesible en loadUserProfile
    private String currentUserId;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        dbFirebase = new DBFirebase(this);
        dbLocal = new DBLocal(this); // Inicializar DBLocal

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mi Perfil");
        }

        imageViewProfile = findViewById(R.id.imageViewProfile);
        textViewUsername = findViewById(R.id.textViewUsername);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewPhone = findViewById(R.id.textViewPhone);
        textViewAddress = findViewById(R.id.textViewAddress);
        textViewReportCount = findViewById(R.id.textViewReportCount);
        buttonEditProfile = findViewById(R.id.buttonEditProfile);
        buttonLogout = findViewById(R.id.buttonLogout);
        progressBarProfile = findViewById(R.id.progressBarProfile);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // --- INICIO DE LA LÓGICA DE VERIFICACIÓN DE SESIÓN (igual que en HomeActivity/MyReportsActivity) ---
        // Obtener el ID de usuario local
        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String currentUserIdLocal = sharedPref.getString("current_user_id", null);

        // Obtener el usuario de Firebase (puede ser null si Firebase no está conectado/logueado)
        FirebaseUser currentUserFirebase = mAuth.getCurrentUser();

        // Determinar qué ID de usuario usar
        if (currentUserFirebase != null) {
            currentUserId = currentUserFirebase.getUid(); // Priorizar Firebase si está logueado
        } else if (currentUserIdLocal != null) {
            currentUserId = currentUserIdLocal; // Si no hay Firebase, pero sí local, usar el local
        } else {
            // Si no hay ningún usuario (ni Firebase ni local), redirigir a Login
            Toast.makeText(this, "Necesitas iniciar sesión para ver tu perfil.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return; // Terminar onCreate aquí para evitar más ejecución
        }
        // --- FIN DE LA LÓGICA DE VERIFICACIÓN DE SESIÓN ---


        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_report) {
                startActivity(new Intent(ProfileActivity.this, ReportProblemActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_my_reports) {
                startActivity(new Intent(ProfileActivity.this, MyReportsActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_chat) {
                startActivity(new Intent(ProfileActivity.this, CommunityChatActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true; // Ya estás en Perfil
            }
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        // Cargar el perfil del usuario después de haber determinado currentUserId
        loadUserProfile();

        buttonEditProfile.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Navegar a la pantalla de edición de perfil", Toast.LENGTH_SHORT).show();
            // Implementar la navegación a tu EditProfileActivity aquí
            // Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            // startActivity(intent);
        });

        buttonLogout.setOnClickListener(v -> signOutUser());
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
            startActivity(new Intent(ProfileActivity.this, NotificationsActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(ProfileActivity.this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadUserProfile() {
        // currentUserId ya se ha determinado en onCreate()
        if (currentUserId == null) {
            // Esto no debería ocurrir si onCreate() manejó bien la redirección
            Toast.makeText(this, "Error: No se pudo determinar el ID de usuario.", Toast.LENGTH_SHORT).show();
            progressBarProfile.setVisibility(View.GONE);
            return;
        }

        progressBarProfile.setVisibility(View.VISIBLE);

        // Primero, intentar cargar de Firebase si hay un currentUserFirebase o si Firebase está activo
        FirebaseUser currentUserFirebase = mAuth.getCurrentUser();
        if (currentUserFirebase != null) {
            dbFirebase.obtenerDatosDeUsuario(currentUserId, new DBFirebase.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    progressBarProfile.setVisibility(View.GONE);
                    if (user != null) {
                        // Si se obtienen datos de Firebase, mostrarlos
                        displayUserProfile(user);
                    } else {
                        // Si no hay datos en Firebase, intentar obtener de DBLocal
                        Toast.makeText(ProfileActivity.this, "Perfil no encontrado en la nube. Cargando datos locales.", Toast.LENGTH_SHORT).show();
                        loadUserProfileFromLocalDb(currentUserId);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    // Si falla la carga de Firebase, intentar desde DBLocal
                    Log.e(TAG, "Error al cargar perfil desde Firebase: " + e.getMessage());
                    Toast.makeText(ProfileActivity.this, "Error al cargar perfil de la nube. Cargando datos locales.", Toast.LENGTH_LONG).show();
                    loadUserProfileFromLocalDb(currentUserId);
                }
            });
        } else {
            // Si no hay currentUserFirebase (por problemas de conexión o no logueado con Firebase),
            // cargar directamente desde DBLocal.
            loadUserProfileFromLocalDb(currentUserId);
        }
    }

    private void loadUserProfileFromLocalDb(String userId) {
        // En tu DBLocal actual, los métodos loginUser y registerUser solo manejan email y password.
        // No hay un método para obtener un objeto 'User' completo con username, phone, address, etc.
        // Por ahora, solo podemos mostrar el email del usuario local.
        // PARA MOSTRAR TODOS LOS DATOS, NECESITAS ACTUALIZAR DBLocal y la clase User.
        User localUser = dbLocal.getUserProfile(userId); // <-- ESTE MÉTODO DEBE EXISTIR EN DBLocal

        progressBarProfile.setVisibility(View.GONE);
        if (localUser != null) {
            displayUserProfile(localUser);
        } else {
            // Si no hay datos en DBLocal para el userId (puede ser que solo se guardó userId y email en Login)
            // Mostrar un mensaje de que los datos no están disponibles o mostrar el email.
            textViewUsername.setText("Usuario: N/A");
            textViewEmail.setText("Correo: " + dbLocal.getUserEmail(userId)); // Necesitas un método para obtener solo el email por ID
            textViewPhone.setText("Teléfono: N/A");
            textViewAddress.setText("Dirección: N/A");
            textViewReportCount.setText("Reportes enviados: N/A");
            imageViewProfile.setImageResource(R.drawable.ic_default_profile);
            Toast.makeText(this, "No se encontraron datos de perfil completos localmente.", Toast.LENGTH_SHORT).show();
        }
    }

    // Método auxiliar para mostrar los datos del perfil
    private void displayUserProfile(User user) {
        // Se asume que la clase User tiene todos estos getters
        textViewUsername.setText("Usuario: " + (user.getUsername() != null ? user.getUsername() : "N/A"));
        textViewEmail.setText("Correo: " + (user.getEmail() != null ? user.getEmail() : "N/A"));
        textViewPhone.setText("Teléfono: " + (user.getPhone() != null ? user.getPhone() : "N/A"));
        textViewAddress.setText("Dirección: " + (user.getAddress() != null ? user.getAddress() : "N/A"));
        textViewReportCount.setText("Reportes enviados: " + user.getReportsCount()); // Ajusta si User no tiene este campo

        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Picasso.get().load(user.getProfileImageUrl())
                    .placeholder(R.drawable.ic_default_profile)
                    .error(R.drawable.ic_default_profile)
                    .into(imageViewProfile);
        } else {
            imageViewProfile.setImageResource(R.drawable.ic_default_profile);
        }
    }


    private void signOutUser() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que quieres cerrar tu sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Si se cierra sesión, limpiar el userId local de SharedPreferences
                    SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.remove("current_user_id");
                    editor.apply();

                    // Intentar cerrar sesión de Firebase también, si estaba logueado
                    if (mAuth.getCurrentUser() != null) {
                        mAuth.signOut();
                    }

                    Toast.makeText(ProfileActivity.this, "Sesión cerrada.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}