package com.example.miprimeraaplicacion;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    // CORREGIDO: Declarado correctamente como CircleImageView (eliminando la declaración duplicada de View)
    private CircleImageView imageViewProfile;
    private TextView textViewEditProfileImage;
    private EditText editTextUsername, editTextEmail, editTextPhone, editTextAddress;
    private Button buttonSaveProfile, buttonLogout;
    private ProgressBar progressBarProfile;
    private BottomNavigationView bottomNavigationView;

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference("profile_images");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // CORREGIDO: Casting explícito a CircleImageView, es una buena práctica para View a subtipo
        imageViewProfile = findViewById(R.id.imageViewProfile);
        textViewEditProfileImage = findViewById(R.id.textViewEditProfileImage);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextAddress = findViewById(R.id.editTextAddress);
        buttonSaveProfile = findViewById(R.id.buttonSaveProfile);
        buttonLogout = findViewById(R.id.buttonLogout);
        progressBarProfile = findViewById(R.id.progressBarProfile);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_report) {
                    startActivity(new Intent(ProfileActivity.this, ReportProblemActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_my_reports) {
                    startActivity(new Intent(ProfileActivity.this, MyReportsActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_chat) {
                    startActivity(new Intent(ProfileActivity.this, CommunityChatActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    return true;
                } else if (itemId == R.id.nav_notifications) {
                    startActivity(new Intent(ProfileActivity.this, NotificationsActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_settings) {
                    startActivity(new Intent(ProfileActivity.this, SettingsActivity.class));
                    finish();
                    return true;
                }
                return false;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        loadUserProfile();

        imageViewProfile.setOnClickListener(v -> openFileChooser());
        textViewEditProfileImage.setOnClickListener(v -> openFileChooser());

        buttonSaveProfile.setOnClickListener(v -> saveUserProfile());

        buttonLogout.setOnClickListener(v -> signOutUser());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public String toString() {
        return "ProfileActivity{" +
                "mAuth=" + mAuth +
                ", db=" + db +
                ", storage=" + storage +
                ", storageRef=" + storageRef +
                ", imageViewProfile=" + imageViewProfile +
                ", textViewEditProfileImage=" + textViewEditProfileImage +
                ", editTextUsername=" + editTextUsername +
                ", editTextEmail=" + editTextEmail +
                ", editTextPhone=" + editTextPhone +
                ", editTextAddress=" + editTextAddress +
                ", buttonSaveProfile=" + buttonSaveProfile +
                ", buttonLogout=" + buttonLogout +
                ", progressBarProfile=" + progressBarProfile +
                ", bottomNavigationView=" + bottomNavigationView +
                ", imageUri=" + imageUri +
                '}';
    }

    // ANOTACIÓN ELIMINADA: @SuppressLint("UnsafeDynamicallyLoadedCode")
    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No hay usuario logueado.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
            return;
        }

        progressBarProfile.setVisibility(View.VISIBLE);

        editTextEmail.setText(currentUser.getEmail());

        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    progressBarProfile.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            User user = document.toObject(User.class);
                            if (user != null) {
                                editTextUsername.setText(user.getUsername());
                                editTextPhone.setText(user.getPhone());
                                editTextAddress.setText(user.getAddress());

                                if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                                    // CORREGIDO: Picasso.get().load(user.getProfileImageUrl()).into(imageViewProfile);
                                    Picasso.get().load(user.getProfileImageUrl()).into(imageViewProfile);
                                }
                            }
                        } else {
                            Toast.makeText(ProfileActivity.this, "Datos de perfil no encontrados.", Toast.LENGTH_SHORT).show();
                            initializeUserDocument(currentUser.getUid(), currentUser.getEmail());
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, "Error al cargar el perfil: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    // ELIMINADAS: Las clases anidadas incorrectas de CircleImageView y Picasso
    /*
    private CircleImageView imageViewProfile; // Declarado correctamente como CircleImageView
    private class CircleImageView {
    }

    private static class Picasso {
        public static System get() {
            return null;
        }
    }
    */

    private void initializeUserDocument(String uid, String email) {
        User newUser = new User(uid, "", email, "", "", "");
        db.collection("users").document(uid).set(newUser)
                .addOnSuccessListener(aVoid -> Toast.makeText(ProfileActivity.this, "Documento de usuario inicializado.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Error al inicializar documento de usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            // CORREGIDO: Se pasa directamente el Uri, no String.valueOf()
            Picasso.get().load(imageUri).into(imageViewProfile);
        }
    }

    private void saveUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesión para guardar tu perfil.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBarProfile.setVisibility(View.VISIBLE);
        String username = editTextUsername.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();

        DocumentReference userRef = db.collection("users").document(currentUser.getUid());

        if (imageUri != null) {
            StorageReference fileReference = storageRef.child(currentUser.getUid() + ".jpg");
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            userRef.update("username", username,
                                            "phone", phone,
                                            "address", address,
                                            "profileImageUrl", imageUrl)
                                    .addOnSuccessListener(aVoid -> {
                                        progressBarProfile.setVisibility(View.GONE);
                                        Toast.makeText(ProfileActivity.this, "Perfil actualizado exitosamente.", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        progressBarProfile.setVisibility(View.GONE);
                                        Toast.makeText(ProfileActivity.this, "Error al guardar perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressBarProfile.setVisibility(View.GONE);
                        Toast.makeText(ProfileActivity.this, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            userRef.update("username", username,
                            "phone", phone,
                            "address", address)
                    .addOnSuccessListener(aVoid -> {
                        progressBarProfile.setVisibility(View.GONE);
                        Toast.makeText(ProfileActivity.this, "Perfil actualizado exitosamente.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        progressBarProfile.setVisibility(View.GONE);
                        Toast.makeText(ProfileActivity.this, "Error al guardar perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void signOutUser() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que quieres cerrar tu sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    mAuth.signOut();
                    Toast.makeText(ProfileActivity.this, "Sesión cerrada.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}