package com.example.miprimeraaplicacion;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerViewChatTopics = findViewById(R.id.recyclerViewChatTopics);
        progressBarChat = findViewById(R.id.progressBarChat);
        textViewNoChats = findViewById(R.id.textViewNoChats);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        recyclerViewChatTopics.setLayoutManager(new LinearLayoutManager(this));
        chatTopicList = new ArrayList<>();
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

        // En una aplicación real, tendrías una colección de "chatTopics" en Firestore
        // Por ahora, cargaremos algunos temas de ejemplo.
        // La implementación real podría involucrar escuchar cambios en tiempo real.
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
                                ChatTopic topic = document.toObject(ChatTopic.class);
                                topic.setId(document.getId());
                                chatTopicList.add(topic);
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

    private class ChatTopicAdapter extends RecyclerView.Adapter {
        public ChatTopicAdapter(CommunityChatActivity communityChatActivity, List<ChatTopic> chatTopicList) {

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

        public void updateChatTopics(List<ChatTopic> chatTopicList) {

        }
    }

    private class ChatTopic {
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