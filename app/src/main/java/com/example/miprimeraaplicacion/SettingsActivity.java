package com.example.miprimeraaplicacion;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private LinearLayout layoutNotificationSettings, layoutPrivacySettings;
    private TextView textViewAppVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        layoutNotificationSettings = findViewById(R.id.layout_notification_settings);
        layoutPrivacySettings = findViewById(R.id.layout_privacy_settings);
        textViewAppVersion = findViewById(R.id.textViewAppVersion);

        // Configurar la navegación inferior
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_report) {
                    startActivity(new Intent(SettingsActivity.this, ReportProblemActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_my_reports) {
                    startActivity(new Intent(SettingsActivity.this, MyReportsActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_chat) {
                    startActivity(new Intent(SettingsActivity.this, CommunityChatActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(SettingsActivity.this, ProfileActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_notifications) {
                    startActivity(new Intent(SettingsActivity.this, NotificationsActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_settings) {
                    // Ya estamos en SettingsActivity
                    return true;
                }
                return false;
            }
        });
        // Asegurarse de que el ítem "Ajustes" esté seleccionado al inicio
        bottomNavigationView.setSelectedItemId(R.id.nav_settings);

        // Mostrar la versión de la app (ejemplo)
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            textViewAppVersion.setText("Versión de la App: " + versionName);
        } catch (Exception e) {
            e.printStackTrace();
            textViewAppVersion.setText("Versión de la App: N/A");
        }

        // Listener para opciones de ajustes
        layoutNotificationSettings.setOnClickListener(v -> Toast.makeText(SettingsActivity.this, "Abriendo preferencias de notificaciones...", Toast.LENGTH_SHORT).show());
        layoutPrivacySettings.setOnClickListener(v -> Toast.makeText(SettingsActivity.this, "Abriendo ajustes de privacidad...", Toast.LENGTH_SHORT).show());

        // Puedes añadir más lógicas para abrir nuevas actividades de sub-ajustes.
    }

    // Método para manejar el botón de regreso de la Toolbar
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}