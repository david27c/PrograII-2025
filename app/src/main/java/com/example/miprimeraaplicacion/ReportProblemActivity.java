package com.example.miprimeraaplicacion;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu; // Importar para el menú de la Toolbar
import android.view.MenuItem; // Importar para los ítems del menú de la Toolbar
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Date;
import java.util.UUID;

public class ReportProblemActivity extends AppCompatActivity implements LocationListener {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String TAG = "ReportProblemActivity";

    private EditText editTextDescription;
    private Button buttonTakePhoto, buttonSelectGallery, buttonSendReport;
    private ImageView imageViewPreview;
    private Spinner spinnerReportType;
    private TextView textViewLocation;
    private CheckBox checkBoxReportToAuthorities;
    private ProgressBar progressBarReport;
    private BottomNavigationView bottomNavigationView;

    private Uri imageUri;
    private LocationManager locationManager;
    private String currentLocation = "Ubicación desconocida";
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;

    private FirebaseAuth mAuth;
    private DBLocal dbLocal;
    private DBFirebase dbFirebase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_problem);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(ReportProblemActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        dbLocal = new DBLocal(this);
        dbFirebase = new DBFirebase(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Reportar un Problema"); // Título para la Toolbar
        }


        editTextDescription = findViewById(R.id.editTextDescription);
        buttonTakePhoto = findViewById(R.id.buttonTakePhoto);
        buttonSelectGallery = findViewById(R.id.buttonSelectGallery);
        buttonSendReport = findViewById(R.id.buttonSendReport);
        imageViewPreview = findViewById(R.id.imageViewPreview);
        spinnerReportType = findViewById(R.id.spinnerReportType);
        textViewLocation = findViewById(R.id.textViewLocation);
        checkBoxReportToAuthorities = findViewById(R.id.checkBoxReportToAuthorities);
        progressBarReport = findViewById(R.id.progressBarReport);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Listener de la BottomNavigationView (SOLO LOS 5 ITEMS PRINCIPALES)
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(ReportProblemActivity.this, HomeActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_report) {
                return true; // Ya estás en Reportar
            } else if (itemId == R.id.nav_my_reports) {
                startActivity(new Intent(ReportProblemActivity.this, MyReportsActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_chat) {
                startActivity(new Intent(ReportProblemActivity.this, CommunityChatActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(ReportProblemActivity.this, ProfileActivity.class));
                finish();
                return true;
            }
            // Las referencias a nav_notifications y nav_settings se ELIMINAN de aquí
            // porque ahora están en el menú de la Toolbar.
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.nav_report); // Seleccionar "Reportar"

        buttonTakePhoto.setOnClickListener(v -> checkPermissionsAndDispatchTakePictureIntent());
        buttonSelectGallery.setOnClickListener(v -> checkPermissionsAndPickImage());
        buttonSendReport.setOnClickListener(v -> sendReport());

        requestLocationUpdates();
    }

    // *** MÉTODOS PARA EL MENÚ DE LA TOOLBAR (Notificaciones y Configuración) ***
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_notifications) {
            startActivity(new Intent(ReportProblemActivity.this, NotificationsActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(ReportProblemActivity.this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // *** FIN DE MÉTODOS DE TOOLBAR ***

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void checkPermissionsAndDispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        } else {
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void checkPermissionsAndPickImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        } else {
            pickImageFromGallery();
        }
    }

    private void pickImageFromGallery() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhotoIntent, REQUEST_PICK_IMAGE);
    }

    private void requestLocationUpdates() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        } else {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, this);
            } else {
                textViewLocation.setText("GPS deshabilitado. No se pudo obtener la ubicación.");
                Toast.makeText(this, "Por favor, habilita el GPS para obtener tu ubicación.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestLocationUpdates();
                } else if (permissions[0].equals(Manifest.permission.CAMERA) || permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    dispatchTakePictureIntent(); // Volver a intentar la acción que requería el permiso
                } else if (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    pickImageFromGallery(); // Volver a intentar la acción que requería el permiso
                }
            } else {
                Toast.makeText(this, "Permisos denegados. Algunas funcionalidades no estarán disponibles.", Toast.LENGTH_SHORT).show();
                if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    textViewLocation.setText("Permiso de ubicación denegado.");
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                // Para fotos tomadas, la URI no siempre viene directamente en data.getData().
                // Es preferible usar un FileProvider para guardar la imagen y obtener su URI.
                // Por ahora, solo muestra un mensaje, ya que tu implementación actual no guarda la URI de la cámara fácilmente.
                Toast.makeText(this, "Foto tomada. (Necesita lógica de guardado de URI)", Toast.LENGTH_LONG).show();
                // Si la foto no es accesible por URI aquí, no la mostramos para evitar errores.
                imageViewPreview.setVisibility(View.GONE);
                imageUri = null; // Reiniciar, ya que no tenemos una URI válida por ahora
            } else if (requestCode == REQUEST_PICK_IMAGE && data != null && data.getData() != null) {
                imageUri = data.getData();
                imageViewPreview.setImageURI(imageUri);
                imageViewPreview.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
        currentLocation = "Lat: " + String.format("%.4f", currentLatitude) + ", Lon: " + String.format("%.4f", currentLongitude);
        textViewLocation.setText(currentLocation);
        // locationManager.removeUpdates(this); // Puedes descomentar si solo necesitas una vez
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        textViewLocation.setText("Obteniendo ubicación...");
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        textViewLocation.setText("GPS deshabilitado.");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestLocationUpdates();
    }

    private void sendReport() {
        String description = editTextDescription.getText().toString().trim();
        String reportType = spinnerReportType.getSelectedItem().toString();
        // boolean reportToAuthorities = checkBoxReportToAuthorities.isChecked(); // Este campo no está en la clase Denuncia

        if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Por favor, describe el problema.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesión para reportar un problema.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBarReport.setVisibility(View.VISIBLE);
        buttonSendReport.setEnabled(false);

        String userId = currentUser.getUid();
        String reportId = UUID.randomUUID().toString(); // Generar un ID único para la denuncia
        Date fechaHora = new Date(); // Usar java.util.Date() para que coincida con la clase Denuncia

        // Crear un objeto Denuncia
        Denuncia nuevaDenuncia = new Denuncia(
                reportId,
                userId,
                "Reporte de " + reportType, // Título inicial
                description,
                reportType,
                currentLatitude,
                currentLongitude,
                null, // La URL de la imagen se actualizará después de la subida
                fechaHora, // Usar el objeto Date
                "Pendiente" // Estado inicial
        );

        // Guardar la denuncia usando DBLocal y DBFirebase
        // Primero, intentar guardar localmente
        boolean localSuccess = dbLocal.insertarDenuncia(nuevaDenuncia) != null;

        if (localSuccess) {
            Log.d(TAG, "Reporte guardado localmente: " + reportId);
            // Luego, intentar guardar en Firebase (con la imagen si existe)
            dbFirebase.guardarDenuncia(nuevaDenuncia, imageUri, new DBFirebase.DenunciaCallback() {
                @Override
                public void onSuccess(Denuncia denunciaGuardada) {
                    // Si la imagen se subió, la URL de la imagen en 'denunciaGuardada' estará actualizada.
                    // Actualizar la URL de la imagen en la DB local si es necesario
                    if (imageUri != null && denunciaGuardada.getUrlImagen() != null && !denunciaGuardada.getUrlImagen().isEmpty()) {
                        nuevaDenuncia.setUrlImagen(denunciaGuardada.getUrlImagen());
                        dbLocal.actualizarDenuncia(nuevaDenuncia); // Actualiza la URL en SQLite
                        Log.d(TAG, "URL de imagen actualizada en DB local: " + denunciaGuardada.getUrlImagen());
                    }

                    progressBarReport.setVisibility(View.GONE);
                    buttonSendReport.setEnabled(true);
                    Toast.makeText(ReportProblemActivity.this, "Reporte enviado exitosamente!", Toast.LENGTH_LONG).show();
                    // Limpiar campos y navegar
                    editTextDescription.setText("");
                    imageViewPreview.setVisibility(View.GONE);
                    imageUri = null;
                    // Opcional: Volver a la pantalla de inicio o a la lista de reportes
                    // startActivity(new Intent(ReportProblemActivity.this, HomeActivity.class));
                    // finish();
                }

                @Override
                public void onFailure(Exception e) {
                    progressBarReport.setVisibility(View.GONE);
                    buttonSendReport.setEnabled(true);
                    Toast.makeText(ReportProblemActivity.this, "Reporte guardado localmente, pero falló en la nube: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error al guardar reporte en Firebase: " + e.getMessage());
                }
            });
        } else {
            progressBarReport.setVisibility(View.GONE);
            buttonSendReport.setEnabled(true);
            Toast.makeText(this, "Error al guardar el reporte localmente.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error al insertar denuncia en SQLite.");
        }
    }
}