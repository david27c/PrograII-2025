package com.example.miprimeraaplicacion;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DenunciaDetailActivity extends AppCompatActivity {

    public static final String EXTRA_DENUNCIA_ID = "denuncia_id";
    private static final String TAG = "DenunciaDetailActivity";

    private ImageView imageViewReport;
    private TextView textViewTitle, textViewDescription, textViewLocation, textViewDateTime, textViewStatus;
    private LinearLayout layoutUserActions, layoutHistory, layoutComments;
    private EditText editTextComment;
    private View buttonEdit, buttonDelete, buttonPostComment;

    private String denunciaId;
    private DBLocal dbLocal;

    private static final String PREF_USER_ID = "logged_in_user_id";
    private String currentUserId;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_denuncia_detail);

        dbLocal = new DBLocal(this);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        currentUserId = sharedPref.getString(PREF_USER_ID, null);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalle del Reporte");
        }

        imageViewReport = findViewById(R.id.imageViewReport);
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewLocation = findViewById(R.id.textViewLocation);
        textViewDateTime = findViewById(R.id.textViewDateTime);
        textViewStatus = findViewById(R.id.textViewStatus);

        layoutUserActions = findViewById(R.id.layoutUserActions);
        layoutHistory = findViewById(R.id.layoutHistory);
        layoutComments = findViewById(R.id.layoutComments);

        buttonEdit = findViewById(R.id.buttonEdit);
        buttonDelete = findViewById(R.id.buttonDelete);
        buttonPostComment = findViewById(R.id.buttonPostComment);
        editTextComment = findViewById(R.id.editTextComment);

        if (getIntent().hasExtra(EXTRA_DENUNCIA_ID)) {
            denunciaId = getIntent().getStringExtra(EXTRA_DENUNCIA_ID);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                loadDenunciaDetails(denunciaId);
            }
        } else {
            Toast.makeText(this, "ID de denuncia no proporcionado.", Toast.LENGTH_SHORT).show();
            finish();
        }

        buttonEdit.setOnClickListener(v -> {
            Toast.makeText(DenunciaDetailActivity.this, "Funcionalidad de editar reporte (por implementar)", Toast.LENGTH_SHORT).show();
        });

        buttonDelete.setOnClickListener(v -> deleteDenuncia());

        buttonPostComment.setOnClickListener(v -> postComment());

        textViewLocation.setOnClickListener(v -> {
            if (textViewLocation.getText().toString().startsWith("Ubicación: Lat:")) {
                String locationText = textViewLocation.getText().toString();
                String[] parts = locationText.split("Lat: | Lon: ");
                try {
                    double latitude = Double.parseDouble(parts[1].split(",")[0].trim());
                    double longitude = Double.parseDouble(parts[2].trim());
                    String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(Ubicación de Denuncia)",
                            latitude, longitude, latitude, longitude);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mapIntent);
                    } else {
                        Toast.makeText(DenunciaDetailActivity.this, "No se encontró una aplicación de mapas.", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error al parsear latitud/longitud: " + e.getMessage());
                    Toast.makeText(DenunciaDetailActivity.this, "Ubicación inválida para abrir en mapa.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadDenunciaDetails(String id) {
        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(this, "Debes iniciar sesión para ver los detalles.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Denuncia denuncia = dbLocal.obtenerDenunciaPorId(id);
        if (denuncia != null) {
            textViewTitle.setText(denuncia.getTitulo());
            textViewDescription.setText(denuncia.getDescripcion());
            textViewLocation.setText(String.format(Locale.getDefault(), "Ubicación: Lat: %.4f, Lon: %.4f",
                    denuncia.getLatitud(), denuncia.getLongitud()));

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            if (denuncia.getFechaHora() != null) {
                textViewDateTime.setText("Fecha y Hora: " + sdf.format(denuncia.getFechaHora()));
            } else {
                textViewDateTime.setText("Fecha y Hora: N/A");
            }

            textViewStatus.setText("Estado: " + denuncia.getEstado());
            String status = denuncia.getEstado();
            if ("Resuelto".equals(status)) {
                textViewStatus.setTextColor(ContextCompat.getColor(DenunciaDetailActivity.this, android.R.color.holo_green_dark));
            } else if ("En Proceso".equals(status)) {
                textViewStatus.setTextColor(ContextCompat.getColor(DenunciaDetailActivity.this, android.R.color.holo_orange_dark));
            } else { // Pendiente o cualquier otro
                textViewStatus.setTextColor(ContextCompat.getColor(DenunciaDetailActivity.this, android.R.color.holo_red_dark));
            }

            if (denuncia.getUrlImagen() != null && !denuncia.getUrlImagen().isEmpty()) {
                imageViewReport.setVisibility(View.VISIBLE);
                // CORRECCIÓN AQUÍ: Llamada correcta a Glide
                Glide.with(DenunciaDetailActivity.this) // Usar el contexto de la actividad
                        .load(denuncia.getUrlImagen())
                        .placeholder(R.drawable.placeholder_image) // Asegúrate de tener este drawable
                        .error(R.drawable.placeholder_image) // Asegúrate de tener este drawable
                        .into(imageViewReport);
            } else {
                imageViewReport.setVisibility(View.GONE);
            }

            if (currentUserId != null && currentUserId.equals(denuncia.getIdUsuario())) {
                layoutUserActions.setVisibility(View.VISIBLE);
            } else {
                layoutUserActions.setVisibility(View.GONE);
            }

            loadHistory(denuncia);
            loadComments(denuncia);

        } else {
            Toast.makeText(DenunciaDetailActivity.this, "Denuncia no encontrada.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void deleteDenuncia() {
        boolean deleted = dbLocal.eliminarDenuncia(denunciaId);
        if (deleted) {
            Toast.makeText(DenunciaDetailActivity.this, "Denuncia eliminada exitosamente.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(DenunciaDetailActivity.this, "Error al eliminar denuncia.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error deleting denuncia with ID: " + denunciaId);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void postComment() {
        String commentText = editTextComment.getText().toString().trim();
        if (TextUtils.isEmpty(commentText)) {
            Toast.makeText(this, "Por favor, escribe un comentario.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(this, "Debes iniciar sesión para comentar.", Toast.LENGTH_SHORT).show();
            return;
        }

        User currentUser = dbLocal.obtenerUsuarioPorId(currentUserId);
        String userName = (currentUser != null && currentUser.getUsername() != null && !currentUser.getUsername().isEmpty()) ? currentUser.getUsername() : "Usuario Anónimo";

        Map<String, Object> comment = new HashMap<>();
        comment.put("userId", currentUserId);
        comment.put("userName", userName);
        comment.put("commentText", commentText);
        comment.put("timestamp", System.currentTimeMillis());

        boolean commentAdded = dbLocal.agregarComentarioADenuncia(denunciaId, comment);
        if (commentAdded) {
            Toast.makeText(DenunciaDetailActivity.this, "Comentario publicado.", Toast.LENGTH_SHORT).show();
            editTextComment.setText("");
            loadDenunciaDetails(denunciaId);
        } else {
            Toast.makeText(DenunciaDetailActivity.this, "Error al publicar comentario.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error posting comment for denuncia: " + denunciaId);
        }
    }

    @SuppressLint("SetTextI18n")
    private void loadHistory(Denuncia denuncia) {
        layoutHistory.removeAllViews();

        TextView historyItem = new TextView(this);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        if (denuncia.getFechaHora() != null) {
            historyItem.setText("Reporte creado el " + sdf.format(denuncia.getFechaHora()));
        } else {
            historyItem.setText("Reporte creado el: N/A");
        }
        historyItem.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        layoutHistory.addView(historyItem);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadComments(Denuncia denuncia) {
        layoutComments.removeAllViews();

        List<Map<String, Object>> commentsList = denuncia.getComments();

        if (commentsList != null && !commentsList.isEmpty()) {
            commentsList.sort((c1, c2) -> {
                Long ts1 = (Long) c1.get("timestamp");
                Long ts2 = (Long) c2.get("timestamp");
                if (ts1 == null || ts2 == null) return 0;
                return ts1.compareTo(ts2);
            });

            for (Map<String, Object> comment : commentsList) {
                String userName = (String) comment.get("userName");
                String commentText = (String) comment.get("commentText");
                Long timestamp = (Long) comment.get("timestamp");

                if (userName != null && commentText != null && timestamp != null) {
                    TextView commentView = new TextView(this);
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault());
                    String formattedDate = sdf.format(new Date(timestamp));
                    commentView.setText(userName + " (" + formattedDate + "):\n" + commentText + "\n");
                    commentView.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                    commentView.setPadding(0, 8, 0, 8);

                    layoutComments.addView(commentView);
                }
            }
        } else {
            TextView noComments = new TextView(this);
            noComments.setText("Sé el primero en comentar.");
            noComments.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            noComments.setPadding(0, 8, 0, 8);
            layoutComments.addView(noComments);
        }
    }
}