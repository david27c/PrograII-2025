package com.example.miprimeraaplicacion;

import static android.os.Build.VERSION_CODES.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.example.miprimeraaplicacion.R; // Asegúrate que tu paquete coincida
import com.example.miprimeraaplicacion.fragments.HomeFragment;
import com.example.miprimeraaplicacion.fragments.MyReportsFragment;
import com.example.miprimeraaplicacion.fragments.CommunityChatFragment;
import com.example.miprimeraaplicacion.fragments.ProfileFragment;
import com.example.miprimeraaplicacion.fragments.NotificationsFragment; // Si decides añadirlo a la nav bar
import com.example.miprimeraaplicacion.fragments.SettingsFragment; // Si decides añadirlo a la nav bar

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Carga el fragmento de inicio por defecto solo si la actividad se crea por primera vez
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        // Configura el listener para la barra de navegación inferior
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

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
     * Carga un Fragmento en el contenedor principal de la actividad.
     * @param fragment El Fragmento a cargar.
     */
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment); // Reemplaza el fragmento actual
        transaction.commit(); // Confirma la transacción
    }

    /**
     * Método de ejemplo para cerrar sesión.
     * Podría ser llamado desde ProfileFragment o SettingsFragment.
     */
    public void logout() {
        // FirebaseAuth.getInstance().signOut(); // Descomentar cuando implementes el logout de Firebase Auth
        Toast.makeText(this, "Sesión cerrada (funcionalidad por implementar)", Toast.LENGTH_SHORT).show();

        // Redirige al usuario a la LoginActivity y cierra todas las actividades anteriores
        // Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // startActivity(intent);
        // finish(); // Finaliza HomeActivity
    }

    private class HomeFragment extends Fragment {
    }
}