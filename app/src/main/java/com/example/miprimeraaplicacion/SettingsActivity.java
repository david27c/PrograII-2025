package com.example.miprimeraaplicacion;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.example.miprimeraaplicacion.R;
import com.example.miprimeraaplicacion.activities.Auth.LoginActivity;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Configuración");
        }

        Button editProfileBtn = findViewById(R.id.btn_edit_profile);
        Button changePasswordBtn = findViewById(R.id.btn_change_password);
        Button notificationsBtn = findViewById(R.id.btn_notifications);
        Button privacyPolicyBtn = findViewById(R.id.btn_privacy_policy);
        Button deleteAccountBtn = findViewById(R.id.btn_delete_account);

        editProfileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        changePasswordBtn.setOnClickListener(v -> {
            // Lógica para cambiar contraseña. Podría ser un diálogo o una nueva actividad.
            showChangePasswordDialog();
        });

        notificationsBtn.setOnClickListener(v -> {
            // Lógica para configurar notificaciones. Podría abrir la configuración de la app o un diálogo.
            Toast.makeText(this, "Configuración de Notificaciones (por implementar)", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(SettingsActivity.this, NotificationSettingsActivity.class);
            // startActivity(intent);
        });

        privacyPolicyBtn.setOnClickListener(v -> {
            // Abrir la política de privacidad en un navegador web o WebView
            String url = "https://www.example.com/privacy_policy"; // Reemplaza con tu URL real
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        });

        deleteAccountBtn.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void showChangePasswordDialog() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Enviar correo de restablecimiento de contraseña a la dirección de correo electrónico del usuario
            mAuth.sendPasswordResetEmail(user.getEmail())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, "Se ha enviado un correo electrónico para restablecer tu contraseña. Revisa tu bandeja de entrada.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(SettingsActivity.this, "Error al enviar el correo: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(this, "No hay usuario autenticado.", Toast.LENGTH_SHORT).show();
            // Opcional: Redirigir a la pantalla de login
            startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
            finishAffinity(); // Cierra todas las actividades
        }
    }


    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Cuenta")
                .setMessage("¿Estás seguro de que quieres eliminar tu cuenta? Esta acción no se puede deshacer.")
                .setPositiveButton("Sí, Eliminar", (dialog, which) -> deleteUserAccount())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteUserAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // También elimina los datos del usuario de Firestore (opcional pero recomendado)
                            // Firestore.getInstance().collection("users").document(user.getUid()).delete();

                            Toast.makeText(SettingsActivity.this, "Cuenta eliminada exitosamente.", Toast.LENGTH_SHORT).show();
                            // Redirigir a la pantalla de login
                            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            // Esto puede ocurrir si la credencial del usuario ha caducado.
                            // Se le puede pedir al usuario que se vuelva a autenticar.
                            Toast.makeText(SettingsActivity.this, "Error al eliminar cuenta. Por favor, vuelve a iniciar sesión y reinténtalo: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            // Opcional: forzar al usuario a volver a loguearse si el error es AUTH_REQUIRED
                            if (task.getException() instanceof FirebaseAuthRecentLoginRequiredException) {
                                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "No hay usuario autenticado.", Toast.LENGTH_SHORT).show();
            // Redirigir a Login
            startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
            finishAffinity();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private class LoginActivity {
    }
}