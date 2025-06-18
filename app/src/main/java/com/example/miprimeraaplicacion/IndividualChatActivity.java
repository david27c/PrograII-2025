package com.example.miprimeraaplicacion;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences; // Necesario para SharedPreferences
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID; // Para generar IDs únicos

public class IndividualChatActivity extends AppCompatActivity {
    private static final String TAG_INDIVIDUAL_CHAT = "IndividualChatActivityDebug";

    private RecyclerView recyclerViewMessages;
    private EditText editTextMessage;
    private ImageButton buttonSendMessage;
    private ProgressBar progressBarChat;
    private TextView textViewNoMessages;

    private String chatTopicId;
    private String chatTopicName;
    private DBLocal dbLocal; // Usaremos DBLocal
    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    private String currentUserId; // El ID del usuario logueado
    private String currentUserName = "Usuario Anónimo"; // Valor por defecto

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG_INDIVIDUAL_CHAT, "onCreate() de IndividualChatActivity: Inicio."); // LOG

        setContentView(R.layout.activity_individual_chat);
        Log.d(TAG_INDIVIDUAL_CHAT, "onCreate(): Layout establecido."); // LOG

        // Inicializar DBLocal al principio del onCreate
        dbLocal = new DBLocal(this);
        Log.d(TAG_INDIVIDUAL_CHAT, "onCreate(): DBLocal inicializado."); // LOG

        // --- LÓGICA DE VERIFICACIÓN DE SESIÓN ---
        // Obtener el ID del usuario logueado desde DBLocal (CORREGIDO)
        // Esto REEMPLAZA la línea de SharedPreferences que tenías aquí
        currentUserId = dbLocal.getLoggedInUserId(this);
        Log.d(TAG_INDIVIDUAL_CHAT, "onCreate(): ID de usuario recuperado de DBLocal: " + currentUserId); // LOG

        if (currentUserId == null || currentUserId.isEmpty()) { // Añadir .isEmpty() para robustez
            Log.d(TAG_INDIVIDUAL_CHAT, "onCreate(): No hay usuario logueado. Redirigiendo a LoginActivity."); // LOG
            Toast.makeText(this, "Debes iniciar sesión para acceder al chat.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(IndividualChatActivity.this, LoginActivity.class);
            // Limpia la pila de actividades para que el usuario no pueda volver con el botón de atrás.
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Finaliza esta actividad
            return; // Terminar onCreate aquí
        }
        // --- FIN DE LÓGICA DE VERIFICACIÓN DE SESIÓN ---

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        Log.d(TAG_INDIVIDUAL_CHAT, "onCreate(): Toolbar inicializada."); // LOG

        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSendMessage = findViewById(R.id.buttonSendMessage);
        progressBarChat = findViewById(R.id.progressBarChat);
        textViewNoMessages = findViewById(R.id.textViewNoMessages);
        Log.d(TAG_INDIVIDUAL_CHAT, "onCreate(): Vistas principales inicializadas (findViewById)."); // LOG


        // Obtener ID y nombre del tema de chat del Intent
        if (getIntent().hasExtra("chatTopicId") && getIntent().hasExtra("chatTopicName")) {
            chatTopicId = getIntent().getStringExtra("chatTopicId");
            chatTopicName = getIntent().getStringExtra("chatTopicName");
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Chat con " + chatTopicName);
            }
            Log.d(TAG_INDIVIDUAL_CHAT, "onCreate(): Tema de chat recibido: ID=" + chatTopicId + ", Nombre=" + chatTopicName); // LOG
        } else {
            Toast.makeText(this, "Tema de chat no especificado.", Toast.LENGTH_SHORT).show();
            finish();
            Log.e(TAG_INDIVIDUAL_CHAT, "onCreate(): Tema de chat no especificado en el Intent."); // LOG
            return;
        }

        // Configurar RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewMessages.setLayoutManager(layoutManager);
        messageList = new ArrayList<>();
        // El adaptador ahora se inicializa con el currentUserId directamente
        // Asegúrate de que tu MessageAdapter tiene un constructor que acepte Context, List<Message> y String (userId)
        messageAdapter = new MessageAdapter(this, messageList, currentUserId); // Pasar currentUserId al adaptador
        recyclerViewMessages.setAdapter(messageAdapter);
        Log.d(TAG_INDIVIDUAL_CHAT, "onCreate(): RecyclerView y Adapter configurados."); // LOG


        // Obtener el nombre de usuario actual (para mostrar en los mensajes)
        loadCurrentUserName(currentUserId);
        Log.d(TAG_INDIVIDUAL_CHAT, "onCreate(): Nombre de usuario actual cargado: " + currentUserName); // LOG


        // Listener para el botón de enviar mensaje
        buttonSendMessage.setOnClickListener(v -> {
            sendMessage();
            Log.d(TAG_INDIVIDUAL_CHAT, "onClick: Botón Enviar Mensaje presionado."); // LOG
        });

        // Cargar mensajes inicialmente
        loadMessages();
        Log.d(TAG_INDIVIDUAL_CHAT, "onCreate(): Método loadMessages() llamado."); // LOG

        Log.d(TAG_INDIVIDUAL_CHAT, "onCreate() de IndividualChatActivity: Fin."); // LOG
    }

    // Método para manejar el botón de regreso de la Toolbar
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadCurrentUserName(String userId) {
        User user = dbLocal.getUserById(userId);
        if (user != null && user.getUsername() != null && !user.getUsername().isEmpty()) {
            currentUserName = user.getUsername();
        } else {
            currentUserName = "Usuario " + userId.substring(0, 4); // Nombre corto si no hay username
        }
    }

    private void loadMessages() {
        progressBarChat.setVisibility(View.VISIBLE);
        textViewNoMessages.setVisibility(View.GONE);
        recyclerViewMessages.setVisibility(View.GONE);

        List<Message> loadedMessages = dbLocal.getChatMessagesForTopic(chatTopicId);

        messageList.clear();
        messageList.addAll(loadedMessages);
        messageAdapter.notifyDataSetChanged();

        progressBarChat.setVisibility(View.GONE);

        if (messageList.isEmpty()) {
            textViewNoMessages.setVisibility(View.VISIBLE);
            textViewNoMessages.setText("No hay mensajes en este chat.");
        } else {
            recyclerViewMessages.setVisibility(View.VISIBLE);
            recyclerViewMessages.scrollToPosition(messageList.size() - 1); // Desplazarse al último mensaje
        }
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();

        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "No puedes enviar un mensaje vacío.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUserId == null) {
            Toast.makeText(this, "Debes iniciar sesión para enviar mensajes.", Toast.LENGTH_SHORT).show();
            return;
        }

        long timestamp = System.currentTimeMillis();

        // Crear el objeto Message
        Message message = new Message(UUID.randomUUID().toString(), chatTopicId, currentUserId, currentUserName, messageText, timestamp);

        // Guardar el mensaje en DBLocal
        if (dbLocal.addChatMessage(message)) {
            editTextMessage.setText(""); // Limpiar el campo de texto
            loadMessages(); // Recargar los mensajes para mostrar el nuevo

            // Opcional: Actualizar el "lastMessage" y "lastMessageTimestamp" en el ChatTopic
            ChatTopic currentTopic = dbLocal.getChatTopicById(chatTopicId);
            if (currentTopic != null) {
                currentTopic.setLastMessage(messageText);
                currentTopic.setLastMessageTimestamp(timestamp);
                dbLocal.updateChatTopic(currentTopic);
            }

        } else {
            Toast.makeText(IndividualChatActivity.this, "Error al enviar mensaje.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar mensajes cada vez que la actividad se vuelve visible
        loadMessages();
    }

    // REMOVIDO: onStop ya no es necesario para listeners de Firebase

    // --- Adaptador para los mensajes del chat ---
    public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

        private Context context;
        private List<Message> messageList;
        private String currentLoggedInUserId; // ID del usuario logueado para determinar si el mensaje es propio

        private static final int VIEW_TYPE_MESSAGE_SENT = 1;
        private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

        public MessageAdapter(Context context, List<Message> messageList, String currentUserId) {
            this.context = context;
            this.messageList = messageList;
            // Obtener el ID del usuario logueado para diferenciar mensajes propios
            SharedPreferences sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            this.currentLoggedInUserId = sharedPref.getString("current_user_id", null);
        }

        public void setMessages(List<Message> newMessages) {
            this.messageList.clear();
            this.messageList.addAll(newMessages);
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            Message message = messageList.get(position);
            // Si el ID del remitente del mensaje es igual al ID del usuario logueado
            if (message.getSenderId() != null && message.getSenderId().equals(currentLoggedInUserId)) {
                return VIEW_TYPE_MESSAGE_SENT;
            } else {
                return VIEW_TYPE_MESSAGE_RECEIVED;
            }
        }

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (viewType == VIEW_TYPE_MESSAGE_SENT) {
                view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            } else {
                view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            }
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            Message message = messageList.get(position);
            holder.textViewMessage.setText(message.getText());
            // Formatear la marca de tiempo (opcional, podrías necesitar una utilidad de fecha)
            // holder.textViewTimestamp.setText(formatTimestamp(message.getTimestamp()));

            // Solo mostrar el nombre del remitente si no es el usuario actual
            if (getItemViewType(position) == VIEW_TYPE_MESSAGE_RECEIVED) {
                holder.textViewSenderName.setText(message.getSenderName());
                holder.textViewSenderName.setVisibility(View.VISIBLE);
            } else {
                holder.textViewSenderName.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return messageList.size();
        }

        public class MessageViewHolder extends RecyclerView.ViewHolder {
            TextView textViewMessage;
            TextView textViewSenderName; // Para el nombre del remitente en mensajes recibidos
            // TextView textViewTimestamp; // Si decides mostrar la marca de tiempo

            public MessageViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewMessage = itemView.findViewById(R.id.textViewMessage);
                textViewSenderName = itemView.findViewById(R.id.textViewSenderName);
                // textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
            }
        }

        // Método de utilidad para formatear la marca de tiempo (puedes moverlo a una clase de utilidades)
        private String formatTimestamp(long timestamp) {
            // Ejemplo básico, puedes usar SimpleDateFormat para un formato más bonito
            return new Date(timestamp).toString();
        }
    }
}