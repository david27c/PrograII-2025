package com.example.miprimeraaplicacion;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private FirebaseAuth mAuth;
    private DBFirebase dbFirebase; // Usar nuestra clase DBFirebase

    private CircleImageView imageViewProfile;
    private TextView textViewEditProfileImage;
    private EditText editTextUsername, editTextEmail, editTextPhone, editTextAddress;
    private Button buttonSaveProfile, buttonLogout;
    private ProgressBar progressBarProfile;
    private BottomNavigationView bottomNavigationView;

    private Uri imageUri;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        dbFirebase = new DBFirebase(this); // Inicializar DBFirebase

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mi Perfil"); // Título según tu layout
        }

        imageViewProfile = findViewById(R.id.imageViewProfile);
        textViewEditProfileImage = findViewById(R.id.textViewEditProfileImage);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextAddress = findViewById(R.id.editTextAddress);
        buttonSaveProfile = findViewById(R.id.buttonSaveProfile);
        buttonLogout = findViewById(R.id.buttonLogout);
        progressBarProfile = findViewById(R.id.progressBarProfile);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

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
            } else if (itemId == R.id.nav_notifications) {
                startActivity(new Intent(ProfileActivity.this, NotificationsActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(ProfileActivity.this, SettingsActivity.class));
                finish();
                return true;
            }
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        loadUserProfile();

        imageViewProfile.setOnClickListener(v -> openFileChooser());
        textViewEditProfileImage.setOnClickListener(v -> openFileChooser());

        buttonSaveProfile.setOnClickListener(v -> saveUserProfile());

        buttonLogout.setOnClickListener(v -> signOutUser());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadUserProfile() {
        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        String currentUserEmail = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : null;

        if (currentUserId == null) {
            Toast.makeText(this, "No hay usuario logueado.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
            return;
        }

        progressBarProfile.setVisibility(View.VISIBLE);
        editTextEmail.setText(currentUserEmail); // Mostrar el email del usuario logueado

        dbFirebase.obtenerDatosDeUsuario(currentUserId, new DBFirebase.UserCallback() {
            @Override
            public void onSuccess(User user) {
                progressBarProfile.setVisibility(View.GONE);
                if (user != null) {
                    editTextUsername.setText(user.getUsername());
                    editTextPhone.setText(user.getPhone());
                    editTextAddress.setText(user.getAddress());

                    if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                        Picasso.get().load(user.getProfileImageUrl())
                                .placeholder(R.drawable.ic_default_profile) // Placeholder mientras carga
                                .error(R.drawable.ic_default_profile)     // Imagen en caso de error
                                .into(imageViewProfile);
                    } else {
                        // Si no hay URL de imagen, mostrar la imagen por defecto
                        imageViewProfile.setImageResource(R.drawable.ic_default_profile);
                    }
                } else {
                    // Si el documento no existe, inicializarlo
                    Toast.makeText(ProfileActivity.this, "Datos de perfil no encontrados, inicializando...", Toast.LENGTH_SHORT).show();
                    dbFirebase.inicializarDocumentoUsuario(currentUserId, currentUserEmail, new DBFirebase.VoidCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(ProfileActivity.this, "Documento de usuario inicializado.", Toast.LENGTH_SHORT).show();
                            // Puedes recargar el perfil si es necesario, o simplemente ya está inicializado
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(ProfileActivity.this, "Error al inicializar documento de usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                progressBarProfile.setVisibility(View.GONE);
                Toast.makeText(ProfileActivity.this, "Error al cargar el perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            Picasso.get().load(imageUri).into(imageViewProfile);
        }
    }

    private void saveUserProfile() {
        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (currentUserId == null) {
            Toast.makeText(this, "Debes iniciar sesión para guardar tu perfil.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBarProfile.setVisibility(View.VISIBLE);
        String username = editTextUsername.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", username);
        updates.put("phone", phone);
        updates.put("address", address);

        if (imageUri != null) {
            dbFirebase.subirImagenPerfil(imageUri, currentUserId, new DBFirebase.ImageUploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    updates.put("profileImageUrl", imageUrl);
                    dbFirebase.actualizarPerfilUsuario(currentUserId, updates, new DBFirebase.VoidCallback() {
                        @Override
                        public void onSuccess() {
                            progressBarProfile.setVisibility(View.GONE);
                            Toast.makeText(ProfileActivity.this, "Perfil actualizado exitosamente.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            progressBarProfile.setVisibility(View.GONE);
                            Toast.makeText(ProfileActivity.this, "Error al guardar perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    progressBarProfile.setVisibility(View.GONE);
                    Toast.makeText(ProfileActivity.this, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            dbFirebase.actualizarPerfilUsuario(currentUserId, updates, new DBFirebase.VoidCallback() {
                @Override
                public void onSuccess() {
                    progressBarProfile.setVisibility(View.GONE);
                    Toast.makeText(ProfileActivity.this, "Perfil actualizado exitosamente.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    progressBarProfile.setVisibility(View.GONE);
                    Toast.makeText(ProfileActivity.this, "Error al guardar perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void signOutUser() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que quieres cerrar tu sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    mAuth.signOut();
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