package com.example.miprimeraaplicacion;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu; // Importar para el menú de la toolbar
import android.view.MenuItem; // Importar para los ítems del menú de la toolbar
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView; // Usado para mostrar los datos
import android.widget.Toast;

import androidx.annotation.NonNull; // Para onOptionsItemSelected
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser; // Importar FirebaseUser
import com.squareup.picasso.Picasso;

// Estas importaciones ya no son necesarias en ProfileActivity si solo es de visualización,
// ya que la lógica de guardado y actualización se movería a una EditProfileActivity.
// import java.util.HashMap;
// import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1; // Solo si permites cambiar imagen desde aquí

    private FirebaseAuth mAuth;
    private DBFirebase dbFirebase;
    private FirebaseUser currentUser; // Declara FirebaseUser

    private CircleImageView imageViewProfile;
    // Cambiamos de EditText a TextView ya que el XML actual solo los muestra
    private TextView textViewUsername, textViewEmail, textViewPhone, textViewAddress, textViewReportCount;
    // Un solo botón para ir a la edición del perfil
    private Button buttonEditProfile, buttonLogout;
    private ProgressBar progressBarProfile;
    private BottomNavigationView bottomNavigationView;

    private Uri imageUri; // Solo si permites cambiar imagen desde aquí

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        dbFirebase = new DBFirebase(this);
        currentUser = mAuth.getCurrentUser(); // Inicializa currentUser aquí

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mi Perfil");
        }

        imageViewProfile = findViewById(R.id.imageViewProfile);
        // Ahora estos son TextViews según tu activity_profile.xml
        textViewUsername = findViewById(R.id.textViewUsername);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewPhone = findViewById(R.id.textViewPhone);
        textViewAddress = findViewById(R.id.textViewAddress);
        textViewReportCount = findViewById(R.id.textViewReportCount); // TextView para reportes
        buttonEditProfile = findViewById(R.id.buttonEditProfile); // Botón para ir a la edición
        buttonLogout = findViewById(R.id.buttonLogout);
        progressBarProfile = findViewById(R.id.progressBarProfile);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Listener para la BottomNavigationView
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
            // Eliminadas las referencias a nav_notifications y nav_settings de aquí
            // porque ahora están en el menú de la Toolbar.
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        loadUserProfile();

        // El textViewEditProfileImage no existe en tu activity_profile.xml.
        // Si quieres que la imagen sea clickeable para cambiar, asocia el listener directamente a imageViewProfile.
        // Si la edición de la imagen también se hará en una EditProfileActivity separada,
        // entonces este listener no debería ir aquí, sino en esa otra actividad.
        // Por ahora, lo comentaré para evitar un NullPointerException.
        // imageViewProfile.setOnClickListener(v -> openFileChooser());

        // Botón para editar perfil (probablemente ir a otra actividad)
        buttonEditProfile.setOnClickListener(v -> {
            // Aquí deberías iniciar una nueva actividad para editar el perfil
            // Por ejemplo:
            // startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
            Toast.makeText(ProfileActivity.this, "Navegar a la pantalla de edición de perfil", Toast.LENGTH_SHORT).show();
        });

        buttonLogout.setOnClickListener(v -> signOutUser());
    }

    // *** MÉTODOS PARA EL MENÚ DE LA TOOLBAR (Notificaciones y Configuración) ***
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
    // *** FIN DE MÉTODOS DE TOOLBAR ***

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadUserProfile() {
        if (currentUser == null) {
            Toast.makeText(this, "No hay usuario logueado.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
            return;
        }

        progressBarProfile.setVisibility(View.VISIBLE);
        textViewEmail.setText("Correo: " + currentUser.getEmail()); // Mostrar el email del usuario logueado

        dbFirebase.obtenerDatosDeUsuario(currentUser.getUid(), new DBFirebase.UserCallback() {
            @Override
            public void onSuccess(User user) {
                progressBarProfile.setVisibility(View.GONE);
                if (user != null) {
                    textViewUsername.setText(user.getUsername());
                    textViewPhone.setText("Teléfono: " + (user.getPhone() != null ? user.getPhone() : "N/A"));
                    textViewAddress.setText("Dirección: " + (user.getAddress() != null ? user.getAddress() : "N/A"));
                    // Asegúrate de que tu clase User tenga un método getReportsCount()
                    textViewReportCount.setText("Reportes enviados: " + user.getReportsCount());

                    if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                        Picasso.get().load(user.getProfileImageUrl())
                                .placeholder(R.drawable.ic_default_profile)
                                .error(R.drawable.ic_default_profile)
                                .into(imageViewProfile);
                    } else {
                        imageViewProfile.setImageResource(R.drawable.ic_default_profile);
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Datos de perfil no encontrados, inicializando...", Toast.LENGTH_SHORT).show();
                    dbFirebase.inicializarDocumentoUsuario(currentUser.getUid(), currentUser.getEmail(), new DBFirebase.VoidCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(ProfileActivity.this, "Documento de usuario inicializado.", Toast.LENGTH_SHORT).show();
                            loadUserProfile(); // Recargar el perfil después de la inicialización
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

    // Estos métodos (openFileChooser, onActivityResult, saveUserProfile) solo son necesarios si la ProfileActivity
    // permite la edición directa del perfil. Si usas una EditProfileActivity separada, muévelos allí.
    // Como tu activity_profile.xml actual es solo de visualización, los comentamos o deberías moverlos.
    /*
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
            Toast.makeText(this, "Imagen seleccionada, pulsa 'Editar Perfil' para guardar los cambios.", Toast.LENGTH_LONG).show();
        }
    }

    private void saveUserProfile() {
        // Este método ahora debería llamarse desde una actividad de edición de perfil,
        // no desde esta ProfileActivity si esta solo es de visualización.
        // Si mantienes la edición aquí, necesitarás los EditText y el botón de guardar.
        // El código aquí sería el mismo que ya teníamos para actualizar el perfil.
        Toast.makeText(this, "Este método se llamaría desde la pantalla de edición.", Toast.LENGTH_SHORT).show();
    }
    */

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