package com.example.miprimeraaplicacion;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText editTextFullName, editTextEmail, editTextUsername, editTextPassword, editTextConfirmPassword;
    private ImageView imageViewProfile;
    private Button buttonRegister;
    private TextView textViewLogin, textViewError;

    private DBLocal dbLocal;

    private Uri profileImageUri;
    private String currentPhotoPath;

    private static final String TAG = "CreateAccountActivity";

    private static final int PERMISSION_GALLERY_REQUEST_CODE = 100;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 101;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    profileImageUri = result.getData().getData();
                    Picasso.get().load(profileImageUri).into(imageViewProfile);
                    currentPhotoPath = null;
                }
            }
    );

    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success) {
                    Picasso.get().load(profileImageUri).into(imageViewProfile);
                } else {
                    profileImageUri = null;
                    currentPhotoPath = null;
                    Toast.makeText(this, "No se tomó ninguna foto.", Toast.LENGTH_SHORT).show();
                }
            }
    );


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        dbLocal = new DBLocal(this);

        editTextFullName = findViewById(R.id.editTextFullName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        imageViewProfile = findViewById(R.id.imageViewProfile);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogin = findViewById(R.id.textViewLogin);
        textViewError = findViewById(R.id.textViewError);

        imageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageSourceDialog();
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUserLocal();
            }
        });

        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Imagen de Perfil");
        builder.setItems(new CharSequence[]{"Tomar Foto", "Elegir de Galería"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        checkAndRequestCameraPermission();
                        break;
                    case 1:
                        checkAndRequestGalleryPermission();
                        break;
                }
            }
        });
        builder.show();
    }

    private void checkAndRequestGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_GALLERY_REQUEST_CODE);
            } else {
                openImageChooser();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_GALLERY_REQUEST_CODE);
            } else {
                openImageChooser();
            }
        }
    }

    private void checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA_REQUEST_CODE);
        } else {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CAMERA_REQUEST_CODE);
            } else {
                dispatchTakePictureIntent();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == PERMISSION_GALLERY_REQUEST_CODE) {
                openImageChooser();
            } else if (requestCode == PERMISSION_CAMERA_REQUEST_CODE) {
                dispatchTakePictureIntent();
            }
        } else {
            String permissionName = "";
            if (requestCode == PERMISSION_GALLERY_REQUEST_CODE) {
                permissionName = "acceder a la galería";
            } else if (requestCode == PERMISSION_CAMERA_REQUEST_CODE) {
                permissionName = "acceder a la cámara";
            }
            Toast.makeText(this, "Permiso denegado para " + permissionName + ".", Toast.LENGTH_SHORT).show();
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) {
            Log.e(TAG, "External storage directory is null. Cannot create image file.");
            throw new IOException("External storage directory not available.");
        }
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "Error al crear archivo de imagen para la cámara: " + ex.getMessage());
                Toast.makeText(this, "Error: No se pudo crear archivo para la foto.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (photoFile != null) {
                profileImageUri = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        photoFile);
                takePictureLauncher.launch(profileImageUri);
            }
        } else {
            Toast.makeText(this, "No hay aplicación de cámara disponible.", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerUserLocal() {
        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(username) ||
                TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            textViewError.setText("Por favor, completa todos los campos.");
            textViewError.setVisibility(View.VISIBLE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            textViewError.setText("Las contraseñas no coinciden.");
            textViewError.setVisibility(View.VISIBLE);
            return;
        }

        if (password.length() < 6) {
            textViewError.setText("La contraseña debe tener al menos 6 caracteres.");
            textViewError.setVisibility(View.VISIBLE);
            return;
        }

        textViewError.setVisibility(View.GONE);

        if (dbLocal.checkUserExists(email)) {
            textViewError.setText("Este email ya está registrado localmente. Intenta iniciar sesión o usa otro email.");
            textViewError.setVisibility(View.VISIBLE);
            Log.d(TAG, "Intento de registro con email ya existente: " + email);
            return;
        }

        String userId = UUID.randomUUID().toString();
        String profileImageUrl = (profileImageUri != null) ? profileImageUri.toString() : "";

        // Inicializa las variables que no se obtienen directamente del UI de registro
        // Puedes obtenerlas de otros campos, o darles valores por defecto aquí.
        String phone = ""; // Si no hay campo de teléfono en el registro, usa cadena vacía o null
        String address = ""; // Si no hay campo de dirección, usa cadena vacía o null
        int reportsCount = 0; // Por defecto al registrar una nueva cuenta
        boolean showFullNamePublic = false; // Por defecto
        boolean showProfilePhotoInComments = false; // Por defecto
        boolean showEmailPublic = false; // Por defecto
        boolean showPhonePublic = false; // Por defecto


        // ¡ORDEN CORREGIDO DE ARGUMENTOS para que coincida con el constructor de User!
        User newUser = new User(
                userId,
                username, // Segundo argumento en el constructor de User
                email,    // Tercer argumento en el constructor de User
                password, // Cuarto argumento en el constructor de User
                fullName,
                phone,
                address,
                profileImageUrl,
                reportsCount,
                showFullNamePublic,
                showProfilePhotoInComments,
                showEmailPublic,
                showPhonePublic
        );

        if (dbLocal.addUser(newUser)) {
            Toast.makeText(CreateAccountActivity.this, "Cuenta creada exitosamente (localmente).", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Usuario registrado localmente: " + email);

            Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            textViewError.setText("Error al registrar la cuenta localmente. Intenta de nuevo.");
            textViewError.setVisibility(View.VISIBLE);
            Log.e(TAG, "Error desconocido al registrar usuario localmente: " + email);
        }
    }
}