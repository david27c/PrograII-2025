package com.example.miprimeraaplicacion;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

// No necesitas importar NotificationsFragment ni SettingsFragment aquí si no los pones directamente en la barra de navegación.
// Si los pones, sí necesitarías importarlos y manejarlos en el listener.

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private static final int PERMISSION_REQUEST_CODE_NOTIFICATIONS = 101; // Código para solicitar permiso

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Solicita el permiso de notificaciones para Android 13+
        requestNotificationPermission();

        // Carga el fragmento de inicio por defecto solo si la actividad se crea por primera vez
        if (savedInstanceState == null) {
            loadFragment(new com.example.miprimeraaplicacion.HomeFragment());
        }

        // Configura el listener para la barra de navegación inferior
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId(); // Usamos getItemId() para obtener el ID del ítem

                // Maneja la selección de ítems en la barra de navegación
                if (itemId == R.id.nav_home) {
                    selectedFragment = new com.example.miprimeraaplicacion.HomeFragment();
                } else if (itemId == R.id.nav_report) {
                    // Cuando se selecciona "Reportar", iniciamos la ReportProblemActivity
                    startActivity(new Intent(HomeActivity.this, ReportProblemActivity.class));
                    // No se carga ningún fragmento, por lo que regresamos true
                    return true;
                } else if (itemId == R.id.nav_my_reports) {
                    selectedFragment = new com.example.miprimeraaplicacion.MyReportsFragment();
                } else if (itemId == R.id.nav_community_chat) {
                    selectedFragment = new com.example.miprimeraaplicacion.CommunityChatFragment();
                } else if (itemId == R.id.nav_profile) {
                    selectedFragment = new com.example.miprimeraaplicacion.ProfileFragment();
                }
                // Si tienes más de 5 elementos en la barra, considera un menú desplegable o menos iconos.
                // Si añades Notificaciones (nav_notifications) o Configuración (nav_settings) a la barra,
                // también debes manejar sus IDs aquí para cargar sus respectivos Fragments.

                // Si un fragmento ha sido seleccionado, cárgalo
                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                    return true; // Indica que el ítem fue manejado
                }
                return false; // Indica que el ítem no fue manejado
            }
        });
    }

    /**
     * Solicita el permiso POST_NOTIFICATIONS para Android 13 (API 33) y superior.
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Si el permiso no está concedido, solicítalo al usuario
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE_NOTIFICATIONS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, puedes informar al usuario si lo deseas
                Toast.makeText(this, "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show();
            } else {
                // Permiso denegado, puedes informar al usuario y quizás deshabilitar funcionalidades
                Toast.makeText(this, "Permiso de notificaciones denegado. Algunas funcionalidades pueden no estar disponibles.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Carga un Fragmento en el contenedor principal de la actividad.
     * @param fragment El Fragmento a cargar.
     */
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment); // Reemplaza el fragmento actual
        transaction.commit(); // Confirma la transacción
    }

    /**
     * Método para cerrar sesión. Se llamará desde ProfileFragment o SettingsActivity.
     */
    public void logout() {
        FirebaseAuth.getInstance().signOut(); // Cierra la sesión de Firebase Auth
        Toast.makeText(this, "Sesión cerrada.", Toast.LENGTH_SHORT).show();

        // Redirige al usuario a la LoginActivity y cierra todas las actividades anteriores
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Finaliza HomeActivity
    }
}