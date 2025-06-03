package com.example.miprimeraaplicacion;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private TextView textViewForgotPassword, textViewCreateAccount, textViewError;
    private FirebaseAuth mAuth; // Instancia de Firebase Authentication

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Enlazar elementos del diseño con las variables Java
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword);
        textViewCreateAccount = findViewById(R.id.textViewCreateAccount);
        textViewError = findViewById(R.id.textViewError);

        // Listener para el botón de Iniciar Sesión
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Listener para el enlace "¿Olvidaste tu contraseña?"
        textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí podrías abrir una nueva Activity para restablecer contraseña
                Toast.makeText(LoginActivity.this, "Funcionalidad de restablecer contraseña (por implementar)", Toast.LENGTH_SHORT).show();
            }
        });

        // Listener para el enlace "Crear Cuenta"
        textViewCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abre la actividad para crear una cuenta
                Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
                startActivity(intent);
            }
        });
    }

    // Método para iniciar sesión
    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validaciones básicas
        if (TextUtils.isEmpty(email)) {
            textViewError.setText("Por favor, ingresa tu correo electrónico.");
            textViewError.setVisibility(View.VISIBLE);
            return;
        }
        if (TextUtils.isEmpty(password)) {
            textViewError.setText("Por favor, ingresa tu contraseña.");
            textViewError.setVisibility(View.VISIBLE);
            return;
        }

        // Ocultar mensaje de error si todo está bien hasta ahora
        textViewError.setVisibility(View.GONE);

        // Iniciar sesión con Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Inicio de sesión exitoso
                            Toast.makeText(LoginActivity.this, "¡Inicio de sesión exitoso!", Toast.LENGTH_SHORT).show();
                            // Navegar a la pantalla principal (Inicio)
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish(); // Finaliza esta actividad para que el usuario no pueda volver atrás con el botón 'atrás'
                        } else {
                            // Si el inicio de sesión falla, muestra un mensaje al usuario.
                            String errorMessage = "Error al iniciar sesión. Verifica tus credenciales.";
                            if (task.getException() != null) {
                                errorMessage += "\nDetalles: " + task.getException().getMessage();
                            }
                            textViewError.setText(errorMessage);
                            textViewError.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Verificar si el usuario ya ha iniciado sesión (persistencia de sesión)
        if (mAuth.getCurrentUser() != null) {
            // Si el usuario ya está logueado, lo enviamos directamente a la pantalla principal
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // Finaliza esta actividad
        }
    }
}