package com.example.miprimeraaplicacion;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.miprimeraaplicacion.R;
import com.example.miprimeraaplicacion.adapters.MessageAdapter; // Necesitarás crear este adaptador
import com.example.miprimeraaplicacion.models.Message; // Necesitarás crear esta clase
import com.example.miprimeraaplicacion.models.User; // Para obtener el nombre del usuario

import java.util.ArrayList;
import java.util.List;

public class IndividualChatActivity extends AppCompatActivity {

    private Toolbar chatToolbar;
    private TextView toolbarTitle;
    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private Button sendMessageButton;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private String topicId;
    private String topicName;
    private String currentUserName; // Nombre del usuario actual

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_chat);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Obtener el ID del tema del chat y el nombre de la Intent
        topicId = getIntent().getStringExtra("topicId");
        topicName = getIntent().getStringExtra("topicName");

        if (topicId == null || topicName == null || currentUser == null) {
            Toast.makeText(this, "Error: No se pudo iniciar el chat. Asegúrate de estar logueado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadCurrentUserProfile(); // Cargar el nombre del usuario actual
        listenForMessages();

        sendMessageButton.setOnClickListener(v -> sendMessage());
    }

    private void initViews() {
        chatToolbar = findViewById(R.id.chat_toolbar);
        toolbarTitle = findViewById(R.id.toolbar_title);
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        messageEditText = findViewById(R.id.message_edit_text);
        sendMessageButton = findViewById(R.id.send_message_button);
        progressBar = findViewById(R.id.chat_progress_bar);
    }

    private void setupToolbar() {
        setSupportActionBar(chatToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Deshabilita el título por defecto
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Habilita el botón de retroceso
        }
        toolbarTitle.setText(topicName); // Establece el título del chat
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Maneja el botón de retroceso de la toolbar
        return true;
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList, currentUser.getUid());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Para que el RecyclerView se desplace automáticamente al final
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(messageAdapter);
    }

    private void loadCurrentUserProfile() {
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null && user.getFullName() != null) {
                            currentUserName = user.getFullName();
                        } else {
                            currentUserName = currentUser.getEmail(); // Fallback si no hay nombre completo
                        }
                    } else {
                        currentUserName = currentUser.getEmail(); // Fallback
                    }
                })
                .addOnFailureListener(e -> {
                    currentUserName = currentUser.getEmail(); // Fallback en caso de error
                    Toast.makeText(this, "No se pudo cargar el nombre del usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void listenForMessages() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("chat_topics").document(topicId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(IndividualChatActivity.this, "Error al cargar mensajes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        return;
                    }

                    if (snapshots != null) {
                        messageList.clear();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Message message = doc.toObject(Message.class);
                            if (message != null) {
                                messageList.add(message);
                            }
                        }
                        messageAdapter.notifyDataSetChanged();
                        chatRecyclerView.scrollToPosition(messageList.size() - 1); // Desplazarse al último mensaje
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();

        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "El mensaje no puede estar vacío.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser == null || currentUserName == null) {
            Toast.makeText(this, "Error: Usuario no autenticado o nombre no disponible.", Toast.LENGTH_SHORT).show();
            return;
        }

        Message newMessage = new Message(
                currentUser.getUid(),
                currentUserName,
                messageText,
                System.currentTimeMillis()
        );

        db.collection("chat_topics").document(topicId)
                .collection("messages")
                .add(newMessage)
                .addOnSuccessListener(documentReference -> {
                    messageEditText.setText(""); // Limpiar el campo de texto
                    // Actualizar el último mensaje en el tema del chat
                    updateChatTopicLastMessage(messageText, newMessage.getTimestamp());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(IndividualChatActivity.this, "Error al enviar mensaje: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateChatTopicLastMessage(String lastMessage, long timestamp) {
        DocumentReference topicRef = db.collection("chat_topics").document(topicId);
        topicRef.update("lastMessage", lastMessage, "lastMessageTimestamp", timestamp)
                .addOnFailureListener(e -> {
                    // Manejar el error de actualización, quizás loggear
                    // Toast.makeText(IndividualChatActivity.this, "Error al actualizar tema de chat: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}