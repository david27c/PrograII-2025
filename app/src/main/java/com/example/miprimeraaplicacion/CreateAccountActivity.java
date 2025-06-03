package com.example.miprimeraaplicacion;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText editTextFullName, editTextEmail, editTextUsername, editTextPassword, editTextConfirmPassword;
    private ImageView imageViewProfile;
    private Button buttonRegister;
    private TextView textViewLogin, textViewError;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editTextFullName = findViewById(R.id.editTextFullName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        imageViewProfile = findViewById(R.id.imageViewProfile);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogin = findViewById(R.id.textViewLogin);
        textViewError = findViewById(R.id.textViewError);

        // Listener para la imagen de perfil (por ahora solo un Toast)
        imageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CreateAccountActivity.this, "Funcionalidad para subir/tomar foto (por implementar)", Toast.LENGTH_SHORT).show();
            }
        });

        // Listener para el botón Registrarse
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Listener para el enlace "¿Ya tienes cuenta? Iniciar sesión"
        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Regresa a la actividad de Login
                finish(); // Cierra esta actividad para volver a la anterior (LoginActivity)
            }
        });
    }

    private void registerUser() {
        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(username) ||
                TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            textViewError.setText("Por favor, completa todos los campos.");
            textViewError.setVisibility(View.VISIBLE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            textViewError.setText("Las contraseñas no coinciden.");
            textViewError.setVisibility(View.VISIBLE);
            return;
        }

        if (password.length() < 6) {
            textViewError.setText("La contraseña debe tener al menos 6 caracteres.");
            textViewError.setVisibility(View.VISIBLE);
            return;
        }

        textViewError.setVisibility(View.GONE); // Ocultar errores previos

        // Crear usuario con Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registro exitoso
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Guardar información adicional del usuario en Firestore
                                saveUserDataToFirestore(user.getUid(), fullName, email, username);
                            }
                        } else {
                            // Si el registro falla, muestra un mensaje al usuario.
                            String errorMessage = "Error al registrar la cuenta.";
                            if (task.getException() != null) {
                                errorMessage += "\nDetalles: " + task.getException().getMessage();
                            }
                            textViewError.setText(errorMessage);
                            textViewError.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void saveUserDataToFirestore(String userId, String fullName, String email, String username) {
        Map<String, Object> user = new HashMap<>();
        user.put("fullName", fullName);
        user.put("email", email);
        user.put("username", username);
        // Puedes añadir más campos como URL de foto de perfil aquí

        db.collection("users").document(userId)
                .set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(CreateAccountActivity.this, "Cuenta creada exitosamente y datos guardados.", Toast.LENGTH_SHORT).show();
                            // Navegar a la pantalla principal o a la de login
                            Intent intent = new Intent(CreateAccountActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish(); // Finaliza esta actividad
                        } else {
                            String errorMessage = "Error al guardar datos del usuario.";
                            if (task.getException() != null) {
                                errorMessage += "\nDetalles: " + task.getException().getMessage();
                            }
                            textViewError.setText(errorMessage);
                            textViewError.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }
}