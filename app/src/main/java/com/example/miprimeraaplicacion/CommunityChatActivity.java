package com.example.miprimeraaplicacion;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup; // Necesario para onCreateViewHolder
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CommunityChatActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private RecyclerView recyclerViewChatTopics;
    private ChatTopicAdapter chatTopicAdapter;
    private List<ChatTopic> chatTopicList;

    private ProgressBar progressBarChat;
    private TextView textViewNoChats;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_chat);

        // Se inicia la base de datos de Firestore
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerViewChatTopics = findViewById(R.id.recyclerViewChatTopics);
        progressBarChat = findViewById(R.id.progressBarChat);
        textViewNoChats = findViewById(R.id.textViewNoChats);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        recyclerViewChatTopics.setLayoutManager(new LinearLayoutManager(this));
        chatTopicList = new ArrayList<>();
        // Asegúrate de que ChatTopicAdapter reciba el Contexto correcto
        chatTopicAdapter = new ChatTopicAdapter(this, chatTopicList);
        recyclerViewChatTopics.setAdapter(chatTopicAdapter);

        // Configurar la navegación inferior
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(CommunityChatActivity.this, HomeActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_report) {
                    startActivity(new Intent(CommunityChatActivity.this, ReportProblemActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_my_reports) {
                    startActivity(new Intent(CommunityChatActivity.this, MyReportsActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_chat) {
                    // Ya estamos en CommunityChatActivity
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(CommunityChatActivity.this, ProfileActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_notifications) {
                    startActivity(new Intent(CommunityChatActivity.this, NotificationsActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_settings) {
                    startActivity(new Intent(CommunityChatActivity.this, SettingsActivity.class));
                    finish();
                    return true;
                }
                return false;
            }
        });
        // Asegurarse de que el ítem "Chat" esté seleccionado al inicio
        bottomNavigationView.setSelectedItemId(R.id.nav_chat);

        loadChatTopics();
    }

    private void loadChatTopics() {
        progressBarChat.setVisibility(View.VISIBLE);
        textViewNoChats.setVisibility(View.GONE);
        recyclerViewChatTopics.setVisibility(View.GONE);

        db.collection("chatTopics")
                .orderBy("lastMessageTimestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    progressBarChat.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        chatTopicList.clear();
                        if (task.getResult().isEmpty()) {
                            // Si no hay temas de chat en Firestore, añadir algunos por defecto
                            addDefaultChatTopics();
                        } else {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // AÑADIDO: Bloque try-catch para la conversión de objeto
                                try {
                                    ChatTopic topic = document.toObject(ChatTopic.class);
                                    topic.setId(document.getId());
                                    chatTopicList.add(topic);
                                } catch (Exception e) {
                                    // Manejar el error de conversión (por ejemplo, loguear o mostrar un toast)
                                    Toast.makeText(CommunityChatActivity.this,
                                            "Error al procesar tema de chat: " + e.getMessage() + " (ID: " + document.getId() + ")",
                                            Toast.LENGTH_LONG).show();
                                    // Opcional: System.err.println("Error converting document to ChatTopic: " + e.getMessage());
                                }
                            }
                        }
                        chatTopicAdapter.updateChatTopics(chatTopicList);

                        if (chatTopicList.isEmpty()) {
                            textViewNoChats.setVisibility(View.VISIBLE);
                            recyclerViewChatTopics.setVisibility(View.GONE);
                        } else {
                            textViewNoChats.setVisibility(View.GONE);
                            recyclerViewChatTopics.setVisibility(View.VISIBLE);
                        }
                    } else {
                        // El error de la tarea completa (por ejemplo, problemas de red, permisos)
                        Toast.makeText(CommunityChatActivity.this, "Error al cargar temas de chat: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        textViewNoChats.setText("Error al cargar temas de chat.");
                        textViewNoChats.setVisibility(View.VISIBLE);
                        recyclerViewChatTopics.setVisibility(View.GONE);
                    }
                });
    }

    // Método para añadir temas de chat por defecto si la colección está vacía
    private void addDefaultChatTopics() {
        List<ChatTopic> defaultTopics = new ArrayList<>();
        defaultTopics.add(new ChatTopic("baches_id", "Baches y Calzadas", "Reporta baches en tu zona.", System.currentTimeMillis(), 0));
        defaultTopics.add(new ChatTopic("basura_id", "Gestión de Basura", "Comenta sobre la recolección de basura.", System.currentTimeMillis(), 0));
        defaultTopics.add(new ChatTopic("agua_id", "Servicios de Agua", "Problemas con el suministro de agua.", System.currentTimeMillis(), 0));
        defaultTopics.add(new ChatTopic("seguridad_id", "Seguridad Ciudadana", "Discute temas de seguridad y delincuencia.", System.currentTimeMillis(), 0));

        // Guarda estos temas en Firestore para que estén disponibles
        // Las operaciones addOnSuccessListener y addOnFailureListener son el try-catch de las operaciones asíncronas de Firebase
        for (ChatTopic topic : defaultTopics) {
            db.collection("chatTopics").document(topic.getId()).set(topic)
                    .addOnSuccessListener(aVoid -> System.out.println("Added default chat topic: " + topic.getName()))
                    .addOnFailureListener(e -> System.err.println("Error adding default topic: " + e.getMessage()));
        }
        chatTopicList.addAll(defaultTopics); // Agregarlos a la lista actual también
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChatTopics(); // Recargar los temas de chat cada vez que la actividad se hace visible
    }

    // ///////////////////////////////////////////////////////////////////////////////
    // AVISO IMPORTANTE: ESTAS CLASES DEBEN ESTAR EN SUS PROPIOS ARCHIVOS SEPARADOS.
    // ESTAR AQUÍ DENTRO CAUSARÁ ERRORES DE COMPILACIÓN O COMPORTAMIENTOS INESPERADOS.
    // LAS MANTENGO AQUÍ SOLO POR LA RESTRICCIÓN DE "NO TOCAR LO DEMÁS".
    // ///////////////////////////////////////////////////////////////////////////////

    private class ChatTopicAdapter extends RecyclerView.Adapter {
        public ChatTopicAdapter(CommunityChatActivity communityChatActivity, List<ChatTopic> chatTopicList) {
            // Este constructor debe ser más completo si lo usas fuera de aquí
            // Por ejemplo:
            // this.context = communityChatActivity;
            // this.chatTopicList = chatTopicList;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Esto es incorrecto, debería inflar una vista y devolver un ViewHolder válido
            return null; // Aquí debería ir la lógica real de inflado del ViewHolder
        }
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            // Esto es incorrecto, aquí iría la lógica para vincular los datos
        }

        @Override
        public int getItemCount() {
            // Esto es incorrecto, debería devolver chatTopicList.size()
            return 0;
        }

        public void updateChatTopics(List<ChatTopic> chatTopicList) {
            // Este método debe actualizar la lista interna y notificar cambios
        }
    }

    private class ChatTopic {
        // Este constructor está incompleto. Debería ser el constructor completo como el que te di antes.
        public ChatTopic(String bachesId, String bachesYCalzadas, String s, long l, int i) {

        }

        public void setId(String id) {

        }

        public String getId() {
            return null;
        }

        public String getName() {
            return null;
        }
    }
}