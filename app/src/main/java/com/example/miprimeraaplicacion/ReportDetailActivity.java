package com.example.miprimeraaplicacion;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportDetailActivity extends AppCompatActivity {

    private ImageView imageViewReport;
    private TextView textViewTitle, textViewDescription, textViewLocation, textViewDateTime, textViewStatus;
    private LinearLayout layoutUserActions, layoutHistory, layoutComments;
    private View buttonEdit;
    private View buttonDelete;
    private View buttonPostComment;
    private EditText editTextComment;

    private String reportId;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ObjectInputStream.GetField Picasso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Habilita el botón de regreso
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

        // Obtener el ID del reporte del Intent
        if (getIntent().hasExtra("reportId")) {
            reportId = getIntent().getStringExtra("reportId");
            loadReportDetails(reportId);
        } else {
            Toast.makeText(this, "ID de reporte no proporcionado.", Toast.LENGTH_SHORT).show();
            finish(); // Cierra la actividad si no hay ID
        }

        buttonEdit.setOnClickListener(v -> {
            // Lógica para editar el reporte (abrir otra actividad de edición o un diálogo)
            Toast.makeText(ReportDetailActivity.this, "Funcionalidad de editar reporte (por implementar)", Toast.LENGTH_SHORT).show();
        });

        buttonDelete.setOnClickListener(v -> deleteReport());

        buttonPostComment.setOnClickListener(v -> postComment());
    }

    // Método para manejar el botón de regreso de la Toolbar
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Cierra esta actividad y regresa a la anterior
        return true;
    }

    private void loadReportDetails(String id) {
        db.collection("reports").document(id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Mapear los datos del reporte a los TextViews
                            String title = documentSnapshot.getString("type"); // Usamos el tipo como título
                            String description = documentSnapshot.getString("description");
                            String location = documentSnapshot.getString("location");
                            Long timestamp = documentSnapshot.getLong("timestamp");
                            String status = documentSnapshot.getString("status");
                            String imageUrl = documentSnapshot.getString("imageUrl");
                            String userId = documentSnapshot.getString("userId");

                            textViewTitle.setText(title);
                            textViewDescription.setText(description);
                            textViewLocation.setText("Ubicación: " + location);

                            if (timestamp != null) {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                                textViewDateTime.setText("Fecha y Hora: " + sdf.format(new Date(timestamp)));
                            } else {
                                textViewDateTime.setText("Fecha y Hora: Desconocida");
                            }

                            textViewStatus.setText("Estado: " + status);
                            // Asignar color al estado
                            if ("Resuelto".equals(status)) {
                                textViewStatus.setTextColor(ContextCompat.getColor(ReportDetailActivity.this, android.R.color.holo_green_dark));
                            } else if ("En Proceso".equals(status)) {
                                textViewStatus.setTextColor(ContextCompat.getColor(ReportDetailActivity.this, android.R.color.holo_orange_dark));
                            } else {
                                textViewStatus.setTextColor(ContextCompat.getColor(ReportDetailActivity.this, android.R.color.holo_red_dark));
                            }

                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                imageViewReport.setVisibility(View.VISIBLE);
                            } else {
                                imageViewReport.setVisibility(View.GONE);
                            }

                            // Mostrar botones de Editar/Eliminar si es el reporte del usuario actual
                            if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getUid().equals(userId)) {
                                layoutUserActions.setVisibility(View.VISIBLE);
                            } else {
                                layoutUserActions.setVisibility(View.GONE);
                            }

                            // Cargar historial de cambios (simplificado, solo para demostrar)
                            loadHistory(documentSnapshot);

                            // Cargar comentarios
                            loadComments(documentSnapshot);

                        } else {
                            Toast.makeText(ReportDetailActivity.this, "Reporte no encontrado.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ReportDetailActivity.this, "Error al cargar el reporte: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void deleteReport() {
        // Implementa un diálogo de confirmación antes de eliminar
        db.collection("reports").document(reportId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ReportDetailActivity.this, "Reporte eliminado exitosamente.", Toast.LENGTH_SHORT).show();
                        finish(); // Regresar a la pantalla anterior (Mis Reportes o Inicio)
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ReportDetailActivity.this, "Error al eliminar reporte: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void postComment() {
        String commentText = editTextComment.getText().toString().trim();
        if (TextUtils.isEmpty(commentText)) {
            Toast.makeText(this, "Por favor, escribe un comentario.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Debes iniciar sesión para comentar.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        // En una aplicación real, obtendrías el nombre de usuario de Firestore
        String userName = "Usuario Anónimo"; // Placeholder

        Map<String, Object> comment = new HashMap<>();
        comment.put("userId", userId);
        comment.put("userName", userName);
        comment.put("commentText", commentText);
        comment.put("timestamp", System.currentTimeMillis());

        db.collection("reports").document(reportId)
                .update("comments", FieldValue.arrayUnion(comment)) // Añadir el comentario a un array
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ReportDetailActivity.this, "Comentario publicado.", Toast.LENGTH_SHORT).show();
                        editTextComment.setText(""); // Limpiar el campo
                        // Recargar comentarios (o añadir el nuevo directamente)
                        loadReportDetails(reportId); // Recargar toda la pantalla para actualizar comentarios
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ReportDetailActivity.this, "Error al publicar comentario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void loadHistory(DocumentSnapshot documentSnapshot) {
        layoutHistory.removeAllViews(); // Limpiar vistas anteriores

        // Suponiendo que tienes un campo "history" como un array de Map en Firestore
        // Si no tienes un campo de historial explícito, puedes simularlo o implementarlo
        // de otra manera. Para este ejemplo, solo cargaremos un mensaje.
        TextView historyItem = new TextView(this);
        historyItem.setText("Reporte creado el " + textViewDateTime.getText().toString());
        historyItem.setTextColor(ContextCompat.getColor(this, R.color.black)); // Asegúrate de que este color exista
        layoutHistory.addView(historyItem);

        // Puedes añadir más lógica aquí si tienes un historial real en Firestore
        // List<Map<String, Object>> historyList = (List<Map<String, Object>>) documentSnapshot.get("history");
        // if (historyList != null && !historyList.isEmpty()) { ... }
    }

    private void loadComments(DocumentSnapshot documentSnapshot) {
        layoutComments.removeAllViews(); // Limpiar vistas anteriores

        // Suponiendo que los comentarios están en un campo "comments" como un array de Map
        List<Map<String, Object>> commentsList = (List<Map<String, Object>>) documentSnapshot.get("comments");

        if (commentsList != null && !commentsList.isEmpty()) {
            for (Map<String, Object> comment : commentsList) {
                String userName = (String) comment.get("userName");
                String commentText = (String) comment.get("commentText");
                Long timestamp = (Long) comment.get("timestamp");

                if (userName != null && commentText != null && timestamp != null) {
                    TextView commentView = new TextView(this);
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault());
                    String formattedDate = sdf.format(new Date(timestamp));
                    commentView.setText(userName + " (" + formattedDate + "):\n" + commentText + "\n");
                    commentView.setTextColor(ContextCompat.getColor(this, R.color.black));
                    commentView.setPadding(0, 4, 0, 4); // Pequeño padding

                    layoutComments.addView(commentView);
                }
            }
        } else {
            TextView noComments = new TextView(this);
            noComments.setText("Sé el primero en comentar.");
            noComments.setTextColor(ContextCompat.getColor(this, R.color.black));
            layoutComments.addView(noComments);
        }
    }
}