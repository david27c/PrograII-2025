package com.example.miprimeraaplicacion;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.miprimeraaplicacion.R;
import com.example.miprimeraaplicacion.activities.IndividualChatActivity;
import com.example.miprimeraaplicacion.adapters.ChatTopicAdapter;
import com.example.miprimeraaplicacion.models.ChatTopic;

import java.util.ArrayList;
import java.util.List;

public class CommunityChatFragment extends Fragment implements ChatTopicAdapter.OnChatTopicClickListener {

    private RecyclerView communityChatRecyclerView;
    private ChatTopicAdapter chatTopicAdapter;
    private List<ChatTopic> chatTopicList;
    private ProgressBar progressBar;
    private Button authoritiesListBtn;

    private FirebaseFirestore db;

    public CommunityChatFragment() {
        // Constructor público vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community_chat, container, false);

        db = FirebaseFirestore.getInstance();

        initViews(view);
        setupRecyclerView();
        loadChatTopics();

        authoritiesListBtn.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Listado de autoridades (por implementar)", Toast.LENGTH_SHORT).show();
            // Aquí podrías abrir una nueva Activity o un diálogo con la lista de autoridades
        });

        return view;
    }

    private void initViews(View view) {
        communityChatRecyclerView = view.findViewById(R.id.community_chat_recycler_view);
        progressBar = view.findViewById(R.id.chat_progress_bar);
        authoritiesListBtn = view.findViewById(R.id.authorities_list_btn);
    }

    private void setupRecyclerView() {
        chatTopicList = new ArrayList<>();
        chatTopicAdapter = new ChatTopicAdapter(getContext(), chatTopicList, this);
        communityChatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        communityChatRecyclerView.setAdapter(chatTopicAdapter);
    }

    private void loadChatTopics() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("chat_topics") // Suponiendo que tienes una colección para temas de chat
                .orderBy("lastMessageTimestamp", com.google.firebase.firestore.Query.Direction.DESCENDING) // Ordenar por actividad reciente
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(getContext(), "Error al cargar temas de chat: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        return;
                    }

                    if (snapshots != null) {
                        chatTopicList.clear();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            ChatTopic topic = doc.toObject(ChatTopic.class);
                            if (topic != null) {
                                topic.setId(doc.getId()); // Asigna el ID del documento
                                chatTopicList.add(topic);
                            }
                        }
                        chatTopicAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onChatTopicClick(ChatTopic topic) {
        // Abrir la actividad de chat individual para el tema seleccionado
        Intent intent = new Intent(getContext(), IndividualChatActivity.class);
        intent.putExtra("topicId", topic.getId());
        intent.putExtra("topicName", topic.getTitle()); // Pasa el título del tema para mostrar en la barra
        startActivity(intent);
    }
}