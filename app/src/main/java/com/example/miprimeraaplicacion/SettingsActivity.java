package com.example.miprimeraaplicacion;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings; // Para abrir los ajustes de permisos
import android.text.TextUtils;
import android.util.Log; // Importar la clase Log
import android.view.Menu; // Importar para el menú de la toolbar
import android.view.MenuItem; // Importar para los ítems del menú de la toolbar
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox; // Importar CheckBox
import android.widget.EditText;
import android.widget.LinearLayout; // Importar LinearLayout
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

import de.hdodenhof.circleimageview.CircleImageView; // Importar CircleImageView si lo usas para la foto

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG_SETTINGS = "SettingsActivityDebug"; // Nueva etiqueta para logs

    private DBLocal dbLocal;
    private String currentUserId; // Para almacenar el ID del usuario logueado

    // Vistas de la UI - Configuración de Perfil
    private CircleImageView imageViewProfile; // Usar CircleImageView para la foto de perfil
    private TextView textViewChangeProfilePhoto;
    private EditText editTextFullName;
    private EditText editTextEmail;
    private EditText editTextUsername;
    private EditText editTextPhoneNumber; // Corregido: Coincide con el ID del XML
    private EditText editTextCurrentPassword;
    private EditText editTextNewPassword;
    private EditText editTextConfirmNewPassword;
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
    private LinearLayout layoutLanguageSelector; // Corregido: Coincide con el ID del XML
    private TextView textViewSelectedLanguage;
    private TextView textViewAppPermissions; // Corregido: Coincide con el ID del XML
    private Button buttonDeleteAccount;
    private TextView textViewAppVersion;
    private TextView textViewCredits;
    private Button buttonLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG_SETTINGS, "onCreate() de SettingsActivity: Inicio."); // LOG
        setContentView(R.layout.activity_settings);
        Log.d(TAG_SETTINGS, "onCreate(): Layout activity_settings establecido."); // LOG

        dbLocal = new DBLocal(this); // Inicializar DBLocal al principio del onCreate
        Log.d(TAG_SETTINGS, "onCreate(): DBLocal inicializado."); // LOG

        // --- LÓGICA DE VERIFICACIÓN DE SESIÓN (MODIFICADA) ---
        // Obtener el ID de usuario logueado desde DBLocal
        currentUserId = dbLocal.getLoggedInUserId(this);
        Log.d(TAG_SETTINGS, "onCreate(): ID de usuario recuperado de DBLocal: " + currentUserId); // LOG

        if (currentUserId == null || currentUserId.isEmpty()) { // Verificación más robusta (null o vacío)
            Log.d(TAG_SETTINGS, "onCreate(): No hay usuario logueado o ID vacío. Redirigiendo a LoginActivity."); // LOG
            Toast.makeText(this, "No hay sesión activa. Redirigiendo...", Toast.LENGTH_LONG).show();
            redirectToLogin(); // Redirige al login y finaliza esta actividad
            return; // Detener la ejecución si no hay usuario logueado
        }
        // --- FIN DE LÓGICA DE VERIFICACIÓN DE SESIÓN ---

        // Configurar la Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Configuración");
        }
        Log.d(TAG_SETTINGS, "onCreate(): Toolbar configurada."); // LOG

        // Configurar BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
                finish();
                Log.d(TAG_SETTINGS, "BottomNav: Redirigiendo a HomeActivity."); // LOG
                return true;
            } else if (itemId == R.id.nav_notifications) {
                startActivity(new Intent(SettingsActivity.this, NotificationsActivity.class));
                finish();
                Log.d(TAG_SETTINGS, "BottomNav: Redirigiendo a NotificationsActivity."); // LOG
                return true;
            } else if (itemId == R.id.nav_profile) {
                // Ya estamos en Settings, puedes ir a ProfileActivity si es diferente, o no hacer nada
                startActivity(new Intent(SettingsActivity.this, ProfileActivity.class));
                finish();
                Log.d(TAG_SETTINGS, "BottomNav: Redirigiendo a ProfileActivity."); // LOG
                return true;
            } else if (itemId == R.id.nav_settings) {
                // Ya estamos aquí
                Log.d(TAG_SETTINGS, "BottomNav: Ya en SettingsActivity."); // LOG
                return true;
            }
            return false;
        });
        Log.d(TAG_SETTINGS, "onCreate(): BottomNavigationView configurado."); // LOG


        // Inicializar vistas de Configuración de Perfil
        imageViewProfile = findViewById(R.id.imageViewProfile);
        textViewChangeProfilePhoto = findViewById(R.id.textViewChangeProfilePhoto);
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber); // ID corregido
        editTextCurrentPassword = findViewById(R.id.editTextCurrentPassword);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextConfirmNewPassword = findViewById(R.id.editTextConfirmNewPassword);
        buttonSaveProfileChanges = findViewById(R.id.buttonSaveProfileChanges);
        progressBarProfileChanges = findViewById(R.id.progressBarProfileChanges);
        Log.d(TAG_SETTINGS, "onCreate(): Vistas de Perfil inicializadas."); // LOG

        // Inicializar vistas de Preferencias de Visibilidad
        checkBoxShowFullName = findViewById(R.id.checkBoxShowFullName);
        checkBoxShowProfilePhoto = findViewById(R.id.checkBoxShowProfilePhoto);
        checkBoxShowEmail = findViewById(R.id.checkBoxShowEmail);
        checkBoxShowPhoneNumber = findViewById(R.id.checkBoxShowPhoneNumber);
        buttonSaveVisibilityPreferences = findViewById(R.id.buttonSaveVisibilityPreferences);
        Log.d(TAG_SETTINGS, "onCreate(): Vistas de Visibilidad inicializadas."); // LOG


        // Inicializar vistas de Configuración General
        switchPushNotifications = findViewById(R.id.switchPushNotifications);
        layoutLanguageSelector = findViewById(R.id.layoutLanguageSelector); // ID corregido
        textViewSelectedLanguage = findViewById(R.id.textViewSelectedLanguage);
        textViewAppPermissions = findViewById(R.id.textViewAppPermissions); // ID corregido
        buttonDeleteAccount = findViewById(R.id.buttonDeleteAccount);
        textViewAppVersion = findViewById(R.id.textViewAppVersion);
        textViewCredits = findViewById(R.id.textViewCredits);
        buttonLogout = findViewById(R.id.buttonLogout);
        Log.d(TAG_SETTINGS, "onCreate(): Vistas de Configuración General inicializadas."); // LOG

        // Cargar datos del usuario y preferencias (ya que currentUserId no es nulo/vacío)
        loadUserProfile();
        loadGeneralPreferences(); // Cargar preferencias generales como notificaciones
        loadVisibilityPreferences(); // Cargar preferencias de visibilidad
        Log.d(TAG_SETTINGS, "onCreate(): Datos de usuario y preferencias cargadas."); // LOG


        // Asignar Listeners
        buttonSaveProfileChanges.setOnClickListener(v -> saveProfileChanges());
        buttonSaveVisibilityPreferences.setOnClickListener(v -> saveVisibilityPreferences()); // Listener para guardar visibilidad
        buttonDeleteAccount.setOnClickListener(v -> confirmDeleteAccount());
        buttonLogout.setOnClickListener(v -> confirmLogout());
        layoutLanguageSelector.setOnClickListener(v -> showLanguageSelectionDialog()); // Listener para el selector de idioma
        textViewAppPermissions.setOnClickListener(v -> openAppSettings()); // Listener para permisos de app
        textViewCredits.setOnClickListener(v -> showCredits());

        // Listener para la foto de perfil (simulado por ahora)
        imageViewProfile.setOnClickListener(v -> Toast.makeText(this, "Toca para cambiar foto (funcionalidad no implementada)", Toast.LENGTH_SHORT).show());
        textViewChangeProfilePhoto.setOnClickListener(v -> Toast.makeText(this, "Cambiar foto de perfil (funcionalidad no implementada)", Toast.LENGTH_SHORT).show());

        // Listener para el switch de notificaciones push
        switchPushNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePushNotificationPreference(isChecked);
        });
        Log.d(TAG_SETTINGS, "onCreate(): Listeners configurados."); // LOG

        // Establecer la versión de la aplicación (ejemplo, puedes obtenerla dinámicamente)
        // textViewAppVersion.setText("Versión de la App: " + BuildConfig.VERSION_NAME); // Si usas BuildConfig
        textViewAppVersion.setText("Versión de la App: 1.0.0"); // Valor hardcodeado de ejemplo
        Log.d(TAG_SETTINGS, "onCreate(): Versión de la App establecida."); // LOG

        // Seleccionar el ítem correcto en el BottomNavigationView
        Menu menu = bottomNavigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            if (menuItem.getItemId() == R.id.nav_settings) {
                menuItem.setChecked(true);
                break;
            }
        }
        Log.d(TAG_SETTINGS, "onCreate(): Ítem de BottomNavigationView seleccionado."); // LOG

        Log.d(TAG_SETTINGS, "onCreate() de SettingsActivity: Fin."); // LOG
    }

    // Método para cargar los datos del perfil del usuario
    private void loadUserProfile() {
        progressBarProfileChanges.setVisibility(View.VISIBLE);
        dbLocal.getUserProfileAsync(currentUserId, new DBLocal.UserCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    progressBarProfileChanges.setVisibility(View.GONE);
                    if (user != null) {
                        editTextFullName.setText(user.getFullName());
                        editTextEmail.setText(user.getEmail());
                        editTextUsername.setText(user.getUsername());
                        editTextPhoneNumber.setText(user.getPhone()); // Cargar número de teléfono

                        // Aquí podrías cargar la foto de perfil si tuvieras una URL o URI en el objeto User
                        // Glide.with(SettingsActivity.this).load(user.getProfileImageUrl()).into(imageViewProfile);
                        // O usar un default si no hay
                        // if (user.getProfileImageUrl() == null || user.getProfileImageUrl().isEmpty()) {
                        //     imageViewProfile.setImageResource(R.drawable.ic_default_profile);
                        // }

                        Log.d(TAG, "Perfil de usuario cargado para: " + user.getEmail());
                    } else {
                        Toast.makeText(SettingsActivity.this, "No se pudo cargar el perfil del usuario.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "loadUserProfile: Usuario es null al cargar perfil para ID: " + currentUserId);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    progressBarProfileChanges.setVisibility(View.GONE);
                    Toast.makeText(SettingsActivity.this, "Error al cargar el perfil: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "loadUserProfile: Error al cargar perfil para ID: " + currentUserId, e);
                });
            }
        });
    }

    // Método para guardar los cambios del perfil
    private void saveProfileChanges() {
        String newFullName = editTextFullName.getText().toString().trim();
        String newUsername = editTextUsername.getText().toString().trim();
        String newPhone = editTextPhoneNumber.getText().toString().trim(); // Obtener número de teléfono
        String currentPassword = editTextCurrentPassword.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString().trim();
        String confirmNewPassword = editTextConfirmNewPassword.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(newFullName) || TextUtils.isEmpty(newUsername)) {
            Toast.makeText(this, "Nombre completo y nombre de usuario no pueden estar vacíos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Manejo de cambio de contraseña
        if (!TextUtils.isEmpty(currentPassword) || !TextUtils.isEmpty(newPassword) || !TextUtils.isEmpty(confirmNewPassword)) {
            if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmNewPassword)) {
                Toast.makeText(this, "Para cambiar la contraseña, todos los campos de contraseña deben estar llenos.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPassword.equals(confirmNewPassword)) {
                Toast.makeText(this, "La nueva contraseña y su confirmación no coinciden.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPassword.length() < 6) {
                Toast.makeText(this, "La nueva contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Proceder con el cambio de contraseña
            progressBarProfileChanges.setVisibility(View.VISIBLE);
            dbLocal.updateUserPasswordAsync(currentUserId, currentPassword, newPassword, new DBLocal.VoidCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(SettingsActivity.this, "Contraseña actualizada exitosamente.", Toast.LENGTH_SHORT).show();
                        editTextCurrentPassword.setText("");
                        editTextNewPassword.setText("");
                        editTextConfirmNewPassword.setText("");
                        // Después de cambiar la contraseña, guardamos el resto del perfil
                        updateUserProfileDetails(newFullName, newUsername, newPhone);
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    runOnUiThread(() -> {
                        progressBarProfileChanges.setVisibility(View.GONE);
                        Toast.makeText(SettingsActivity.this, "Error al actualizar contraseña: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "saveProfileChanges: Error al actualizar contraseña para ID: " + currentUserId, e);
                    });
                }
            });
        } else {
            // No se intenta cambiar la contraseña, solo actualizar detalles del perfil
            updateUserProfileDetails(newFullName, newUsername, newPhone);
        }
    }

    // Método auxiliar para actualizar los detalles del perfil (sin contraseña)
    private void updateUserProfileDetails(String fullName, String username, String phone) {
        progressBarProfileChanges.setVisibility(View.VISIBLE);
        dbLocal.getUserProfileAsync(currentUserId, new DBLocal.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    user.setFullName(fullName);
                    user.setUsername(username);
                    user.setPhone(phone); // Actualizar el número de teléfono

                    // Aquí podrías añadir lógica para actualizar la URL de la imagen de perfil si la tuvieras
                    // user.setProfileImageUrl("nueva_url_imagen");

                    dbLocal.updateUserProfileAsync(user, new DBLocal.VoidCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                progressBarProfileChanges.setVisibility(View.GONE);
                                Toast.makeText(SettingsActivity.this, "Cambios de perfil guardados.", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "updateUserProfileDetails: Perfil actualizado exitosamente para ID: " + currentUserId);
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            runOnUiThread(() -> {
                                progressBarProfileChanges.setVisibility(View.GONE);
                                Toast.makeText(SettingsActivity.this, "Error al guardar cambios del perfil: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                Log.e(TAG, "updateUserProfileDetails: Error al actualizar perfil en DBLocal para ID: " + currentUserId, e);
                            });
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        progressBarProfileChanges.setVisibility(View.GONE);
                        Toast.makeText(SettingsActivity.this, "Error: Usuario no encontrado para actualizar.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "updateUserProfileDetails: Usuario null al intentar actualizar perfil para ID: " + currentUserId);
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    progressBarProfileChanges.setVisibility(View.GONE);
                    Toast.makeText(SettingsActivity.this, "Error al obtener usuario para actualizar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "updateUserProfileDetails: Error al obtener usuario para actualizar perfil para ID: " + currentUserId, e);
                });
            }
        });
    }

    // Método para cargar preferencias generales (ej. notificaciones)
    private void loadGeneralPreferences() {
        SharedPreferences prefs = getSharedPreferences("app_preferences", MODE_PRIVATE);
        boolean pushNotificationsEnabled = prefs.getBoolean("push_notifications_enabled", true); // Valor por defecto true
        switchPushNotifications.setChecked(pushNotificationsEnabled);

        String selectedLang = prefs.getString("selected_language", "Español");
        textViewSelectedLanguage.setText(selectedLang);
        Log.d(TAG, "loadGeneralPreferences: Notificaciones Push: " + pushNotificationsEnabled + ", Idioma: " + selectedLang);
    }

    // Método para guardar la preferencia de notificaciones push
    private void savePushNotificationPreference(boolean isEnabled) {
        SharedPreferences prefs = getSharedPreferences("app_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("push_notifications_enabled", isEnabled);
        editor.apply();
        Toast.makeText(this, "Notificaciones Push " + (isEnabled ? "activadas" : "desactivadas"), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "savePushNotificationPreference: Notificaciones Push guardadas: " + isEnabled);
    }


    // Método para cargar las preferencias de visibilidad
    private void loadVisibilityPreferences() {
        // Obtenemos el usuario actual para cargar sus preferencias desde la BD
        dbLocal.getUserProfileAsync(currentUserId, new DBLocal.UserCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    if (user != null) {
                        checkBoxShowFullName.setChecked(user.isShowFullNamePublic());
                        checkBoxShowProfilePhoto.setChecked(user.isShowProfilePhotoInComments());
                        checkBoxShowEmail.setChecked(user.isShowEmailPublic());
                        checkBoxShowPhoneNumber.setChecked(user.isShowPhonePublic());
                        Log.d(TAG, "loadVisibilityPreferences: Preferencias de visibilidad cargadas.");
                    } else {
                        Log.e(TAG, "loadVisibilityPreferences: Usuario es null, no se pudieron cargar las preferencias.");
                        // Opcional: setear valores por defecto en los checkboxes si el usuario es null
                        checkBoxShowFullName.setChecked(true);
                        checkBoxShowProfilePhoto.setChecked(true);
                        checkBoxShowEmail.setChecked(false);
                        checkBoxShowPhoneNumber.setChecked(false);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(SettingsActivity.this, "Error al cargar preferencias de visibilidad: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "loadVisibilityPreferences: Error al cargar preferencias para ID: " + currentUserId, e);
                });
            }
        });
    }

    // Método para guardar las preferencias de visibilidad
    private void saveVisibilityPreferences() {
        progressBarProfileChanges.setVisibility(View.VISIBLE); // Usar la misma progress bar
        dbLocal.getUserProfileAsync(currentUserId, new DBLocal.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    user.setShowFullNamePublic(checkBoxShowFullName.isChecked());
                    user.setShowProfilePhotoInComments(checkBoxShowProfilePhoto.isChecked());
                    user.setShowEmailPublic(checkBoxShowEmail.isChecked());
                    user.setShowPhonePublic(checkBoxShowPhoneNumber.isChecked());

                    dbLocal.updateUserProfileAsync(user, new DBLocal.VoidCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                progressBarProfileChanges.setVisibility(View.GONE);
                                Toast.makeText(SettingsActivity.this, "Preferencias de visibilidad guardadas.", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "saveVisibilityPreferences: Preferencias de visibilidad actualizadas para ID: " + currentUserId);
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            runOnUiThread(() -> {
                                progressBarProfileChanges.setVisibility(View.GONE);
                                Toast.makeText(SettingsActivity.this, "Error al guardar preferencias de visibilidad: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                Log.e(TAG, "saveVisibilityPreferences: Error al actualizar preferencias en DBLocal para ID: " + currentUserId, e);
                            });
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        progressBarProfileChanges.setVisibility(View.GONE);
                        Toast.makeText(SettingsActivity.this, "Error: Usuario no encontrado para guardar preferencias.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "saveVisibilityPreferences: Usuario null al intentar guardar preferencias para ID: " + currentUserId);
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    progressBarProfileChanges.setVisibility(View.GONE);
                    Toast.makeText(SettingsActivity.this, "Error al obtener usuario para guardar preferencias: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "saveVisibilityPreferences: Error al obtener usuario para guardar preferencias para ID: " + currentUserId, e);
                });
            }
        });
    }


    // Método para mostrar el diálogo de selección de idioma
    private void showLanguageSelectionDialog() {
        final String[] languages = {"Español", "English"}; // Puedes añadir más idiomas
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Idioma");
        builder.setItems(languages, (dialog, which) -> {
            String selectedLanguage = languages[which];
            textViewSelectedLanguage.setText(selectedLanguage);
            // Aquí guardarías la preferencia de idioma en SharedPreferences
            SharedPreferences prefs = getSharedPreferences("app_preferences", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("selected_language", selectedLanguage);
            editor.apply();
            Toast.makeText(SettingsActivity.this, "Idioma cambiado a: " + selectedLanguage, Toast.LENGTH_SHORT).show();
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

    // Confirmación para eliminar cuenta
    private void confirmDeleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Cuenta")
                .setMessage("¿Estás seguro de que quieres eliminar tu cuenta? Esta acción es irreversible.")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> deleteUserAccountAndData())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Lógica para eliminar la cuenta y los datos asociados
    private void deleteUserAccountAndData() {
        progressBarProfileChanges.setVisibility(View.VISIBLE);
        // Usar el método asíncrono deleteUserAsync de DBLocal
        dbLocal.deleteUserAsync(currentUserId, new DBLocal.VoidCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    progressBarProfileChanges.setVisibility(View.GONE);
                    dbLocal.clearLoggedInUserId(SettingsActivity.this); // Limpiar sesión local, pasar el contexto
                    Toast.makeText(SettingsActivity.this, "Cuenta eliminada exitosamente.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "deleteUserAccountAndData: Cuenta eliminada localmente para ID: " + currentUserId);
                    redirectToLogin();
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    progressBarProfileChanges.setVisibility(View.GONE);
                    Toast.makeText(SettingsActivity.this, "Error al eliminar la cuenta: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "deleteUserAccountAndData: Falló la eliminación de la cuenta en DBLocal para ID: " + currentUserId, e);
                });
            }
        });
    }

    // Confirmación para cerrar sesión
    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que quieres cerrar tu sesión?")
                .setPositiveButton("Sí, cerrar sesión", (dialog, which) -> logoutUser())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Lógica para cerrar sesión
    private void logoutUser() {
        dbLocal.clearLoggedInUserId(this); // Limpiar el ID de usuario logueado
        Toast.makeText(this, "Sesión cerrada.", Toast.LENGTH_SHORT).show();
        redirectToLogin();
        Log.d(TAG, "logoutUser: Sesión cerrada para usuario: " + currentUserId);
    }

    // Redirigir a la pantalla de Login y finalizar esta actividad
    private void redirectToLogin() {
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Limpiar la pila de actividades
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbLocal != null) {
            dbLocal.close();
            Log.d(TAG, "onDestroy: DBLocal cerrada.");
        }
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