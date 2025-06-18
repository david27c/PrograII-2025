package com.example.miprimeraaplicacion;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG_PROFILE = "ProfileActivityDebug"; // Nueva etiqueta para logs
    private static final int PICK_IMAGE_REQUEST = 1;

    private DBLocal dbLocal; // Declaración para la base de datos local

    private CircleImageView imageViewProfile;
    private TextView textViewUsername, textViewEmail, textViewPhone, textViewAddress, textViewReportCount;
    private Button buttonEditProfile, buttonLogout;
    private ProgressBar progressBarProfile;
    private BottomNavigationView bottomNavigationView;

    private Uri imageUri; // Mantener por si se usa para selección de imagen local
    private String currentUserId; // Para almacenar el ID del usuario logueado localmente

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG_PROFILE, "onCreate() de ProfileActivity: Inicio."); // LOG
        setContentView(R.layout.activity_profile);
        Log.d(TAG_PROFILE, "onCreate(): Layout activity_profile establecido."); // LOG

        dbLocal = new DBLocal(this); // Inicializar DBLocal
        Log.d(TAG_PROFILE, "onCreate(): DBLocal inicializado."); // LOG

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mi Perfil");
        }
        Log.d(TAG_PROFILE, "onCreate(): Toolbar configurada."); // LOG


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
        Log.d(TAG_PROFILE, "onCreate(): Vistas principales inicializadas (findViewById)."); // LOG


        // --- LÓGICA DE VERIFICACIÓN DE SESIÓN (CORREGIDA PARA USAR DBLocal) ---
        currentUserId = dbLocal.getLoggedInUserId(this); // Obtener el ID de usuario logueado desde DBLocal
        Log.d(TAG_PROFILE, "onCreate(): ID de usuario recuperado de DBLocal: " + currentUserId); // LOG

        if (currentUserId == null || currentUserId.isEmpty()) { // Verificación más robusta
            Log.d(TAG_PROFILE, "onCreate(): No hay usuario logueado o ID vacío. Redirigiendo a LoginActivity."); // LOG
            Toast.makeText(this, "Necesitas iniciar sesión para ver tu perfil.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            // Flags para limpiar el stack de actividades y asegurar que LoginActivity sea la única en el stack
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Finalizar ProfileActivity para que el usuario no pueda volver atrás
            return; // Terminar onCreate aquí para evitar más ejecución
        }
        // --- FIN DE LA LÓGICA DE VERIFICACIÓN DE SESIÓN ---


        bottomNavigationView.setOnItemSelectedListener(item -> { // Usar setOnItemSelectedListener para versiones recientes
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
                finish();
                Log.d(TAG_PROFILE, "BottomNav: Redirigiendo a HomeActivity."); // LOG
                return true;
            } else if (itemId == R.id.nav_report) {
                startActivity(new Intent(ProfileActivity.this, ReportProblemActivity.class));
                finish();
                Log.d(TAG_PROFILE, "BottomNav: Redirigiendo a ReportProblemActivity."); // LOG
                return true;
            } else if (itemId == R.id.nav_my_reports) {
                startActivity(new Intent(ProfileActivity.this, MyReportsActivity.class));
                finish();
                Log.d(TAG_PROFILE, "BottomNav: Redirigiendo a MyReportsActivity."); // LOG
                return true;
            } else if (itemId == R.id.nav_chat) {
                startActivity(new Intent(ProfileActivity.this, CommunityChatActivity.class));
                finish();
                Log.d(TAG_PROFILE, "BottomNav: Redirigiendo a CommunityChatActivity."); // LOG
                return true;
            } else if (itemId == R.id.nav_profile) {
                Log.d(TAG_PROFILE, "BottomNav: Ya en ProfileActivity."); // LOG
                return true; // Ya estás en Perfil
            }
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.nav_profile); // Seleccionar el ítem de perfil en el BottomNavigationView
        Log.d(TAG_PROFILE, "onCreate(): BottomNavigationView configurado."); // LOG


        // Cargar el perfil del usuario desde DBLocal
        loadUserProfileFromLocalDb();
        Log.d(TAG_PROFILE, "onCreate(): Método loadUserProfileFromLocalDb() llamado."); // LOG


        buttonEditProfile.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Navegar a la pantalla de edición de perfil", Toast.LENGTH_SHORT).show();
            // Implementar la navegación a tu EditProfileActivity aquí
            // Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            // startActivity(intent);
            Log.d(TAG_PROFILE, "onClick: Botón Editar Perfil presionado."); // LOG
        });

        buttonLogout.setOnClickListener(v -> {
            signOutUser();
            Log.d(TAG_PROFILE, "onClick: Botón Cerrar Sesión presionado."); // LOG
        });

        Log.d(TAG_PROFILE, "onCreate() de ProfileActivity: Fin."); // LOG
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

    private void loadUserProfileFromLocalDb() {
        progressBarProfile.setVisibility(View.VISIBLE);

        // Obtener el perfil del usuario de DBLocal usando el currentUserId
        User user = dbLocal.getUserProfile(currentUserId);

        progressBarProfile.setVisibility(View.GONE);

        if (user != null) {
            // Asigna los datos a los TextViews
            textViewUsername.setText("Usuario: " + (user.getUsername() != null ? user.getUsername() : "N/A"));
            textViewEmail.setText("Correo: " + (user.getEmail() != null ? user.getEmail() : "N/A"));
            textViewPhone.setText("Teléfono: " + (user.getPhone() != null ? user.getPhone() : "N/A"));
            textViewAddress.setText("Dirección: " + (user.getAddress() != null ? user.getAddress() : "N/A"));
            textViewReportCount.setText("Reportes enviados: " + user.getReportsCount());

            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                try {
                    // Intenta cargar como URI local (file:// o content://)
                    Uri profileUri = Uri.parse(user.getProfileImageUrl());
                    Picasso.get().load(profileUri)
                            .placeholder(R.drawable.ic_default_profile) // Placeholder mientras carga
                            .error(R.drawable.ic_default_profile)     // Imagen si hay error al cargar
                            .into(imageViewProfile);
                } catch (Exception e) {
                    Log.e(TAG, "Error al cargar imagen de perfil local: " + e.getMessage());
                    imageViewProfile.setImageResource(R.drawable.ic_default_profile); // Si hay un error, usa la imagen por defecto
                }
            } else {
                imageViewProfile.setImageResource(R.drawable.ic_default_profile); // Si no hay URL, usa la imagen por defecto
            }
            Log.d(TAG, "Perfil cargado desde DBLocal para usuario: " + user.getUsername());

        } else {
            // Manejar caso donde no se encuentra el usuario en DBLocal (aunque esto no debería pasar si currentUserId está bien)
            Toast.makeText(this, "No se encontraron datos de perfil localmente para este usuario.", Toast.LENGTH_LONG).show();
            Log.w(TAG, "No se encontró perfil para userId: " + currentUserId + " en DBLocal.");
            // Opcional: limpiar SharedPreferences y redirigir a Login si el usuario no existe localmente
            signOutUser();
        }
    }

    private void signOutUser() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que quieres cerrar tu sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Limpiar el userId local de SharedPreferences
                    SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.remove("current_user_id"); // Eliminar el ID de usuario
                    editor.apply();

                    // No hay mAuth.signOut() porque ya no usamos Firebase Auth

                    Toast.makeText(ProfileActivity.this, "Sesión cerrada.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    // Flags para limpiar el stack de actividades y asegurar que LoginActivity sea la única en el stack
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); // Finalizar ProfileActivity
                })
                .setNegativeButton("No", null)
                .show();
    }
}