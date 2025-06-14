package com.example.miprimeraaplicacion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
// import android.view.ViewGroup; // Ya no es necesario si ChatTopicAdapter es externo
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
import com.google.firebase.firestore.Query; // Asegúrate de que esta importación esté presente
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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chat Comunitario");
        }

        recyclerViewChatTopics = findViewById(R.id.recyclerViewChatTopics);
        progressBarChat = findViewById(R.id.progressBarChat);
        textViewNoChats = findViewById(R.id.textViewNoChats);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        recyclerViewChatTopics.setLayoutManager(new LinearLayoutManager(this));
        chatTopicList = new ArrayList<>();
        // Asegúrate de que ChatTopicAdapter reciba el Contexto correcto
        // Ahora se usa el ChatTopicAdapter externo
        chatTopicAdapter = new ChatTopicAdapter(this, chatTopicList);
        recyclerViewChatTopics.setAdapter(chatTopicAdapter);

        // Configurar la navegación inferior (SOLO LOS 5 ITEMS PRINCIPALES)
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
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
            }
            // Los ítems nav_notifications y nav_settings han sido movidos a la Toolbar
            return false;
        });
        // Asegurarse de que el ítem "Chat" esté seleccionado al inicio
        bottomNavigationView.setSelectedItemId(R.id.nav_chat);

        loadChatTopics();
    }

    // *** NUEVOS MÉTODOS PARA EL MENÚ DE LA TOOLBAR (igual que en HomeActivity) ***
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Infla el menú de la Toolbar
        getMenuInflater().inflate(R.menu.toolbar_menu, menu); // Asegúrate de tener res/menu/toolbar_menu.xml
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_notifications) { // Usamos action_notifications de toolbar_menu.xml
            startActivity(new Intent(CommunityChatActivity.this, NotificationsActivity.class));
            return true;
        } else if (id == R.id.action_settings) { // Usamos action_settings de toolbar_menu.xml
            startActivity(new Intent(CommunityChatActivity.this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // *** FIN DE NUEVOS MÉTODOS ***

    private void loadChatTopics() {
        progressBarChat.setVisibility(View.VISIBLE);
        textViewNoChats.setVisibility(View.GONE);
        recyclerViewChatTopics.setVisibility(View.GONE);

        // Si estás desactivando Firebase por el momento, esta sección causaría un problema
        // ya que intenta usar FirebaseFirestore. Si realmente quieres desactivar Firebase,
        // deberías comentar o reemplazar esta lógica con una carga de datos local (DBLocal).
        // Por ahora, asumo que Firestore está configurado o lo será.
        db.collection("chatTopics")
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING) // Usar Query.Direction
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
                                try {
                                    // Firebase deserializará directamente si ChatTopic es un POJO válido
                                    ChatTopic topic = document.toObject(ChatTopic.class);
                                    // Si el ID no se mapea automáticamente en el constructor, asignarlo manualmente
                                    if (topic.getId() == null || topic.getId().isEmpty()) {
                                        topic.setId(document.getId());
                                    }
                                    chatTopicList.add(topic);
                                } catch (Exception e) {
                                    Toast.makeText(CommunityChatActivity.this,
                                            "Error al procesar tema de chat: " + e.getMessage() + " (ID: " + document.getId() + ")",
                                            Toast.LENGTH_LONG).show();
                                    e.printStackTrace(); // Imprime la traza completa para depuración
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
                        task.getException().printStackTrace(); // Imprime la traza completa para depuración
                    }
                });
    }

    // Método para añadir temas de chat por defecto si la colección está vacía
    private void addDefaultChatTopics() {
        List<ChatTopic> defaultTopics = new ArrayList<>();
        // Aquí se usa el constructor de ChatTopic con 'lastMessage' como tercer parámetro
        defaultTopics.add(new ChatTopic(db.collection("chatTopics").document().getId(), "Baches y Calzadas", "Reporta baches en tu zona.", System.currentTimeMillis(), 0));
        defaultTopics.add(new ChatTopic(db.collection("chatTopics").document().getId(), "Gestión de Basura", "Comenta sobre la recolección de basura.", System.currentTimeMillis(), 0));
        defaultTopics.add(new ChatTopic(db.collection("chatTopics").document().getId(), "Servicios de Agua", "Problemas con el suministro de agua.", System.currentTimeMillis(), 0));
        defaultTopics.add(new ChatTopic(db.collection("chatTopics").document().getId(), "Seguridad Ciudadana", "Discute temas de seguridad y delincuencia.", System.currentTimeMillis(), 0));

        // Guarda estos temas en Firestore para que estén disponibles
        for (ChatTopic topic : defaultTopics) {
            db.collection("chatTopics").document(topic.getId()).set(topic)
                    .addOnSuccessListener(aVoid -> Log.d("CommunityChatActivity", "Added default chat topic: " + topic.getName()))
                    .addOnFailureListener(e -> Log.e("CommunityChatActivity", "Error adding default topic: " + e.getMessage(), e));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChatTopics(); // Recargar los temas de chat cada vez que la actividad se hace visible
    }

    // ==========================================================================================
    // AVISO IMPORTANTE: LAS CLASES 'ChatTopicAdapter' y 'ChatTopic' QUE ESTABAN AQUÍ HAN SIDO
    // ELIMINADAS. DEBEN EXISTIR COMO ARCHIVOS SEPARADOS EN EL MISMO PAQUETE.
    // LA PRESENCIA DE DEFINICIONES ANIDADAS E INCOMPLETAS AQUÍ CAUSA ERRORES.
    // ==========================================================================================
}