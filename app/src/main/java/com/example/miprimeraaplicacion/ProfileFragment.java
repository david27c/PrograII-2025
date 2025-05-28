package com.example.miprimeraaplicacion;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.example.miprimeraaplicacion.R;
import com.example.miprimeraaplicacion.activities.Auth.LoginActivity; // Importa LoginActivity
import com.example.miprimeraaplicacion.activities.Main.HomeActivity; // Para acceder al método logout
import com.example.miprimeraaplicacion.activities.SettingsActivity; // Para la pantalla de configuración
import com.example.miprimeraaplicacion.models.User;

public class ProfileFragment extends Fragment {

    private ImageView profileImageView;
    private TextView nameTextView, emailTextView, phoneTextView, reportsCountTextView;
    private Button editProfileButton, logoutButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    public ProfileFragment() {
        // Constructor público vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        initViews(view);
        loadUserProfile();

        editProfileButton.setOnClickListener(v -> {
            // Ir a la actividad de configuración
            Intent intent = new Intent(getContext(), SettingsActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                mAuth.signOut();
                Toast.makeText(getContext(), "Sesión cerrada.", Toast.LENGTH_SHORT).show();
                // Redirigir a la pantalla de Login y limpiar el stack de actividades
                Intent intent = new Intent(getContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

        return view;
    }

    private void initViews(View view) {
        profileImageView = view.findViewById(R.id.profile_image_view_profile);
        nameTextView = view.findViewById(R.id.profile_name_tv);
        emailTextView = view.findViewById(R.id.profile_email_tv);
        phoneTextView = view.findViewById(R.id.profile_phone_tv);
        reportsCountTextView = view.findViewById(R.id.profile_reports_count_tv);
        editProfileButton = view.findViewById(R.id.edit_profile_button);
        logoutButton = view.findViewById(R.id.logout_button);
    }

    private void loadUserProfile() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            // Cargar datos del usuario desde Firestore
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                nameTextView.setText(user.getFullName());
                                emailTextView.setText(user.getEmail());
                                // Asume que el número de teléfono y los reportes son parte del modelo User
                                phoneTextView.setText("Teléfono: N/A"); // Actualizar cuando añadas campo phone a User
                                // reportsCountTextView.setText("Reportes Enviados: " + user.getReportsCount()); // Actualizar cuando añadas campo reportsCount

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
                            Toast.makeText(getContext(), "Datos de perfil no encontrados.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error al cargar perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

            // Contar los reportes enviados por el usuario
            db.collection("reports")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        reportsCountTextView.setText("Reportes Enviados: " + queryDocumentSnapshots.size());
                    })
                    .addOnFailureListener(e -> {
                        reportsCountTextView.setText("Reportes Enviados: Error");
                    });

        } else {
            // Usuario no logueado, redirigir a Login o mostrar mensaje
            nameTextView.setText("No logueado");
            emailTextView.setText("");
            phoneTextView.setText("");
            reportsCountTextView.setText("");
            profileImageView.setImageResource(R.drawable.ic_default_profile);
            editProfileButton.setEnabled(false);
            logoutButton.setText("Iniciar Sesión");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar el perfil cada vez que el fragmento se hace visible (por si hay cambios desde SettingsActivity)
        loadUserProfile();
    }

    private class LoginActivity {
    }
}