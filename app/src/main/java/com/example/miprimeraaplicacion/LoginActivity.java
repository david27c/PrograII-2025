package com.example.miprimeraaplicacion;

import android.content.Context; // Necesario para SharedPreferences
import android.content.Intent;
import android.content.SharedPreferences; // Para guardar el userId
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

// Importaciones de Firebase (las mantenemos pero su uso será temporalmente desactivado)
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser; // Para onStart()

public class LoginActivity extends AppCompatActivity {

    EditText editTextEmail, editTextPassword;
    Button buttonLogin;
    TextView textViewForgotPassword, textViewCreateAccount, textViewError;

    // Instancia de Firebase Authentication (mantendremos la declaración, pero no la usaremos para el login por ahora)
    FirebaseAuth mAuth;

    // *** NUEVA INSTANCIA PARA LA BASE DE DATOS LOCAL ***
    private DBLocal dbLocal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // mAuth = FirebaseAuth.getInstance(); // Comentamos o quitamos la inicialización de Firebase Auth

        dbLocal = new DBLocal(this); // *** Inicializamos la base de datos local ***

        // Enlazar elementos del diseño con las variables Java
        editTextEmail = findViewById(R.id.editTextEmail); // Asegúrate de que el ID sea correcto en tu layout
        editTextPassword = findViewById(R.id.editTextPassword); // Asegúrate de que el ID sea correcto en tu layout
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword);
        textViewCreateAccount = findViewById(R.id.textViewCreateAccount);
        textViewError = findViewById(R.id.textViewError);

        // Listener para el botón de Iniciar Sesión
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUserLocal(); // *** CAMBIO: Llamamos al método de inicio de sesión local ***
            }
        });

        // Listener para el enlace "¿Olvidaste tu contraseña?"
        textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    private void mostrarmsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    // Método para iniciar sesión con Firebase (comentado para no usarlo por ahora)
    /*
    private void loginUserFirebase() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

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

        textViewError.setVisibility(View.GONE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "¡Inicio de sesión exitoso!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            String errorMessage = "Error al iniciar sesión. Verifica tus credenciales.";
                            if (task.getException() != null) {
                                if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                    errorMessage = "Este correo electrónico no está registrado.";
                                } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    errorMessage = "La contraseña es incorrecta.";
                                } else {
                                    errorMessage += "\nDetalles: " + task.getException().getMessage();
                                }
                            }
                            textViewError.setText(errorMessage);
                            textViewError.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }
    */

    // *******************************************************
    // *** NUEVO MÉTODO PARA INICIAR SESIÓN CON DB LOCAL ***
    // *******************************************************
    private void loginUserLocal() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

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

        textViewError.setVisibility(View.GONE);

        String userId = dbLocal.loginUser(email, password); // Llamada al método de login en DBLocal

        if (userId != null) {
            // Inicio de sesión exitoso localmente
            Toast.makeText(LoginActivity.this, "¡Inicio de sesión exitoso (localmente)!", Toast.LENGTH_SHORT).show();

            // *** IMPORTANTE: Guarda el userId localmente para futuras operaciones ***
            SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("current_user_id", userId);
            editor.apply(); // Usa apply() para guardar de forma asíncrona

            // Navegar a la pantalla principal (HomeActivity)
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // Finaliza esta actividad de login
        } else {
            // Fallo de inicio de sesión local
            textViewError.setText("Credenciales incorrectas o usuario no registrado localmente.");
            textViewError.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Opcional: Si quieres que la persistencia de la sesión local funcione de inmediato
        // Esto solo comprobaría si hay un userId guardado localmente, no si existe en Firebase.
        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String currentUserId = sharedPref.getString("current_user_id", null);

        if (currentUserId != null) {
            // Si hay un usuario logueado localmente, lo enviamos directamente a la pantalla principal
            // Comenta la siguiente línea si quieres que siempre pase por la pantalla de login temporalmente.
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // Finaliza esta actividad
        }

        // Comentamos o quitamos la verificación de Firebase Auth, ya que no la usaremos por ahora.
        /*
        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        */
    }
}