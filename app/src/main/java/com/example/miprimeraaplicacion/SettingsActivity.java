package com.example.miprimeraaplicacion;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings; // Para abrir los ajustes de permisos
import android.util.Log; // Importar la clase Log
import android.view.Menu; // Importar para el menú de la toolbar
import android.view.MenuItem; // Importar para los ítems del menú de la toolbar
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;


public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    private DBLocal dbLocal;

    // Vistas de la UI - Configuración de Perfil
    private EditText editTextFullName;
    private EditText editTextEmail;
    private EditText editTextUsername;
    private EditText editTextPhoneNumber;
    private EditText editTextCurrentPassword;
    private EditText editTextNewPassword;
    private EditText editTextConfirmNewPassword; // Añadido
    private Button buttonSaveProfileChanges;
    private ProgressBar progressBarProfileChanges;

    // Vistas de la UI - Preferencias de Visibilidad
    private CheckBox checkBoxShowFullName;
    private CheckBox checkBoxShowProfilePhoto;
    private CheckBox checkBoxShowEmail;
    private CheckBox checkBoxShowPhoneNumber;
    private Button buttonSaveVisibilityPreferences;

    // Vistas de la UI - Configuración General
    private Switch switchPushNotifications;
    private LinearLayout layoutLanguageSelector; // Para el selector de idioma
    private TextView textViewSelectedLanguage;
    private TextView textViewAppPermissions;
    private TextView textViewAppVersion; // Añadido
    private TextView textViewCredits; // Añadido
    private Button buttonDeleteAccount;
    private Button buttonLogout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dbLocal = new DBLocal(this);

        // --- INICIALIZACIÓN DE VISTAS - CONFIGURACIÓN DE PERFIL ---
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextCurrentPassword = findViewById(R.id.editTextCurrentPassword);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextConfirmNewPassword = findViewById(R.id.editTextConfirmNewPassword); // Inicializado
        buttonSaveProfileChanges = findViewById(R.id.buttonSaveProfileChanges);
        progressBarProfileChanges = findViewById(R.id.progressBarProfileChanges);

        // --- INICIALIZACIÓN DE VISTAS - PREFERENCIAS DE VISIBILIDAD ---
        checkBoxShowFullName = findViewById(R.id.checkBoxShowFullName);
        checkBoxShowProfilePhoto = findViewById(R.id.checkBoxShowProfilePhoto);
        checkBoxShowEmail = findViewById(R.id.checkBoxShowEmail);
        checkBoxShowPhoneNumber = findViewById(R.id.checkBoxShowPhoneNumber);
        buttonSaveVisibilityPreferences = findViewById(R.id.buttonSaveVisibilityPreferences);

        // --- INICIALIZACIÓN DE VISTAS - CONFIGURACIÓN GENERAL ---
        switchPushNotifications = findViewById(R.id.switchPushNotifications);
        layoutLanguageSelector = findViewById(R.id.layoutLanguageSelector);
        textViewSelectedLanguage = findViewById(R.id.textViewSelectedLanguage);
        textViewAppPermissions = findViewById(R.id.textViewAppPermissions);
        textViewAppVersion = findViewById(R.id.textViewAppVersion); // Inicializado
        textViewCredits = findViewById(R.id.textViewCredits); // Inicializado
        buttonDeleteAccount = findViewById(R.id.buttonDeleteAccount);
        buttonLogout = findViewById(R.id.buttonLogout);


        // --- LISTENERS DE LA UI ---
        buttonLogout.setOnClickListener(v -> signOutUser());
        buttonDeleteAccount.setOnClickListener(v -> deleteUserAccountAndData());
        buttonSaveProfileChanges.setOnClickListener(v -> saveProfileChanges()); // Listener para guardar perfil y contraseña

        // Listener para guardar preferencias de visibilidad
        buttonSaveVisibilityPreferences.setOnClickListener(v -> saveVisibilityPreferences());

        // Listener para notificaciones PUSH
        switchPushNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> saveNotificationPreference(isChecked));

        // Listeners para elementos de Configuración General
        layoutLanguageSelector.setOnClickListener(v -> showLanguageSelectionDialog());
        textViewAppPermissions.setOnClickListener(v -> openAppSettings());
        textViewCredits.setOnClickListener(v -> showCredits());


        // Manejo de la barra de navegación inferior (BottomNavigationView)
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_settings);
            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                Intent intent;
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    intent = new Intent(SettingsActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_denuncias) {
                    // Si DenunciaActivity ya no existe o se ha renombrado, redirige a HomeActivity
                    Toast.makeText(SettingsActivity.this, "La funcionalidad de Denuncias no está disponible o ha sido movida.", Toast.LENGTH_SHORT).show();
                    intent = new Intent(SettingsActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    intent = new Intent(SettingsActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_settings) {
                    return true; // Ya estamos en SettingsActivity
                }
                return false;
            });
        }


        // Configuración de la Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Configuración"); // Título ajustado para coincidir con XML
            // Puedes añadir un botón de retroceso si lo deseas
            // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Log.d(TAG, "onCreate: SettingsActivity creada.");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: SettingsActivity iniciada.");
        loadUserSettings();
        loadVisibilityPreferences(); // Cargar estado de las preferencias de visibilidad
        loadGeneralPreferences(); // Cargar estado de preferencias generales (ej. notificaciones)
    }

    private void loadUserSettings() {
        Log.d(TAG, "loadUserSettings: Iniciando carga de configuración de usuario.");

        String loggedInUserId = dbLocal.getLoggedInUserId();

        if (loggedInUserId != null) {
            // Asumo que DBLocal.getUserData() podría devolver un objeto Usuario o un Map con todos los datos
            // O que tienes métodos específicos como getUserEmail, getUserName, getUserFullName, getUserPhoneNumber
            String userEmail = dbLocal.getUserEmail(loggedInUserId);
            String userName = dbLocal.getUserName(loggedInUserId); // Asumo que este es el nombre de usuario
            String userFullName = dbLocal.getUserFullName(loggedInUserId); // Necesitarías este método en DBLocal
            String userPhoneNumber = dbLocal.getUserPhoneNumber(loggedInUserId); // Necesitarías este método en DBLocal

            if (userEmail != null) {
                editTextEmail.setText(userEmail);
                editTextEmail.setEnabled(false); // Mantenerlo no editable como en el XML
            }
            if (userName != null) {
                editTextUsername.setText(userName);
            }
            if (userFullName != null) {
                editTextFullName.setText(userFullName);
            }
            if (userPhoneNumber != null) {
                editTextPhoneNumber.setText(userPhoneNumber);
            }

            Log.d(TAG, "loadUserSettings: Usuario logueado localmente: " + userEmail);
        } else {
            editTextEmail.setText("No hay usuario logueado");
            editTextUsername.setText("N/A");
            editTextFullName.setText("N/A");
            editTextPhoneNumber.setText("N/A");
            Log.d(TAG, "loadUserSettings: No hay usuario logueado localmente.");
            redirectToLogin();
        }

        // Cargar foto de perfil - si usas un servicio como Picasso o Glide y guardas la URL en DBLocal
        // String profilePhotoUrl = dbLocal.getProfilePhotoUrl(loggedInUserId); // Necesitarías este método
        // if (profilePhotoUrl != null && !profilePhotoUrl.isEmpty()) {
        //     Picasso.get().load(profilePhotoUrl).into(imageViewProfile); // Asegúrate de tener imageViewProfile inicializado
        // } else {
        //     imageViewProfile.setImageResource(R.drawable.ic_default_profile);
        // }
    }

    /**
     * Guarda los cambios en el perfil de usuario (nombre, usuario, teléfono y contraseña).
     */
    private void saveProfileChanges() {
        String fullName = editTextFullName.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String currentPassword = editTextCurrentPassword.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString().trim();
        String confirmNewPassword = editTextConfirmNewPassword.getText().toString().trim();

        String userId = dbLocal.getLoggedInUserId();

        if (userId == null) {
            Toast.makeText(this, "No hay usuario logueado.", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        progressBarProfileChanges.setVisibility(View.VISIBLE);

        boolean profileUpdated = false;
        boolean passwordUpdated = false;

        // Lógica para actualizar nombre, usuario, teléfono
        // Necesitarás un método updateUserProfile en tu DBLocal
        // Ejemplo (asume que existe updateUserProfile en DBLocal que maneja los campos individuales o en un objeto):
        // boolean profileInfoUpdated = dbLocal.updateUserProfile(userId, fullName, username, phoneNumber);
        // if (profileInfoUpdated) {
        //     profileUpdated = true;
        // }

        // Lógica para cambiar la contraseña
        if (!currentPassword.isEmpty() || !newPassword.isEmpty() || !confirmNewPassword.isEmpty()) {
            if (newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
                Toast.makeText(this, "Por favor, complete los campos de nueva contraseña.", Toast.LENGTH_SHORT).show();
                progressBarProfileChanges.setVisibility(View.GONE);
                return;
            }
            if (!newPassword.equals(confirmNewPassword)) {
                Toast.makeText(this, "La nueva contraseña y su confirmación no coinciden.", Toast.LENGTH_SHORT).show();
                progressBarProfileChanges.setVisibility(View.GONE);
                return;
            }
            if (newPassword.length() < 6) {
                Toast.makeText(this, "La nueva contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show();
                progressBarProfileChanges.setVisibility(View.GONE);
                return;
            }

            // Lógica para actualizar contraseña en DBLocal (asume que verifica la currentPassword internamente)
            boolean updated = dbLocal.updateUserPassword(userId, currentPassword, newPassword); // Necesitarías este método en DBLocal que también verifique la contraseña actual
            if (updated) {
                passwordUpdated = true;
                editTextCurrentPassword.setText("");
                editTextNewPassword.setText("");
                editTextConfirmNewPassword.setText("");
            } else {
                Toast.makeText(SettingsActivity.this, "Error al actualizar la contraseña. Contraseña actual incorrecta o error interno.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Falló la actualización de contraseña en DBLocal.");
            }
        }

        progressBarProfileChanges.setVisibility(View.GONE);

        if (profileUpdated && passwordUpdated) {
            Toast.makeText(this, "Perfil y contraseña actualizados exitosamente.", Toast.LENGTH_SHORT).show();
        } else if (profileUpdated) {
            Toast.makeText(this, "Perfil actualizado exitosamente.", Toast.LENGTH_SHORT).show();
        } else if (passwordUpdated) {
            Toast.makeText(this, "Contraseña actualizada exitosamente.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No se realizaron cambios en el perfil.", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Guarda las preferencias de visibilidad del usuario.
     */
    private void saveVisibilityPreferences() {
        SharedPreferences prefs = getSharedPreferences("UserVisibilityPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("show_full_name", checkBoxShowFullName.isChecked());
        editor.putBoolean("show_profile_photo", checkBoxShowProfilePhoto.isChecked());
        editor.putBoolean("show_email", checkBoxShowEmail.isChecked());
        editor.putBoolean("show_phone_number", checkBoxShowPhoneNumber.isChecked());
        editor.apply();

        Toast.makeText(this, "Preferencias de visibilidad guardadas.", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "saveVisibilityPreferences: Preferencias guardadas.");
    }

    /**
     * Carga las preferencias de visibilidad del usuario.
     */
    private void loadVisibilityPreferences() {
        SharedPreferences prefs = getSharedPreferences("UserVisibilityPrefs", MODE_PRIVATE);
        checkBoxShowFullName.setChecked(prefs.getBoolean("show_full_name", false)); // Default false
        checkBoxShowProfilePhoto.setChecked(prefs.getBoolean("show_profile_photo", true)); // Default true
        checkBoxShowEmail.setChecked(prefs.getBoolean("show_email", false)); // Default false
        checkBoxShowPhoneNumber.setChecked(prefs.getBoolean("show_phone_number", false)); // Default false
        Log.d(TAG, "loadVisibilityPreferences: Preferencias cargadas.");
    }

    /**
     * Carga preferencias generales como el estado de las notificaciones PUSH.
     */
    private void loadGeneralPreferences() {
        switchPushNotifications.setChecked(getNotificationPreference());
        // Aquí podrías cargar el idioma seleccionado si lo guardas en SharedPreferences
        // textViewSelectedLanguage.setText(getSavedLanguage());
    }


    private void signOutUser() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que quieres cerrar tu sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    dbLocal.clearLoggedInUserId(); // Cerrar sesión en DBLocal
                    Toast.makeText(SettingsActivity.this, "Sesión cerrada.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "signOutUser: Sesión de usuario local cerrada.");
                    redirectToLogin();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    Log.d(TAG, "signOutUser: Cierre de sesión cancelado.");
                })
                .show();
    }

    private void deleteUserAccountAndData() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Cuenta")
                .setMessage("¿Estás seguro de que quieres eliminar tu cuenta permanentemente? Esta acción no se puede deshacer y se borrarán todos tus datos asociados.")
                .setPositiveButton("Sí", (dialog, which) -> {
                    progressBarProfileChanges.setVisibility(View.VISIBLE);

                    String userIdToDelete = dbLocal.getLoggedInUserId();
                    if (userIdToDelete != null) {
                        boolean deleted = dbLocal.deleteUser(userIdToDelete);
                        progressBarProfileChanges.setVisibility(View.GONE);
                        if (deleted) {
                            dbLocal.clearLoggedInUserId(); // Limpiar sesión local
                            Toast.makeText(SettingsActivity.this, "Cuenta eliminada exitosamente.", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "deleteUserAccountAndData: Cuenta eliminada localmente para ID: " + userIdToDelete);
                            redirectToLogin();
                        } else {
                            Toast.makeText(SettingsActivity.this, "Error al eliminar la cuenta.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "deleteUserAccountAndData: Falló la eliminación de la cuenta en DBLocal para ID: " + userIdToDelete);
                        }
                    } else {
                        progressBarProfileChanges.setVisibility(View.GONE);
                        Toast.makeText(SettingsActivity.this, "No hay usuario logueado para eliminar.", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "deleteUserAccountAndData: Intento de eliminar cuenta sin usuario logueado.");
                        redirectToLogin();
                    }
                })
                .setNegativeButton("No", (dialog, which) -> {
                    Log.d(TAG, "deleteUserAccountAndData: Eliminación de cuenta cancelada.");
                })
                .show();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Métodos para manejar el estado de las preferencias (notificaciones)
    private void saveNotificationPreference(boolean enabled) {
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("notifications_enabled", enabled);
        editor.apply();
        Toast.makeText(this, "Preferencias de notificación guardadas.", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "saveNotificationPreference: Notificaciones PUSH " + (enabled ? "activadas" : "desactivadas") + ".");
    }

    private boolean getNotificationPreference() {
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        return prefs.getBoolean("notifications_enabled", true); // Default true
    }

    // Método para mostrar el selector de idioma (ejemplo básico)
    private void showLanguageSelectionDialog() {
        final String[] languages = {"Español", "Inglés"}; // Puedes obtenerlos de resources
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Idioma");
        builder.setItems(languages, (dialog, which) -> {
            String selectedLanguage = languages[which];
            textViewSelectedLanguage.setText(selectedLanguage);
            // Aquí deberías añadir la lógica para cambiar el idioma de la aplicación
            // Esto suele implicar cambiar la configuración de recursos y recrear actividades
            Toast.makeText(this, "Idioma cambiado a: " + selectedLanguage, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "showLanguageSelectionDialog: Idioma seleccionado: " + selectedLanguage);
        });
        builder.show();
    }

    // Método para abrir los ajustes de la aplicación (permisos)
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
        Log.d(TAG, "openAppSettings: Abriendo ajustes de la aplicación.");
    }

    // Método para mostrar información de créditos
    private void showCredits() {
        new AlertDialog.Builder(this)
                .setTitle("Créditos de la Aplicación")
                .setMessage("Desarrollado por [Tu Nombre/Equipo]\nVersión: " + textViewAppVersion.getText().toString().replace("Versión de la App: ", ""))
                .setPositiveButton("Aceptar", null)
                .show();
        Log.d(TAG, "showCredits: Mostrando créditos.");
    }

    // Métodos para el menú de la Toolbar (si los usas)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}