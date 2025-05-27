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

import com.example.miprimeraaplicacion.activities.Main.HomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView forgotPasswordTextView, createAccountTextView;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Si el usuario ya está logueado, redirigir a HomeActivity
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        }

        emailEditText = findViewById(R.id.email_et);
        passwordEditText = findViewById(R.id.password_et);
        loginButton = findViewById(R.id.login_button);
        forgotPasswordTextView = findViewById(R.id.forgot_password_tv);
        createAccountTextView = findViewById(R.id.create_account_tv);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        createAccountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí podrías abrir una nueva Activity para restablecer la contraseña
                Toast.makeText(LoginActivity.this, "¿Olvidaste tu contraseña? (Funcionalidad por implementar)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("El correo es requerido.");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("La contraseña es requerida.");
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError("La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        // Iniciar sesión con Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Inicio de sesión exitoso
                            Toast.makeText(LoginActivity.this, "¡Inicio de sesión exitoso!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish(); // Cierra la LoginActivity para que el usuario no pueda volver atrás con el botón de retroceso
                        } else {
                            // Falló el inicio de sesión
                            Toast.makeText(LoginActivity.this, "Error de inicio de sesión: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}