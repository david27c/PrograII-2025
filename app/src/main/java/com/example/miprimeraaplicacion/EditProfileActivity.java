package com.example.miprimeraaplicacion;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.example.miprimeraaplicacion.R;
import com.example.miprimeraaplicacion.models.User;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID; // Para generar nombres de archivo únicos para imágenes

public class EditProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextInputEditText fullNameEditText, phoneNumberEditText;
    private Button saveProfileButton;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseUser currentUser;

    private Uri selectedImageUri; // URI de la imagen seleccionada para subir

    // Launcher para seleccionar imagen de la galería
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    profileImageView.setImageURI(selectedImageUri); // Mostrar la imagen seleccionada
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUser = mAuth.getCurrentUser();

        initViews();
        setupToolbar();
        loadUserProfileData();

        profileImageView.setOnClickListener(v -> openImagePicker());
        saveProfileButton.setOnClickListener(v -> saveProfileChanges());
    }

    private void initViews() {
        profileImageView = findViewById(R.id.profile_image_view_edit);
        fullNameEditText = findViewById(R.id.edit_full_name);
        phoneNumberEditText = findViewById(R.id.edit_phone_number);
        saveProfileButton = findViewById(R.id.btn_save_profile);
        progressBar = findViewById(R.id.edit_profile_progress_bar);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.edit_profile_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Editar Perfil");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadUserProfileData() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                fullNameEditText.setText(user.getFullName());
                                phoneNumberEditText.setText(user.getPhoneNumber()); // Asume que el modelo User tiene getPhoneNumber()

                                if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                                    Picasso.get().load(user.getProfileImageUrl())
                                            .placeholder(R.drawable.ic_default_profile)
                                            .error(R.drawable.ic_default_profile)
                                            .into(profileImageView);
                                } else {
                                    profileImageView.setImageResource(R.drawable.ic_default_profile);
                                }
                            }
                        } else {
                            Toast.makeText(this, "Datos de perfil no encontrados.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al cargar perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void saveProfileChanges() {
        String newFullName = fullNameEditText.getText().toString().trim();
        String newPhoneNumber = phoneNumberEditText.getText().toString().trim();

        if (currentUser == null) {
            Toast.makeText(this, "No hay usuario autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        saveProfileButton.setEnabled(false);

        // Actualizar datos de Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", newFullName);
        updates.put("phoneNumber", newPhoneNumber);

        // Si se seleccionó una nueva imagen, subirla a Storage
        if (selectedImageUri != null) {
            uploadProfileImage(newFullName, newPhoneNumber);
        } else {
            // Si no hay nueva imagen, solo actualizar Firestore
            updateFirestoreProfile(updates);
        }
    }

    private void uploadProfileImage(String newFullName, String newPhoneNumber) {
        if (currentUser == null || selectedImageUri == null) return;

        StorageReference profileImageRef = storage.getReference("profile_images/" + currentUser.getUid() + "/" + UUID.randomUUID().toString());

        profileImageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("fullName", newFullName);
                        updates.put("phoneNumber", newPhoneNumber);
                        updates.put("profileImageUrl", imageUrl); // Guardar la URL de la nueva imagen
                        updateFirestoreProfile(updates);
                    });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    saveProfileButton.setEnabled(true);
                    Toast.makeText(EditProfileActivity.this, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateFirestoreProfile(Map<String, Object> updates) {
        if (currentUser == null) return;

        db.collection("users").document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    saveProfileButton.setEnabled(true);
                    Toast.makeText(EditProfileActivity.this, "Perfil actualizado exitosamente.", Toast.LENGTH_SHORT).show();
                    finish(); // Volver a la actividad anterior (ProfileFragment)
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    saveProfileButton.setEnabled(true);
                    Toast.makeText(EditProfileActivity.this, "Error al actualizar perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}