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

// *** Se eliminan importaciones de Firebase Auth ya no usadas ***
// import com.google.android.gms.tasks.OnCompleteListener;
// import com.google.android.gms.tasks.Task;
// import com.google.firebase.auth.AuthResult;
// import com.google.firebase.auth.FirebaseAuth;
// import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
// import com.google.firebase.auth.FirebaseAuthInvalidUserException;
// import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText editTextEmail, editTextPassword;
    Button buttonLogin;
    TextView textViewForgotPassword, textViewCreateAccount, textViewError;

    // Se mantiene la declaración, pero ya no se inicializa ni usa para login
    // FirebaseAuth mAuth;

    // Instancia para la base de datos local
    private DBLocal dbLocal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbLocal = new DBLocal(this); // Inicializamos la base de datos local

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
                loginUserLocal(); // Llamada al método de inicio de sesión local
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

    // Método para iniciar sesión con DB LOCAL
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

            // Guarda el userId localmente para futuras operaciones
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
        // Verifica si hay un userId guardado localmente (para persistencia de sesión local)
        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String currentUserId = sharedPref.getString("current_user_id", null);

        if (currentUserId != null) {
            // Si hay un usuario logueado localmente, lo enviamos directamente a la pantalla principal
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // Finaliza esta actividad
        }
        // La verificación de Firebase Auth ha sido eliminada por completo.
    }
}