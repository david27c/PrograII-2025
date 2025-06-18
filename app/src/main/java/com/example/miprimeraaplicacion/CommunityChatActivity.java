package com.example.miprimeraaplicacion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

    private static final String TAG_CHAT = "CommunityChatActivityDebug";
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
        Log.d(TAG_CHAT, "onCreate() de CommunityChatActivity: Inicio."); // LOG

        setContentView(R.layout.activity_community_chat);
        Log.d(TAG_CHAT, "onCreate(): Layout establecido."); // LOG

        dbLocal = new com.example.miprimeraaplicacion.DBLocal(this); // Inicializar DBLocal
        Log.d(TAG_CHAT, "onCreate(): DBLocal inicializado."); // LOG

        // --- LÓGICA DE VERIFICACIÓN DE SESIÓN ---
        String currentUserId = dbLocal.getLoggedInUserId(this);
        Log.d(TAG_CHAT, "onCreate(): ID de usuario recuperado de DBLocal: " + currentUserId); // LOG

        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.d(TAG_CHAT, "onCreate(): No hay usuario logueado. Redirigiendo a LoginActivity."); // LOG
            Toast.makeText(this, "Debes iniciar sesión para acceder al chat comunitario.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(CommunityChatActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return; // Terminar onCreate aquí para evitar que se ejecute el resto del código
        }
        // --- FIN DE LÓGICA DE VERIFICACIÓN DE SESIÓN ---

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chat Comunitario");
        }
        Log.d(TAG_CHAT, "onCreate(): Toolbar inicializada."); // LOG

        recyclerViewChatTopics = findViewById(R.id.recyclerViewChatTopics);
        progressBarChat = findViewById(R.id.progressBarChat);
        textViewNoChats = findViewById(R.id.textViewNoChats);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        Log.d(TAG_CHAT, "onCreate(): Vistas principales inicializadas (findViewById)."); // LOG

        recyclerViewChatTopics.setLayoutManager(new LinearLayoutManager(this));
        chatTopicList = new ArrayList<>();
        chatTopicAdapter = new ChatTopicAdapter(this, chatTopicList);
        recyclerViewChatTopics.setAdapter(chatTopicAdapter);
        Log.d(TAG_CHAT, "onCreate(): RecyclerView y Adapter configurados."); // LOG

        bottomNavigationView.setOnItemSelectedListener(item -> { // Usa setOnItemSelectedListener para versiones recientes
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(CommunityChatActivity.this, HomeActivity.class));
                finish();
                Log.d(TAG_CHAT, "BottomNav: Redirigiendo a HomeActivity."); // LOG
                return true;
            } else if (itemId == R.id.nav_report) { // Asumiendo nav_report para ReportProblemActivity
                startActivity(new Intent(CommunityChatActivity.this, ReportProblemActivity.class));
                finish();
                Log.d(TAG_CHAT, "BottomNav: Redirigiendo a ReportProblemActivity."); // LOG
                return true;
            } else if (itemId == R.id.nav_my_reports) { // Asumiendo este ID
                startActivity(new Intent(CommunityChatActivity.this, MyReportsActivity.class));
                finish();
                Log.d(TAG_CHAT, "BottomNav: Redirigiendo a MyReportsActivity."); // LOG
                return true;
            } else if (itemId == R.id.nav_chat) {
                Log.d(TAG_CHAT, "BottomNav: Ya en CommunityChatActivity."); // LOG
                return true; // Ya estamos en esta actividad
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(CommunityChatActivity.this, ProfileActivity.class));
                finish();
                Log.d(TAG_CHAT, "BottomNav: Redirigiendo a ProfileActivity."); // LOG
                return true;
            }
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.nav_chat); // Asegura que "Chat" esté seleccionado al inicio
        Log.d(TAG_CHAT, "onCreate(): BottomNavigationView listener y selección inicial configurados."); // LOG

        loadChatTopics(); // Cargar los temas de chat
        Log.d(TAG_CHAT, "onCreate(): Método loadChatTopics() llamado."); // LOG

        Log.d(TAG_CHAT, "onCreate() de CommunityChatActivity: Fin."); // LOG
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