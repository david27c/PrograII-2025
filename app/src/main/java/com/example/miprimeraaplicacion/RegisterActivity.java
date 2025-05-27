package com.example.miprimeraaplicacion;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.example.miprimeraaplicacion.R;
import com.example.miprimeraaplicacion.models.User; // Necesitarás crear esta clase

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText fullNameEditText, emailEditText, usernameEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton, uploadPhotoButton;
    private TextView alreadyHaveAccountTextView;
    private ImageView profileImageView;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;

    private Uri profileImageUri; // URI de la imagen de perfil seleccionada

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        fullNameEditText = findViewById(R.id.full_name_et);
        emailEditText = findViewById(R.id.email_et_register);
        usernameEditText = findViewById(R.id.username_et);
        passwordEditText = findViewById(R.id.password_et_register);
        confirmPasswordEditText = findViewById(R.id.confirm_password_et);
        registerButton = findViewById(R.id.register_button);
        uploadPhotoButton = findViewById(R.id.upload_photo_button);
        alreadyHaveAccountTextView = findViewById(R.id.already_have_account_tv);
        profileImageView = findViewById(R.id.profile_image_view);

        uploadPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        alreadyHaveAccountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Simplemente cierra esta actividad para volver a Login
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            profileImageUri = data.getData();
            Picasso.get().load(profileImageUri).into(profileImageView);
        }
    }

    private void registerUser() {
        final String fullName = fullNameEditText.getText().toString().trim();
        final String email = emailEditText.getText().toString().trim();
        final String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            fullNameEditText.setError("El nombre completo es requerido.");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("El correo es requerido.");
            return;
        }
        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("El nombre de usuario es requerido.");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("La contraseña es requerida.");
            return;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordEditText.setError("Confirma tu contraseña.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Las contraseñas no coinciden.");
            return;
        }
        if (password.length() < 6) {
            passwordEditText.setError("La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        // Crear usuario con Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                // Subir foto de perfil a Firebase Storage si se seleccionó una
                                if (profileImageUri != null) {
                                    uploadProfileImage(firebaseUser, fullName, username, email);
                                } else {
                                    // Si no hay foto, guardar solo los datos del usuario en Firestore
                                    saveUserDataToFirestore(firebaseUser, fullName, username, email, null);
                                }
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, "Error de registro: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void uploadProfileImage(final FirebaseUser firebaseUser, final String fullName, final String username, final String email) {
        if (profileImageUri != null) {
            final StorageReference fileReference = storageRef.child(System.currentTimeMillis() + "." + getFileExtension(profileImageUri));

            fileReference.putFile(profileImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setPhotoUri(uri)
                                            .build();
                                    firebaseUser.updateProfile(profileUpdates); // Actualiza la URL de la foto en el perfil de Firebase Auth

                                    saveUserDataToFirestore(firebaseUser, fullName, username, email, imageUrl);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(RegisterActivity.this, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        saveUserDataToFirestore(firebaseUser, fullName, username, email, null); // Guardar datos sin imagen si falla la subida
                    });
        }
    }

    private void saveUserDataToFirestore(FirebaseUser firebaseUser, String fullName, String username, String email, String profileImageUrl) {
        // Crear un objeto User (o un Map) para guardar en Firestore
        User user = new User(firebaseUser.getUid(), fullName, username, email, profileImageUrl);

        db.collection("users").document(firebaseUser.getUid())
                .set(user) // Guarda el objeto User directamente
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(RegisterActivity.this, "Registro exitoso y datos guardados.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish(); // Cierra esta actividad
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "Error al guardar datos de usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Considera eliminar el usuario de Auth si falla Firestore para evitar inconsistencias
                    firebaseUser.delete();
                });
    }

    // Helper para obtener la extensión del archivo (para Firebase Storage)
    private String getFileExtension(Uri uri) {
        return getContentResolver().getType(uri).substring(getContentResolver().getType(uri).lastIndexOf('/') + 1);
    }
}