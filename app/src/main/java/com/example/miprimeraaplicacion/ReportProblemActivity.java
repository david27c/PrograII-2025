package com.example.miprimeraaplicacion;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.example.miprimeraaplicacion.R;
import com.example.miprimeraaplicacion.database.AppDatabase;
import com.example.miprimeraaplicacion.database.ReporteLocal;
import com.example.miprimeraaplicacion.models.Report; // Necesitarás crear esta clase

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReportProblemActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_VIDEO_REQUEST = 2;
    private static final int TAKE_PHOTO_REQUEST = 3;
    private static final int TAKE_VIDEO_REQUEST = 4;

    private EditText titleEditText, descriptionEditText;
    private Spinner typeSpinner;
    private TextView locationTextView;
    private CheckBox reportAuthoritiesCheckBox;
    private Button sendReportButton, takePhotoButton, selectGalleryButton;
    private ImageView reportImagePreview;
    private VideoView reportVideoPreview;
    private TextView noMediaText;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private FusedLocationProviderClient fusedLocationClient;

    private Uri mediaUri; // URI del archivo multimedia (imagen o video)
    private String mediaType; // "image" o "video"
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_problem);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("report_media");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initViews();
        setupSpinner();
        checkPermissionsAndGetLocation();

        takePhotoButton.setOnClickListener(v -> showMediaSourceDialog(true)); // True para cámara
        selectGalleryButton.setOnClickListener(v -> showMediaSourceDialog(false)); // False para galería
        sendReportButton.setOnClickListener(v -> sendReport());
    }

    private void initViews() {
        titleEditText = findViewById(R.id.report_title_et);
        descriptionEditText = findViewById(R.id.report_description_et);
        typeSpinner = findViewById(R.id.report_type_spinner);
        locationTextView = findViewById(R.id.location_tv);
        reportAuthoritiesCheckBox = findViewById(R.id.report_authorities_checkbox);
        sendReportButton = findViewById(R.id.send_report_button);
        takePhotoButton = findViewById(R.id.take_photo_button);
        selectGalleryButton = findViewById(R.id.select_gallery_button);
        reportImagePreview = findViewById(R.id.report_image_preview);
        reportVideoPreview = findViewById(R.id.report_video_preview);
        noMediaText = findViewById(R.id.no_media_text);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupSpinner() {
        String[] reportTypes = {"Baches", "Basura", "Agua Potable", "Electricidad", "Seguridad", "Transporte", "Otros"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, reportTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
    }

    private void showMediaSourceDialog(boolean fromCamera) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Tipo de Contenido");
        String[] options = {"Imagen", "Video"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) { // Imagen
                if (fromCamera) {
                    dispatchTakePictureIntent();
                } else {
                    openImageChooser();
                }
            } else { // Video
                if (fromCamera) {
                    dispatchTakeVideoIntent();
                } else {
                    openVideoChooser();
                }
            }
        });
        builder.show();
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleccionar Imagen"), PICK_IMAGE_REQUEST);
    }

    private void openVideoChooser() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleccionar Video"), PICK_VIDEO_REQUEST);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST);
        }
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, TAKE_VIDEO_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_IMAGE_REQUEST || requestCode == TAKE_PHOTO_REQUEST) {
                mediaUri = data.getData();
                mediaType = "image";
                reportImagePreview.setVisibility(View.VISIBLE);
                reportVideoPreview.setVisibility(View.GONE);
                noMediaText.setVisibility(View.GONE);
                reportImagePreview.setImageURI(mediaUri);
            } else if (requestCode == PICK_VIDEO_REQUEST || requestCode == TAKE_VIDEO_REQUEST) {
                mediaUri = data.getData();
                mediaType = "video";
                reportVideoPreview.setVisibility(View.VISIBLE);
                reportImagePreview.setVisibility(View.GONE);
                noMediaText.setVisibility(View.GONE);
                reportVideoPreview.setVideoURI(mediaUri);
                reportVideoPreview.start(); // Auto-play video preview
            }
        }
    }

    private void checkPermissionsAndGetLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE // Para guardar archivos de cámara en versiones antiguas de Android
                    }, PERMISSION_REQUEST_CODE);
        } else {
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                getLocation();
            } else {
                Toast.makeText(this, "Permisos denegados. Algunas funcionalidades no estarán disponibles.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            currentLocation = location;
                            locationTextView.setText("Ubicación: " + String.format("%.4f", location.getLatitude()) + ", " + String.format("%.4f", location.getLongitude()));
                        } else {
                            locationTextView.setText("Ubicación: No disponible. Asegúrate de tener el GPS activado.");
                        }
                    });
        }
    }

    private void sendReport() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String type = typeSpinner.getSelectedItem().toString();
        boolean reportToAuthorities = reportAuthoritiesCheckBox.isChecked();

        if (TextUtils.isEmpty(title)) {
            titleEditText.setError("El título es requerido.");
            return;
        }
        if (TextUtils.isEmpty(description)) {
            descriptionEditText.setError("La descripción es requerida.");
            return;
        }
        if (currentLocation == null) {
            Toast.makeText(this, "Esperando ubicación GPS. Intenta de nuevo.", Toast.LENGTH_SHORT).show();
            getLocation(); // Re-intentar obtener ubicación
            return;
        }

        // Mostrar ProgressBar
        progressBar.setVisibility(View.VISIBLE);
        sendReportButton.setEnabled(false);

        // Verificar conexión a internet
        if (isNetworkAvailable()) {
            uploadMediaAndReport(title, description, type, reportToAuthorities);
        } else {
            saveReportLocally(title, description, type, reportToAuthorities);
        }
    }

    private boolean isNetworkAvailable() {
        // Implementar lógica para verificar la conexión a internet
        // (Esto requiere el permiso ACCESS_NETWORK_STATE en AndroidManifest)
        // Ejemplo simplificado:
        // ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        // return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        return true; // Por ahora, asumimos que siempre hay red para la demostración
    }

    private void uploadMediaAndReport(String title, String description, String type, boolean reportToAuthorities) {
        if (mediaUri != null) {
            StorageReference fileRef = storageRef.child(System.currentTimeMillis() + "." + getFileExtension(mediaUri));
            fileRef.putFile(mediaUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String mediaUrl = uri.toString();
                        saveReportToFirestore(title, description, type, reportToAuthorities, mediaUrl, mediaType);
                    }))
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        sendReportButton.setEnabled(true);
                        Toast.makeText(this, "Error al subir media: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        // Guardar sin media si falla la subida
                        saveReportToFirestore(title, description, type, reportToAuthorities, null, null);
                    });
        } else {
            saveReportToFirestore(title, description, type, reportToAuthorities, null, null);
        }
    }

    private void saveReportToFirestore(String title, String description, String type, boolean reportToAuthorities, String mediaUrl, String mediaType) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            sendReportButton.setEnabled(true);
            return;
        }

        // Crear un objeto Reporte
        Report newReport = new Report(
                UUID.randomUUID().toString(), // ID único para el reporte
                currentUser.getUid(),
                currentUser.getEmail(), // O el nombre de usuario/nombre completo si lo obtienes de Firestore
                title,
                description,
                type,
                currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                System.currentTimeMillis(),
                "pendiente", // Estado inicial
                mediaUrl,
                mediaType,
                reportToAuthorities
        );

        db.collection("reports")
                .add(newReport) // Firestore generará un ID de documento
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    sendReportButton.setEnabled(true);
                    Toast.makeText(ReportProblemActivity.this, "Reporte enviado con éxito!", Toast.LENGTH_SHORT).show();
                    finish(); // Cierra la actividad de reporte
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    sendReportButton.setEnabled(true);
                    Toast.makeText(ReportProblemActivity.this, "Error al enviar reporte: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    // Si falla Firestore, podríamos intentar guardar localmente de nuevo
                    saveReportLocally(title, description, type, reportToAuthorities);
                });
    }

    private void saveReportLocally(String title, String description, String type, boolean reportToAuthorities) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no autenticado para guardar localmente.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            sendReportButton.setEnabled(true);
            return;
        }

        ReporteLocal localReport = new ReporteLocal(
                title,
                description,
                mediaUri != null ? mediaUri.toString() : null, // Guarda la URI local del archivo
                type,
                currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                System.currentTimeMillis(),
                "pendiente_envio", // Estado para indicar que debe ser enviado luego
                currentUser.getUid()
        );

        // Ejecutar la operación de base de datos en un hilo separado
        AppDatabase.getDatabase(this).reporteDao().insert(localReport);
        Toast.makeText(this, "No hay conexión. Reporte guardado localmente.", Toast.LENGTH_LONG).show();
        progressBar.setVisibility(View.GONE);
        sendReportButton.setEnabled(true);
        finish(); // Cierra la actividad de reporte
    }

    // Helper para obtener la extensión del archivo (para Firebase Storage)
    private String getFileExtension(Uri uri) {
        String type = getContentResolver().getType(uri);
        if (type != null) {
            return type.substring(type.lastIndexOf('/') + 1);
        }
        return "jpg"; // Por defecto, si no se puede determinar
    }
}