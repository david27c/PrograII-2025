package com.example.miprimeraaplicacion;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class IndividualChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMessages;
    private EditText editTextMessage;
    private ImageButton buttonSendMessage;
    private ProgressBar progressBarChat;
    private TextView textViewNoMessages;

    private String chatTopicId;
    private String chatTopicName;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CollectionReference messagesRef; // Referencia a la colección de mensajes del chat
    private ListenerRegistration messagesListener; // Listener para mensajes en tiempo real

    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    private String currentUserName = "Usuario Anónimo"; // Valor por defecto

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_chat);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSendMessage = findViewById(R.id.buttonSendMessage);
        progressBarChat = findViewById(R.id.progressBarChat);
        textViewNoMessages = findViewById(R.id.textViewNoMessages);

        // Obtener ID y nombre del tema de chat del Intent
        if (getIntent().hasExtra("chatTopicId") && getIntent().hasExtra("chatTopicName")) {
            chatTopicId = getIntent().getStringExtra("chatTopicId");
            chatTopicName = getIntent().getStringExtra("chatTopicName");
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Chat con " + chatTopicName);
            }
            messagesRef = db.collection("chatTopics").document(chatTopicId).collection("messages");
        } else {
            Toast.makeText(this, "Tema de chat no especificado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Configurar RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewMessages.setLayoutManager(layoutManager);
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);
        recyclerViewMessages.setAdapter(messageAdapter);

        // Obtener el nombre de usuario actual (para mostrar en los mensajes)
        loadCurrentUserName();

        // Listener para el botón de enviar mensaje
        buttonSendMessage.setOnClickListener(v -> sendMessage());

        // Iniciar la escucha de mensajes en tiempo real
        startListeningForMessages();
    }

    // Método para manejar el botón de regreso de la Toolbar
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadCurrentUserName() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Intenta obtener el nombre de usuario de tu colección 'users'
            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists() && documentSnapshot.getString("username") != null) {
                            currentUserName = documentSnapshot.getString("username");
                        } else if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                            currentUserName = currentUser.getDisplayName(); // Usa el nombre de Firebase si está disponible
                        } else {
                            currentUserName = "Usuario " + currentUser.getUid().substring(0, 4); // Nombre corto
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Si falla, usa el nombre por defecto
                        currentUserName = "Usuario Anónimo";
                    });
        }
    }


    private void startListeningForMessages() {
        progressBarChat.setVisibility(View.VISIBLE);
        textViewNoMessages.setVisibility(View.GONE);

        // Escuchar mensajes en tiempo real, ordenados por marca de tiempo
        messagesListener = messagesRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@androidx.annotation.Nullable QuerySnapshot snapshots,
                                        @androidx.annotation.Nullable FirebaseFirestoreException e) {
                        progressBarChat.setVisibility(View.GONE);
                        if (e != null) {
                            Toast.makeText(IndividualChatActivity.this, "Error al cargar mensajes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            textViewNoMessages.setText("Error al cargar mensajes.");
                            textViewNoMessages.setVisibility(View.VISIBLE);
                            return;
                        }

                        List<Message> newMessages = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) { // Iterar sobre DocumentReferences
                            Message message = doc.toObject(Message.class);
                            newMessages.add(message);
                        }
                        messageAdapter.setMessages(newMessages); // Actualizar toda la lista de mensajes

                        if (newMessages.isEmpty()) {
                            textViewNoMessages.setVisibility(View.VISIBLE);
                            recyclerViewMessages.setVisibility(View.GONE);
                        } else {
                            textViewNoMessages.setVisibility(View.GONE);
                            recyclerViewMessages.setVisibility(View.VISIBLE);
                            // Desplazarse al último mensaje
                            recyclerViewMessages.scrollToPosition(messageList.size() - 1);
                        }
                    }
                });
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();

        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "No puedes enviar un mensaje vacío.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesión para enviar mensajes.", Toast.LENGTH_SHORT).show();
            return;
        }

        String senderId = currentUser.getUid();
        long timestamp = System.currentTimeMillis();

        Message message = new Message(senderId, currentUserName, messageText, timestamp);

        messagesRef.add(message)
                .addOnSuccessListener(documentReference -> {
                    editTextMessage.setText(""); // Limpiar el campo de texto
                    // Ya que estamos escuchando en tiempo real, el mensaje se añadirá automáticamente al RecyclerView
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(IndividualChatActivity.this, "Error al enviar mensaje: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Opcional: Actualizar el "lastMessage" y "lastMessageTimestamp" en el documento del chatTopic
        db.collection("chatTopics").document(chatTopicId)
                .update("lastMessage", messageText,
                        "lastMessageTimestamp", timestamp)
                .addOnFailureListener(e -> System.err.println("Error updating chat topic last message: " + e.getMessage()));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (messagesListener != null) {
            messagesListener.remove(); // Detener la escucha al salir de la actividad
        }
    }

    private class MessageAdapter extends RecyclerView.Adapter {
        public MessageAdapter(IndividualChatActivity individualChatActivity, List<Message> messageList) {

        }

        public void setMessages(List<Message> newMessages) {

        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }

    private class Message {
        public Message(String senderId, String currentUserName, String messageText, long timestamp) {

        }
    }
}