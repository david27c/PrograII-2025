package com.example.miprimeraaplicacion;

import android.Manifest;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText editTextFullName, editTextEmail, editTextUsername, editTextPassword, editTextConfirmPassword;
    private ImageView imageViewProfile;
    private Button buttonRegister;
    private TextView textViewLogin, textViewError;

    private FirebaseAuth mAuth;
    private DBFirebase dbFirebase;

    private Uri profileImageUri; // Para almacenar la URI local de la imagen seleccionada o tomada
    private String currentPhotoPath; // Para guardar la ruta del archivo de la foto tomada con la cámara

    private static final String TAG = "CreateAccountActivity";

    // Códigos de solicitud de permisos
    private static final int PERMISSION_GALLERY_REQUEST_CODE = 100;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 101;

    // ActivityResultLauncher para manejar la selección de imagen de la galería
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    profileImageUri = result.getData().getData();
                    Picasso.get().load(profileImageUri).into(imageViewProfile);
                    currentPhotoPath = null; // Resetear la ruta de la cámara si se elige de galería
                }
            }
    );

    // ActivityResultLauncher para manejar la toma de foto con la cámara
    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success) {
                    // La imagen ya está guardada en profileImageUri (que fue preparada antes de lanzar la cámara)
                    Picasso.get().load(profileImageUri).into(imageViewProfile);
                } else {
                    // Si la foto no se tomó o hubo un error, se puede limpiar la URI temporal
                    profileImageUri = null;
                    currentPhotoPath = null;
                    Toast.makeText(this, "No se tomó ninguna foto.", Toast.LENGTH_SHORT).show();
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mAuth = FirebaseAuth.getInstance();
        dbFirebase = new DBFirebase(this);

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
                showImageSourceDialog(); // Nuevo método para mostrar el diálogo
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // --- Métodos para seleccionar fuente de imagen ---

    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Imagen de Perfil");
        builder.setItems(new CharSequence[]{"Tomar Foto", "Elegir de Galería"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Tomar Foto
                        checkAndRequestCameraPermission();
                        break;
                    case 1: // Elegir de Galería
                        checkAndRequestGalleryPermission();
                        break;
                }
            }
        });
        builder.show();
    }

    // --- Métodos de permisos ---

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
            // También verifica permiso de escritura para versiones antiguas de Android si la cámara guarda en almacenamiento externo
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

    // --- Métodos para abrir selectores de imagen ---

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private File createImageFile() throws IOException {
        // Crear un nombre de archivo de imagen con marca de tiempo
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) {
            Log.e(TAG, "External storage directory is null. Cannot create image file.");
            throw new IOException("External storage directory not available.");
        }
        File image = File.createTempFile(
                imageFileName,  /* prefijo */
                ".jpg",         /* sufijo */
                storageDir      /* directorio */
        );

        // Guardar una ruta de archivo: esto es para la ruta del archivo real
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        // Asegúrate de que haya una actividad de cámara para manejar el intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Crea el archivo donde debería ir la foto
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "Error al crear archivo de imagen para la cámara: " + ex.getMessage());
                Toast.makeText(this, "Error: No se pudo crear archivo para la foto.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Continúa solo si el archivo fue creado exitosamente
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

    // --- Métodos de registro y Firestore (ligeramente modificados para `profileImageUri`) ---

    private void registerUser() {
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

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                handleProfileImageAndUserData(user.getUid(), fullName, email, username);
                            }
                        } else {
                            String errorMessage = "Error al registrar la cuenta.";
                            Exception exception = task.getException();

                            if (exception != null) {
                                if (exception instanceof FirebaseAuthWeakPasswordException) {
                                    errorMessage = "La contraseña es muy débil. Debe tener al menos 6 caracteres y ser más compleja.";
                                } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                                    errorMessage = "El formato del correo electrónico es inválido.";
                                } else if (exception instanceof FirebaseAuthUserCollisionException) {
                                    errorMessage = "Este correo electrónico ya está registrado.";
                                } else {
                                    errorMessage += "\nDetalles: " + exception.getMessage();
                                }
                                Log.e(TAG, "Error de registro de Firebase: " + exception.getMessage(), exception);
                            } else {
                                Log.e(TAG, "Error de registro desconocido: la excepción es nula.");
                            }
                            textViewError.setText(errorMessage);
                            textViewError.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void handleProfileImageAndUserData(String userId, String fullName, String email, String username) {
        if (profileImageUri != null) {
            dbFirebase.subirImagenPerfil(profileImageUri, userId, new DBFirebase.ImageUploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    Log.d(TAG, "Imagen de perfil subida exitosamente: " + imageUrl);
                    initializeUserDocumentInFirestore(userId, email, fullName, username, imageUrl);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Error al subir imagen de perfil: " + e.getMessage(), e);
                    Toast.makeText(CreateAccountActivity.this, "Error al subir la imagen. La cuenta se creará sin foto de perfil.", Toast.LENGTH_LONG).show();
                    initializeUserDocumentInFirestore(userId, email, fullName, username, null);
                }
            });
        } else {
            initializeUserDocumentInFirestore(userId, email, fullName, username, null);
        }
    }

    private void initializeUserDocumentInFirestore(String userId, String email, String fullName, String username, String profileImageUrl) {
        dbFirebase.inicializarDocumentoUsuario(userId, email, new DBFirebase.VoidCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Documento de usuario inicializado con éxito en Firestore para UID: " + userId);

                Map<String, Object> updates = new HashMap<>();
                updates.put("fullName", fullName);
                updates.put("username", username);
                if (profileImageUrl != null) {
                    updates.put("profileImageUrl", profileImageUrl);
                }

                dbFirebase.actualizarPerfilUsuario(userId, updates, new DBFirebase.VoidCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(CreateAccountActivity.this, "Cuenta creada exitosamente y datos guardados.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(CreateAccountActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        String errorMessage = "Cuenta creada, pero hubo un error al actualizar el perfil: " + e.getMessage();
                        Log.e(TAG, errorMessage, e);
                        textViewError.setText(errorMessage);
                        textViewError.setVisibility(View.VISIBLE);
                        // A pesar del error en el perfil, la cuenta de Auth ya existe.
                        // Podrías decidir si permitir al usuario continuar o mostrar un error crítico.
                        Intent intent = new Intent(CreateAccountActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error al inicializar documento de usuario en Firestore: " + e.getMessage(), e);
                textViewError.setText("Error crítico al preparar el perfil del usuario. Intente nuevamente. Detalles: " + e.getMessage());
                textViewError.setVisibility(View.VISIBLE);

                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    currentUser.delete().addOnCompleteListener(deleteTask -> {
                        if (deleteTask.isSuccessful()) {
                            Log.d(TAG, "Cuenta de Auth eliminada debido a fallo en Firestore.");
                            Toast.makeText(CreateAccountActivity.this, "Error fatal: Cuenta no creada. Intente de nuevo.", Toast.LENGTH_LONG).show();
                        } else {
                            Log.e(TAG, "Error al eliminar cuenta de Auth después de fallo en Firestore: " + deleteTask.getException().getMessage());
                            Toast.makeText(CreateAccountActivity.this, "Error fatal: No se pudo completar el registro. Contacte a soporte.", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }
}