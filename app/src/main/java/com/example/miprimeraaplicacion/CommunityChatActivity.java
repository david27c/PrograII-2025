package com.example.miprimeraaplicacion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID; // Para generar IDs únicos

public class CommunityChatActivity extends AppCompatActivity {

    private com.example.miprimeraaplicacion.DBLocal dbLocal; // Usar DBLocal

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

        dbLocal = new com.example.miprimeraaplicacion.DBLocal(this); // Inicializar DBLocal

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chat Comunitario");
        }

        recyclerViewChatTopics = findViewById(R.id.recyclerViewChatTopics);
        progressBarChat = findViewById(R.id.progressBarChat);
        textViewNoChats = findViewById(R.id.textViewNoChats);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        recyclerViewChatTopics.setLayoutManager(new LinearLayoutManager(this));
        chatTopicList = new ArrayList<>();
        chatTopicAdapter = new ChatTopicAdapter(this, chatTopicList);
        recyclerViewChatTopics.setAdapter(chatTopicAdapter);

        bottomNavigationView.setOnItemSelectedListener(item -> {
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
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(CommunityChatActivity.this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.nav_chat);

        loadChatTopics();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_notifications) {
            startActivity(new Intent(CommunityChatActivity.this, NotificationsActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(CommunityChatActivity.this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadChatTopics() {
        progressBarChat.setVisibility(View.VISIBLE);
        textViewNoChats.setVisibility(View.GONE);
        recyclerViewChatTopics.setVisibility(View.GONE);

        List<ChatTopic> topicsFromDb = dbLocal.getAllChatTopics();

        if (topicsFromDb.isEmpty()) {
            addDefaultChatTopics(); // Agrega temas predeterminados si no hay ninguno
            topicsFromDb = dbLocal.getAllChatTopics(); // Vuelve a cargar después de añadir
        }

        chatTopicList.clear();
        chatTopicList.addAll(topicsFromDb);
        chatTopicAdapter.updateChatTopics(chatTopicList);

        progressBarChat.setVisibility(View.GONE);
        if (chatTopicList.isEmpty()) {
            textViewNoChats.setVisibility(View.VISIBLE);
            recyclerViewChatTopics.setVisibility(View.GONE);
        } else {
            textViewNoChats.setVisibility(View.GONE);
            recyclerViewChatTopics.setVisibility(View.VISIBLE);
        }
    }

    private void addDefaultChatTopics() {
        List<ChatTopic> defaultTopics = new ArrayList<>();
        defaultTopics.add(new ChatTopic(UUID.randomUUID().toString(), "Baches y Calzadas", "Reporta baches en tu zona.", System.currentTimeMillis(), 0));
        defaultTopics.add(new ChatTopic(UUID.randomUUID().toString(), "Gestión de Basura", "Comenta sobre la recolección de basura.", System.currentTimeMillis(), 0));
        defaultTopics.add(new ChatTopic(UUID.randomUUID().toString(), "Servicios de Agua", "Problemas con el suministro de agua.", System.currentTimeMillis(), 0));
        defaultTopics.add(new ChatTopic(UUID.randomUUID().toString(), "Seguridad Ciudadana", "Discute temas de seguridad y delincuencia.", System.currentTimeMillis(), 0));

        for (ChatTopic topic : defaultTopics) {
            if (dbLocal.addChatTopic(topic)) {
                Log.d("CommunityChatActivity", "Tema de chat predeterminado agregado a DBLocal: " + topic.getName());
            } else {
                Log.e("CommunityChatActivity", "Error al agregar tema de chat predeterminado a DBLocal: " + topic.getName());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChatTopics();
    }
}