package com.example.miprimeraaplicacion;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;
import com.example.miprimeraaplicacion.R;
import com.example.miprimeraaplicacion.adapters.CommentAdapter; // Necesitarás crear este adaptador
import com.example.miprimeraaplicacion.models.Comment; // Necesitarás crear esta clase
import com.example.miprimeraaplicacion.models.Report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DetailReportActivity extends AppCompatActivity {

    private ImageView reportImagePreview;
    private VideoView reportVideoPreview;
    private TextView titleTextView, descriptionTextView, locationTextView, dateTimeTextView, statusTextView;
    private TextView historyTextView; // Para el historial de cambios
    private LinearLayout userActionsLayout;
    private Button editReportButton, deleteReportButton, sendCommentButton;
    private EditText commentEditText;
    private RecyclerView commentsRecyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String reportId;
    private Report currentReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_report);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        // Obtener el ID del reporte de la Intent
        reportId = getIntent().getStringExtra("reportId");

        if (reportId != null) {
            loadReportDetails(reportId);
            loadComments(reportId);
        } else {
            Toast.makeText(this, "Error: No se encontró el ID del reporte.", Toast.LENGTH_SHORT).show();
            finish();
        }

        sendCommentButton.setOnClickListener(v -> postComment());
        editReportButton.setOnClickListener(v -> editReport());
        deleteReportButton.setOnClickListener(v -> confirmDeleteReport());
    }

    private void initViews() {
        reportImagePreview = findViewById(R.id.detail_report_image);
        reportVideoPreview = findViewById(R.id.detail_report_video);
        titleTextView = findViewById(R.id.detail_report_title);
        descriptionTextView = findViewById(R.id.detail_report_description);
        locationTextView = findViewById(R.id.detail_report_location);
        dateTimeTextView = findViewById(R.id.detail_report_date_time);
        statusTextView = findViewById(R.id.detail_report_status);
        historyTextView = findViewById(R.id.history_tv);
        userActionsLayout = findViewById(R.id.user_actions_layout);
        editReportButton = findViewById(R.id.edit_report_button);
        deleteReportButton = findViewById(R.id.delete_report_button);
        sendCommentButton = findViewById(R.id.send_comment_button);
        commentEditText = findViewById(R.id.comment_et);
        commentsRecyclerView = findViewById(R.id.comments_recycler_view);

        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(commentList); // Implementa tu CommentAdapter
        commentsRecyclerView.setAdapter(commentAdapter);
    }

    private void loadReportDetails(String id) {
        db.collection("reports").document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentReport = documentSnapshot.toObject(Report.class);
                        if (currentReport != null) {
                            displayReportDetails(currentReport);
                            checkUserPermissions(currentReport.getUserId());
                        }
                    } else {
                        Toast.makeText(this, "Reporte no encontrado.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar reporte: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayReportDetails(Report report) {
        titleTextView.setText(report.getTitle());
        descriptionTextView.setText(report.getDescription());
        locationTextView.setText(String.format("Ubicación: %.4f, %.4f", report.getLatitude(), report.getLongitude()));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        dateTimeTextView.setText("Fecha y Hora: " + sdf.format(new Date(report.getTimestamp())));
        statusTextView.setText("Estado: " + report.getStatus());

        if (report.getMediaUrl() != null && !report.getMediaUrl().isEmpty()) {
            if ("image".equals(report.getMediaType())) {
                reportImagePreview.setVisibility(View.VISIBLE);
                reportVideoPreview.setVisibility(View.GONE);
                Picasso.get().load(report.getMediaUrl()).into(reportImagePreview);
            } else if ("video".equals(report.getMediaType())) {
                reportVideoPreview.setVisibility(View.VISIBLE);
                reportImagePreview.setVisibility(View.GONE);
                reportVideoPreview.setVideoURI(Uri.parse(report.getMediaUrl()));
                reportVideoPreview.start(); // Puedes añadir controles de video si lo deseas
            }
        } else {
            reportImagePreview.setVisibility(View.GONE);
            reportVideoPreview.setVisibility(View.GONE);
        }

        // Aquí iría la lógica para cargar el historial de cambios (podría ser un sub-colección en Firestore)
        historyTextView.setText("Historial no implementado aún."); // Placeholder
    }

    private void checkUserPermissions(String reportUserId) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.getUid().equals(reportUserId)) {
            userActionsLayout.setVisibility(View.VISIBLE); // Mostrar botones Editar/Eliminar
        } else {
            userActionsLayout.setVisibility(View.GONE);
        }
    }

    private void editReport() {
        // Lógica para editar el reporte
        // Podrías lanzar una nueva Activity similar a ReportProblemActivity
        // pero precargada con los datos del reporte actual.
        Toast.makeText(this, "Funcionalidad de editar reporte por implementar.", Toast.LENGTH_SHORT).show();
    }

    private void confirmDeleteReport() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Reporte")
                .setMessage("¿Estás seguro de que quieres eliminar este reporte? Esta acción no se puede deshacer.")
                .setPositiveButton("Sí, Eliminar", (dialog, which) -> deleteReport())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteReport() {
        db.collection("reports").document(reportId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(DetailReportActivity.this, "Reporte eliminado con éxito.", Toast.LENGTH_SHORT).show();
                    finish(); // Cierra esta actividad y vuelve a la anterior (Mis Reportes o Inicio)
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DetailReportActivity.this, "Error al eliminar reporte: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadComments(String id) {
        db.collection("reports").document(id).collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(DetailReportActivity.this, "Error al cargar comentarios: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshots != null) {
                        commentList.clear();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Comment comment = doc.toObject(Comment.class);
                            if (comment != null) {
                                commentList.add(comment);
                            }
                        }
                        commentAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void postComment() {
        String commentText = commentEditText.getText().toString().trim();
        if (TextUtils.isEmpty(commentText)) {
            commentEditText.setError("El comentario no puede estar vacío.");
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesión para comentar.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Puedes obtener el nombre del usuario desde Firestore si lo tienes en tu colección "users"
        // Por simplicidad, usaremos el email aquí.
        String authorName = currentUser.getEmail(); // O currentReport.getUserName() si quieres mostrar el nombre del autor del reporte original

        Comment newComment = new Comment(
                currentUser.getUid(),
                authorName, // Podrías buscar el nombre completo del usuario si lo tienes en Firestore
                commentText,
                System.currentTimeMillis()
        );

        db.collection("reports").document(reportId).collection("comments")
                .add(newComment)
                .addOnSuccessListener(documentReference -> {
                    commentEditText.setText(""); // Limpiar el campo de texto
                    Toast.makeText(DetailReportActivity.this, "Comentario publicado.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DetailReportActivity.this, "Error al publicar comentario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}