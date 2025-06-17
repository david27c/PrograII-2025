package com.example.miprimeraaplicacion;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import androidx.core.content.FileProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
// ELIMINADAS importaciones de Firebase
// import com.google.firebase.auth.FirebaseAuth;
// import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
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

    private Uri imageUri; // URI de la imagen seleccionada/capturada
    private LocationManager locationManager;
    private String currentLocation = "Ubicación desconocida";
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;

    // ELIMINADAS declaraciones de Firebase
    // private FirebaseAuth mAuth;
    // private DBFirebase dbFirebase;
    private DBLocal dbLocal; // Declaración para la base de datos local

    private String currentUserId; // Para almacenar el ID del usuario logueado localmente

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_problem);

        // ELIMINADA inicialización de Firebase Auth
        // mAuth = FirebaseAuth.getInstance();

        // --- LÓGICA DE VERIFICACIÓN DE SESIÓN (SOLO CON SHARED PREFERENCES) ---
        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUserId = sharedPref.getString("current_user_id", null);

        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(this, "Debes iniciar sesión para reportar un problema.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ReportProblemActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        // --- FIN DE LÓGICA DE VERIFICACIÓN DE SESIÓN ---

        dbLocal = new DBLocal(this); // Inicializar DBLocal
        // ELIMINADA inicialización de DBFirebase
        // dbFirebase = new DBFirebase(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Reportar un Problema");
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

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(ReportProblemActivity.this, HomeActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_report) {
                return true; // Ya estás en Reportar Problema
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
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.nav_report);

        buttonTakePhoto.setOnClickListener(v -> checkPermissionsAndDispatchTakePictureIntent());
        buttonSelectGallery.setOnClickListener(v -> checkPermissionsAndPickImage());
        buttonSendReport.setOnClickListener(v -> sendReport());

        requestLocationUpdates();
    }

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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void checkPermissionsAndDispatchTakePictureIntent() {
        // Para Android 10 (API 29) y superior, WRITE_EXTERNAL_STORAGE ya no es necesario
        // para acceder a los propios archivos de la app o a la galería
        // Considera READ_MEDIA_IMAGES para Android 13+
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
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
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "Error al crear archivo de imagen: " + ex.getMessage());
                Toast.makeText(this, "Error al crear archivo para la foto.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(this,
                        "com.example.miprimeraaplicacion.fileprovider", // Debe coincidir con el 'authorities' en AndroidManifest.xml
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        return image;
    }

    private void checkPermissionsAndPickImage() {
        // Para Android 13 (API 33) y superior, usar READ_MEDIA_IMAGES
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_CODE);
            } else {
                pickImageFromGallery();
            }
        } else {
            // Para versiones anteriores, usar READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            } else {
                pickImageFromGallery();
            }
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
                } else if (permissions[0].equals(Manifest.permission.CAMERA)) {
                    dispatchTakePictureIntent();
                } else if (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE) || permissions[0].equals(Manifest.permission.READ_MEDIA_IMAGES)) {
                    pickImageFromGallery();
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
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (imageUri != null) {
                    imageViewPreview.setImageURI(imageUri);
                    imageViewPreview.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(this, "Error al obtener la imagen capturada.", Toast.LENGTH_SHORT).show();
                    imageViewPreview.setVisibility(View.GONE);
                }
            } else if (requestCode == REQUEST_PICK_IMAGE && data != null && data.getData() != null) {
                imageUri = data.getData();
                imageViewPreview.setImageURI(imageUri);
                imageViewPreview.setVisibility(View.VISIBLE);
            }
        } else {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                imageUri = null;
            }
            imageViewPreview.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
        currentLocation = "Lat: " + String.format("%.4f", currentLatitude) + ", Lon: " + String.format("%.4f", currentLongitude);
        textViewLocation.setText(currentLocation);
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
            // Es importante detener las actualizaciones de ubicación para ahorrar batería
            locationManager.removeUpdates(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reanudar las actualizaciones de ubicación cuando la actividad vuelve a estar en primer plano
        requestLocationUpdates();
    }

    private void sendReport() {
        String description = editTextDescription.getText().toString().trim();
        String reportType = spinnerReportType.getSelectedItem().toString();

        if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Por favor, describe el problema.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ya validamos currentUserId en onCreate, pero una doble verificación nunca está de más
        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(this, "Error: No se pudo obtener el ID de usuario. Por favor, reinicia la app.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBarReport.setVisibility(View.VISIBLE);
        buttonSendReport.setEnabled(false);

        String reportId = UUID.randomUUID().toString(); // Generar un ID único para el reporte
        Date fechaHora = new Date();

        String imageUrlString = (imageUri != null) ? imageUri.toString() : null;

        Denuncia nuevaDenuncia = new Denuncia(
                reportId,
                currentUserId, // Usar el ID de usuario local de SharedPreferences
                "Reporte de " + reportType, // Título inicial del reporte
                description,
                reportType,
                currentLatitude,
                currentLongitude,
                imageUrlString, // Guarda la URI local de la imagen
                fechaHora,
                "Pendiente" // Estado inicial
        );

        // Insertar la denuncia en DBLocal
        Denuncia insertedDenuncia = dbLocal.insertarDenuncia(nuevaDenuncia);

        progressBarReport.setVisibility(View.GONE);
        buttonSendReport.setEnabled(true);

        if (insertedDenuncia != null) {
            Log.d(TAG, "Reporte guardado localmente: " + insertedDenuncia.getIdDenuncia());
            Toast.makeText(this, "Reporte guardado exitosamente!", Toast.LENGTH_LONG).show();
            // Limpiar los campos después de enviar
            editTextDescription.setText("");
            imageViewPreview.setVisibility(View.GONE);
            imageUri = null; // Limpiar la URI para el próximo reporte
        } else {
            Toast.makeText(this, "Error al guardar el reporte localmente.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error al insertar denuncia en SQLite.");
        }
    }
}