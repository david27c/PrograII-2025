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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class ReportProblemActivity extends AppCompatActivity implements LocationListener {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private static final int PERMISSION_REQUEST_CODE = 100;

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

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_problem);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        // Configurar la navegación inferior
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(ReportProblemActivity.this, HomeActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_report) {
                    // Ya estamos en ReportProblemActivity
                    return true;
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
                } else if (itemId == R.id.nav_notifications) {
                    startActivity(new Intent(ReportProblemActivity.this, NotificationsActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_settings) {
                    startActivity(new Intent(ReportProblemActivity.this, SettingsActivity.class));
                    finish();
                    return true;
                }
                return false;
            }
        });
        // Asegurarse de que el ítem "Reportar" esté seleccionado al inicio
        bottomNavigationView.setSelectedItemId(R.id.nav_report);

        // Listeners para botones de imagen/video
        buttonTakePhoto.setOnClickListener(v -> checkPermissionsAndDispatchTakePictureIntent());
        buttonSelectGallery.setOnClickListener(v -> checkPermissionsAndPickImage());

        // Listener para el botón Enviar Reporte
        buttonSendReport.setOnClickListener(v -> sendReport());

        // Iniciar la obtención de ubicación
        requestLocationUpdates();
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
                // Permiso concedido, intenta nuevamente la operación
                if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestLocationUpdates();
                } else if (permissions[0].equals(Manifest.permission.CAMERA) || permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    checkPermissionsAndDispatchTakePictureIntent();
                } else if (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    checkPermissionsAndPickImage();
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
                Bundle extras = data.getExtras();
                // Si la imagen viene como bitmap, conviértela a Uri o guárdala temporalmente
                // Para simplificar, asumiremos que data.getData() contiene la Uri si es una foto
                // Para fotos tomadas, a veces se necesita guardar el bitmap en un archivo y obtener la Uri.
                // Aquí un ejemplo simplificado para mostrar la imagen:
                // Bitmap imageBitmap = (Bitmap) extras.get("data");
                // imageViewPreview.setImageBitmap(imageBitmap);
                // imageViewPreview.setVisibility(View.VISIBLE);
                // Puedes guardar este bitmap en un archivo temporal para obtener una URI y luego subirlo a Storage.
                Toast.makeText(this, "Foto tomada (se necesita guardar y obtener URI real)", Toast.LENGTH_SHORT).show();
                // Por ahora, para pruebas:
                // imageUri = data.getData(); // Esto no siempre funciona para ACTION_IMAGE_CAPTURE
                imageViewPreview.setVisibility(View.GONE); // Ocultar hasta tener una URI real
            } else if (requestCode == REQUEST_PICK_IMAGE && data != null && data.getData() != null) {
                imageUri = data.getData();
                imageViewPreview.setImageURI(imageUri);
                imageViewPreview.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentLocation = "Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude();
        textViewLocation.setText(currentLocation);
        // Puedes detener las actualizaciones de ubicación si solo necesitas una vez
        // locationManager.removeUpdates(this);
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
            locationManager.removeUpdates(this); // Detener actualizaciones al pausar
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestLocationUpdates(); // Reanudar actualizaciones al volver
    }

    private void sendReport() {
        String description = editTextDescription.getText().toString().trim();
        String reportType = spinnerReportType.getSelectedItem().toString();
        boolean reportToAuthorities = checkBoxReportToAuthorities.isChecked();

        if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Por favor, describe el problema.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Debes iniciar sesión para reportar un problema.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBarReport.setVisibility(View.VISIBLE);
        buttonSendReport.setEnabled(false); // Deshabilitar botón durante el envío

        String userId = mAuth.getCurrentUser().getUid();
        String reportId = UUID.randomUUID().toString(); // Generar un ID único para el reporte

        if (imageUri != null) {
            // Subir imagen a Firebase Storage
            StorageReference fileRef = storageRef.child("reports_images/" + reportId + ".jpg");
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    saveReportToFirestore(userId, reportId, description, reportType, currentLocation, imageUrl, reportToAuthorities);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBarReport.setVisibility(View.GONE);
                            buttonSendReport.setEnabled(true);
                            Toast.makeText(ReportProblemActivity.this, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            // Si no hay imagen, solo guardar el reporte
            saveReportToFirestore(userId, reportId, description, reportType, currentLocation, null, reportToAuthorities);
        }
    }

    private void saveReportToFirestore(String userId, String reportId, String description, String reportType, String location, String imageUrl, boolean reportToAuthorities) {
        Map<String, Object> report = new HashMap<>();
        report.put("userId", userId);
        report.put("description", description);
        report.put("type", reportType);
        report.put("location", location);
        report.put("imageUrl", imageUrl); // Puede ser null
        report.put("timestamp", System.currentTimeMillis()); // Marca de tiempo del reporte
        report.put("status", "Pendiente"); // Estado inicial del reporte
        report.put("reportToAuthorities", reportToAuthorities);

        db.collection("reports").document(reportId)
                .set(report)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressBarReport.setVisibility(View.GONE);
                        buttonSendReport.setEnabled(true);
                        Toast.makeText(ReportProblemActivity.this, "Reporte enviado exitosamente!", Toast.LENGTH_LONG).show();
                        // Opcional: Limpiar campos o navegar a otra pantalla
                        editTextDescription.setText("");
                        imageViewPreview.setVisibility(View.GONE);
                        imageUri = null;
                        // Regresar a la HomeActivity o a Mis Reportes
                        startActivity(new Intent(ReportProblemActivity.this, HomeActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBarReport.setVisibility(View.GONE);
                        buttonSendReport.setEnabled(true);
                        Toast.makeText(ReportProblemActivity.this, "Error al enviar reporte: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}