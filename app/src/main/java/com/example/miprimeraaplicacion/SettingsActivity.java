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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity"; // Etiqueta para los mensajes de Log
    private static final int PICK_IMAGE_REQUEST = 1; // Para seleccionar imagen de perfil

    private FirebaseAuth mAuth;
    private DBFirebase dbFirebase;
    private FirebaseUser currentUser;

    // Elementos de Configuración de Perfil
    private CircleImageView imageViewProfile;
    private TextView textViewChangeProfilePhoto;
    private EditText editTextFullName, editTextEmail, editTextUsername, editTextPhoneNumber;
    private EditText editTextCurrentPassword, editTextNewPassword, editTextConfirmNewPassword;
    private Button buttonSaveProfileChanges;
    private ProgressBar progressBarProfileChanges; // Para el progreso de guardar perfil

    // Elementos de Preferencias de Visibilidad
    private CheckBox checkBoxShowFullName, checkBoxShowProfilePhoto, checkBoxShowEmail, checkBoxShowPhoneNumber;
    private Button buttonSaveVisibilityPreferences;

    // Elementos de Configuración General
    private Switch switchPushNotifications;
    private LinearLayout layoutLanguageSelector; // Para el selector de idioma
    private TextView textViewSelectedLanguage;
    private TextView textViewAppPermissions;
    private Button buttonDeleteAccount;
    private TextView textViewAppVersion;
    private TextView textViewCredits;
    private Button buttonLogout;

    private BottomNavigationView bottomNavigationView;
    private Uri imageUri; // Para la nueva imagen de perfil

    @SuppressLint("MissingInflatedId") // Se puede quitar si todos los IDs se encuentran en el XML
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        dbFirebase = new DBFirebase(this);

        // Obtén el usuario actual aquí para ver su estado al inicio de la actividad
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "onCreate: Usuario logueado: " + currentUser.getEmail());
        } else {
            Log.d(TAG, "onCreate: No hay usuario logueado al iniciar la actividad.");
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Configuración"); // Título para la Toolbar
        }

        // --- Inicialización de elementos del layout ---

        // Configuración de Perfil
        imageViewProfile = findViewById(R.id.imageViewProfile);
        textViewChangeProfilePhoto = findViewById(R.id.textViewChangeProfilePhoto);
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextCurrentPassword = findViewById(R.id.editTextCurrentPassword);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextConfirmNewPassword = findViewById(R.id.editTextConfirmNewPassword);
        buttonSaveProfileChanges = findViewById(R.id.buttonSaveProfileChanges);
        // Asegúrate de que este ProgressBar esté en tu XML si lo quieres usar
        progressBarProfileChanges = findViewById(R.id.progressBarProfileChanges);

        // Preferencias de Visibilidad
        checkBoxShowFullName = findViewById(R.id.checkBoxShowFullName);
        checkBoxShowProfilePhoto = findViewById(R.id.checkBoxShowProfilePhoto);
        checkBoxShowEmail = findViewById(R.id.checkBoxShowEmail);
        checkBoxShowPhoneNumber = findViewById(R.id.checkBoxShowPhoneNumber);
        buttonSaveVisibilityPreferences = findViewById(R.id.buttonSaveVisibilityPreferences);

        // Configuración General
        switchPushNotifications = findViewById(R.id.switchPushNotifications);
        layoutLanguageSelector = findViewById(R.id.layoutLanguageSelector);
        textViewSelectedLanguage = findViewById(R.id.textViewSelectedLanguage);
        textViewAppPermissions = findViewById(R.id.textViewAppPermissions);
        buttonDeleteAccount = findViewById(R.id.buttonDeleteAccount);
        textViewAppVersion = findViewById(R.id.textViewAppVersion);
        textViewCredits = findViewById(R.id.textViewCredits);
        buttonLogout = findViewById(R.id.buttonLogout);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // --- Configuración de Listeners y lógica ---

        // Barra de navegación inferior
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_report) {
                startActivity(new Intent(SettingsActivity.this, ReportProblemActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_my_reports) {
                startActivity(new Intent(SettingsActivity.this, MyReportsActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_chat) {
                startActivity(new Intent(SettingsActivity.this, CommunityChatActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(SettingsActivity.this, ProfileActivity.class));
                finish();
                return true;
            }
            // Los ítems nav_notifications y nav_settings se manejan en la Toolbar, no en BottomNavigationView.
            return false;
        });
        // IMPORTANTE: NO intentes seleccionar un ítem 'nav_settings' aquí
        // porque ya no existe en tu bottom_navigation_menu.xml.
        // Si esta actividad se accede desde la Toolbar, no hay un ítem correspondiente en la BottomNav.
        // bottomNavigationView.setSelectedItemId(R.id.nav_settings);

        // Lógica para cargar/guardar datos del perfil y preferencias
        loadUserSettings();

        // --- Listeners para la Sección de Perfil ---
        imageViewProfile.setOnClickListener(v -> openFileChooser());
        textViewChangeProfilePhoto.setOnClickListener(v -> openFileChooser());
        buttonSaveProfileChanges.setOnClickListener(v -> saveProfileChanges());

        // --- Listeners para la Sección de Preferencias de Visibilidad ---
        buttonSaveVisibilityPreferences.setOnClickListener(v -> saveVisibilityPreferences());

        // --- Listeners para la Sección de Configuración General ---
        switchPushNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Guarda la preferencia de notificación PUSH en SharedPreferences
            SharedPreferences prefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("push_notifications_enabled", isChecked);
            editor.apply();
            Toast.makeText(SettingsActivity.this, "Notificaciones PUSH: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        layoutLanguageSelector.setOnClickListener(v -> showLanguageSelectionDialog()); // Implementar este método
        textViewAppPermissions.setOnClickListener(v -> openAppSettings()); // Abre la configuración de permisos de la app

        buttonDeleteAccount.setOnClickListener(v -> confirmAndDeleteAccount());
        buttonLogout.setOnClickListener(v -> signOutUser());

        // Mostrar la versión de la app
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            textViewAppVersion.setText("Versión de la App: " + versionName);
        } catch (Exception e) {
            e.printStackTrace();
            textViewAppVersion.setText("Versión de la App: N/A");
        }

        // Créditos (solo un Toast de ejemplo, podrías abrir una nueva actividad)
        textViewCredits.setOnClickListener(v -> Toast.makeText(SettingsActivity.this, "Desarrollado por [Tu Nombre/Equipo]", Toast.LENGTH_LONG).show());
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Vuelve a verificar el usuario cuando la actividad se vuelve visible
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "onStart: Usuario logueado: " + currentUser.getEmail());
        } else {
            Log.d(TAG, "onStart: No hay usuario logueado al reanudar la actividad.");
            // Si quieres que la actividad redirija cada vez que vuelve y el usuario no está logueado,
            // podrías descomentar las siguientes líneas.
            // startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
            // finish();
        }
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
            startActivity(new Intent(SettingsActivity.this, NotificationsActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            // Ya estamos en SettingsActivity, no necesitamos hacer nada o podemos mostrar un mensaje
            Toast.makeText(this, "Ya estás en la pantalla de Configuración.", Toast.LENGTH_SHORT).show();
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

    /**
     * Carga la configuración del usuario (perfil y preferencias) desde Firebase y SharedPreferences.
     */
    private void loadUserSettings() {
        if (currentUser == null) {
            Log.e(TAG, "loadUserSettings: currentUser es NULL. Redirigiendo a LoginActivity.");
            Toast.makeText(this, "No hay usuario logueado.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
            finish();
            return;
        }

        progressBarProfileChanges.setVisibility(View.VISIBLE);
        Log.d(TAG, "loadUserSettings: Cargando datos para el usuario ID: " + currentUser.getUid());


        // Cargar datos de perfil desde Firestore
        dbFirebase.obtenerDatosDeUsuario(currentUser.getUid(), new DBFirebase.UserCallback() {
            @Override
            public void onSuccess(User user) {
                progressBarProfileChanges.setVisibility(View.GONE);
                if (user != null) {
                    editTextFullName.setText(user.getFullName() != null ? user.getFullName() : "");
                    editTextEmail.setText(user.getEmail() != null ? user.getEmail() : ""); // El email no es editable
                    editTextUsername.setText(user.getUsername() != null ? user.getUsername() : "");
                    editTextPhoneNumber.setText(user.getPhone() != null ? user.getPhone() : "");

                    // Cargar imagen de perfil
                    if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                        Picasso.get().load(user.getProfileImageUrl())
                                .placeholder(R.drawable.ic_default_profile)
                                .error(R.drawable.ic_default_profile)
                                .into(imageViewProfile);
                    } else {
                        imageViewProfile.setImageResource(R.drawable.ic_default_profile);
                    }

                    // Cargar preferencias de visibilidad
                    checkBoxShowFullName.setChecked(user.isShowFullNamePublic());
                    checkBoxShowProfilePhoto.setChecked(user.isShowProfilePhotoInComments());
                    checkBoxShowEmail.setChecked(user.isShowEmailPublic());
                    checkBoxShowPhoneNumber.setChecked(user.isShowPhonePublic());

                } else {
                    Toast.makeText(SettingsActivity.this, "Datos de perfil no encontrados, inicializando...", Toast.LENGTH_SHORT).show();
                    // Opcional: inicializar documento si no existe, como en ProfileActivity
                    dbFirebase.inicializarDocumentoUsuario(currentUser.getUid(), currentUser.getEmail(), new DBFirebase.VoidCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(SettingsActivity.this, "Documento de usuario inicializado.", Toast.LENGTH_SHORT).show();
                            loadUserSettings(); // Recargar el perfil después de la inicialización
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(SettingsActivity.this, "Error al inicializar documento de usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                progressBarProfileChanges.setVisibility(View.GONE);
                Toast.makeText(SettingsActivity.this, "Error al cargar el perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error al cargar el perfil: ", e);
            }
        });

        // Cargar preferencias generales desde SharedPreferences
        SharedPreferences prefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        boolean pushNotificationsEnabled = prefs.getBoolean("push_notifications_enabled", true); // Default: true
        String selectedLanguage = prefs.getString("selected_language", "Español"); // Default: Español

        switchPushNotifications.setChecked(pushNotificationsEnabled);
        textViewSelectedLanguage.setText(selectedLanguage);
    }

    /**
     * Abre el selector de archivos para elegir una nueva imagen de perfil.
     */
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
            Toast.makeText(this, "Imagen seleccionada, pulsa Guardar Cambios para actualizarla.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Guarda los cambios del perfil (nombre, usuario, teléfono, contraseña e imagen).
     */
    private void saveProfileChanges() {
        if (currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesión para guardar tu perfil.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "saveProfileChanges: No hay usuario logueado para guardar cambios.");
            return;
        }

        progressBarProfileChanges.setVisibility(View.VISIBLE);

        String fullName = editTextFullName.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();
        String phone = editTextPhoneNumber.getText().toString().trim();
        String currentPassword = editTextCurrentPassword.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString().trim();
        String confirmNewPassword = editTextConfirmNewPassword.getText().toString().trim();

        Map<String, Object> profileUpdates = new HashMap<>();
        profileUpdates.put("fullName", fullName);
        profileUpdates.put("username", username);
        profileUpdates.put("phone", phone);

        // Lógica para actualizar la contraseña
        if (!newPassword.isEmpty()) {
            if (!newPassword.equals(confirmNewPassword)) {
                Toast.makeText(this, "Las nuevas contraseñas no coinciden.", Toast.LENGTH_SHORT).show();
                progressBarProfileChanges.setVisibility(View.GONE);
                Log.w(TAG, "saveProfileChanges: Las nuevas contraseñas no coinciden.");
                return;
            }
            if (currentPassword.isEmpty()) {
                Toast.makeText(this, "Introduce tu contraseña actual para cambiarla.", Toast.LENGTH_SHORT).show();
                progressBarProfileChanges.setVisibility(View.GONE);
                Log.w(TAG, "saveProfileChanges: Contraseña actual vacía al intentar cambiarla.");
                return;
            }
            // Re-autenticar al usuario para actualizar la contraseña
            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);
            currentUser.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "saveProfileChanges: Re-autenticación exitosa. Actualizando contraseña...");
                            currentUser.updatePassword(newPassword)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            Toast.makeText(SettingsActivity.this, "Contraseña actualizada.", Toast.LENGTH_SHORT).show();
                                            editTextCurrentPassword.setText("");
                                            editTextNewPassword.setText("");
                                            editTextConfirmNewPassword.setText("");
                                            Log.d(TAG, "saveProfileChanges: Contraseña actualizada con éxito.");
                                            // Continuar con la actualización del resto del perfil
                                            updateFirestoreProfile(currentUser.getUid(), profileUpdates);
                                        } else {
                                            progressBarProfileChanges.setVisibility(View.GONE);
                                            Toast.makeText(SettingsActivity.this, "Error al actualizar contraseña: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            Log.e(TAG, "Error al actualizar contraseña: ", updateTask.getException());
                                        }
                                    });
                        } else {
                            progressBarProfileChanges.setVisibility(View.GONE);
                            Toast.makeText(SettingsActivity.this, "Contraseña actual incorrecta. " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "saveProfileChanges: Error de re-autenticación: ", task.getException());
                        }
                    });
        } else {
            // Si no se cambia la contraseña, solo actualiza el perfil en Firestore
            Log.d(TAG, "saveProfileChanges: No se cambia la contraseña. Actualizando perfil en Firestore.");
            updateFirestoreProfile(currentUser.getUid(), profileUpdates);
        }
    }

    /**
     * Actualiza los datos del perfil en Firestore, incluyendo la imagen si se seleccionó una nueva.
     */
    private void updateFirestoreProfile(String userId, Map<String, Object> profileUpdates) {
        if (imageUri != null) {
            Log.d(TAG, "updateFirestoreProfile: Subiendo nueva imagen de perfil...");
            dbFirebase.subirImagenPerfil(imageUri, userId, new DBFirebase.ImageUploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    profileUpdates.put("profileImageUrl", imageUrl);
                    Log.d(TAG, "updateFirestoreProfile: Imagen subida. URL: " + imageUrl + ". Actualizando perfil en Firestore.");
                    dbFirebase.actualizarPerfilUsuario(userId, profileUpdates, new DBFirebase.VoidCallback() {
                        @Override
                        public void onSuccess() {
                            progressBarProfileChanges.setVisibility(View.GONE);
                            Toast.makeText(SettingsActivity.this, "Perfil actualizado exitosamente.", Toast.LENGTH_SHORT).show();
                            imageUri = null; // Resetear la URI después de subir
                            Log.d(TAG, "updateFirestoreProfile: Perfil (incluyendo imagen) actualizado exitosamente.");
                        }

                        @Override
                        public void onFailure(Exception e) {
                            progressBarProfileChanges.setVisibility(View.GONE);
                            Toast.makeText(SettingsActivity.this, "Error al guardar perfil (Firestore): " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Error al guardar perfil (Firestore) después de subir imagen: ", e);
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    progressBarProfileChanges.setVisibility(View.GONE);
                    Toast.makeText(SettingsActivity.this, "Error al subir imagen de perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al subir imagen de perfil: ", e);
                }
            });
        } else {
            Log.d(TAG, "updateFirestoreProfile: No hay nueva imagen. Actualizando perfil en Firestore.");
            dbFirebase.actualizarPerfilUsuario(userId, profileUpdates, new DBFirebase.VoidCallback() {
                @Override
                public void onSuccess() {
                    progressBarProfileChanges.setVisibility(View.GONE);
                    Toast.makeText(SettingsActivity.this, "Perfil actualizado exitosamente.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "updateFirestoreProfile: Perfil actualizado exitosamente (sin cambio de imagen).");
                }

                @Override
                public void onFailure(Exception e) {
                    progressBarProfileChanges.setVisibility(View.GONE);
                    Toast.makeText(SettingsActivity.this, "Error al guardar perfil (Firestore): " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al guardar perfil (Firestore) sin cambio de imagen: ", e);
                }
            });
        }
    }

    /**
     * Guarda las preferencias de visibilidad del usuario en Firestore.
     */
    private void saveVisibilityPreferences() {
        if (currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesión para guardar tus preferencias.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "saveVisibilityPreferences: No hay usuario logueado.");
            return;
        }

        Map<String, Object> visibilityUpdates = new HashMap<>();
        visibilityUpdates.put("showFullNamePublic", checkBoxShowFullName.isChecked());
        visibilityUpdates.put("showProfilePhotoInComments", checkBoxShowProfilePhoto.isChecked());
        visibilityUpdates.put("showEmailPublic", checkBoxShowEmail.isChecked());
        visibilityUpdates.put("showPhonePublic", checkBoxShowPhoneNumber.isChecked());

        Log.d(TAG, "saveVisibilityPreferences: Guardando preferencias de visibilidad...");
        dbFirebase.actualizarPreferenciasVisibilidad(currentUser.getUid(), visibilityUpdates, new DBFirebase.VoidCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(SettingsActivity.this, "Preferencias de visibilidad guardadas.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "saveVisibilityPreferences: Preferencias guardadas con éxito.");
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SettingsActivity.this, "Error al guardar preferencias: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error al guardar preferencias de visibilidad: ", e);
            }
        });
    }

    /**
     * Muestra un diálogo para que el usuario seleccione un idioma.
     * La implementación real del cambio de idioma requiere más complejidad (recursos, ContextWrapper).
     */
    private void showLanguageSelectionDialog() {
        final String[] languages = {"Español", "English"}; // Define los idiomas disponibles
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Idioma");
        builder.setItems(languages, (dialog, which) -> {
            String selectedLang = languages[which];
            textViewSelectedLanguage.setText(selectedLang);
            // Guarda la preferencia de idioma en SharedPreferences
            SharedPreferences prefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("selected_language", selectedLang);
            editor.apply();

            Toast.makeText(SettingsActivity.this, "Idioma cambiado a " + selectedLang, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "showLanguageSelectionDialog: Idioma seleccionado: " + selectedLang);
            // Aquí iría la lógica para cambiar el idioma real de la app
            // Esto es más complejo y a menudo requiere recrear la actividad o incluso un reinicio suave.
            // Para una implementación completa, investiga "Android App Localization"
        });
        builder.show();
    }

    /**
     * Abre la configuración de la aplicación en los ajustes del sistema, para gestionar permisos.
     */
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
        Log.d(TAG, "openAppSettings: Abriendo ajustes de la aplicación.");
    }

    /**
     * Confirma y elimina la cuenta del usuario de Firebase Authentication y sus datos de Firestore.
     * Esta es una operación destructiva y debe manejarse con extrema precaución.
     */
    private void confirmAndDeleteAccount() {
        if (currentUser == null) {
            Toast.makeText(this, "No hay usuario logueado para eliminar.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "confirmAndDeleteAccount: No hay usuario logueado para eliminar cuenta.");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Eliminar Cuenta")
                .setMessage("¿Estás seguro de que quieres eliminar tu cuenta permanentemente? Esta acción es irreversible.")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> {
                    Log.d(TAG, "confirmAndDeleteAccount: Usuario confirmó eliminación. Solicitando re-autenticación.");
                    showReauthenticateDialogForDeletion();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    Log.d(TAG, "confirmAndDeleteAccount: Usuario canceló eliminación.");
                })
                .show();
    }

    /**
     * Muestra un diálogo para que el usuario re-autentique antes de eliminar la cuenta.
     */
    private void showReauthenticateDialogForDeletion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar Eliminación");
        builder.setMessage("Por seguridad, introduce tu contraseña para confirmar la eliminación de la cuenta.");

        final EditText passwordInput = new EditText(this);
        passwordInput.setHint("Contraseña");
        passwordInput.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD | android.text.InputType.TYPE_CLASS_TEXT);
        builder.setView(passwordInput);

        builder.setPositiveButton("Confirmar", (dialog, which) -> {
            String password = passwordInput.getText().toString().trim();
            if (password.isEmpty()) {
                Toast.makeText(SettingsActivity.this, "La contraseña no puede estar vacía.", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "showReauthenticateDialogForDeletion: Contraseña vacía para re-autenticar.");
                return;
            }
            Log.d(TAG, "showReauthenticateDialogForDeletion: Intentando re-autenticar al usuario...");
            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);
            currentUser.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "showReauthenticateDialogForDeletion: Re-autenticación para eliminación exitosa.");
                            // Contraseña correcta, proceder a eliminar
                            performAccountDeletion(currentUser.getUid());
                        } else {
                            Toast.makeText(SettingsActivity.this, "Contraseña incorrecta o error de re-autenticación: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "showReauthenticateDialogForDeletion: Fallo en re-autenticación para eliminación: ", task.getException());
                        }
                    });
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            Log.d(TAG, "showReauthenticateDialogForDeletion: Re-autenticación cancelada.");
        });
        builder.show();
    }

    /**
     * Procede con la eliminación de la cuenta de Firebase Authentication y los datos de Firestore.
     */
    private void performAccountDeletion(String userId) {
        progressBarProfileChanges.setVisibility(View.VISIBLE); // Muestra progreso
        Log.d(TAG, "performAccountDeletion: Iniciando eliminación de cuenta para UID: " + userId);

        // 1. Eliminar datos del usuario de Firestore
        Log.d(TAG, "performAccountDeletion: Eliminando datos de usuario de Firestore...");
        dbFirebase.eliminarDatosDeUsuario(userId, new DBFirebase.VoidCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "performAccountDeletion: Datos de usuario eliminados de Firestore. Procediendo a eliminar cuenta de Auth.");
                // 2. Eliminar cuenta de Firebase Authentication
                currentUser.delete()
                        .addOnCompleteListener(task -> {
                            progressBarProfileChanges.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                Toast.makeText(SettingsActivity.this, "Cuenta eliminada exitosamente.", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "performAccountDeletion: Cuenta de Firebase Auth eliminada exitosamente.");
                                // Redirigir al usuario a la pantalla de inicio de sesión o bienvenida
                                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(SettingsActivity.this, "Error al eliminar la cuenta: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                Log.e(TAG, "Error al eliminar la cuenta de Firebase Auth: ", task.getException());
                            }
                        });
            }

            @Override
            public void onFailure(Exception e) {
                progressBarProfileChanges.setVisibility(View.GONE);
                Toast.makeText(SettingsActivity.this, "Error al eliminar datos de usuario: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error al eliminar datos de usuario de Firestore: ", e);
            }
        });
    }

    /**
     * Cierra la sesión del usuario.
     */
    private void signOutUser() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que quieres cerrar tu sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    mAuth.signOut();
                    Toast.makeText(SettingsActivity.this, "Sesión cerrada.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "signOutUser: Sesión de usuario cerrada.");
                    Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    Log.d(TAG, "signOutUser: Cierre de sesión cancelado.");
                })
                .show();
    }
}