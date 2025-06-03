package com.example.miprimeraaplicacion;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatTopicAdapter extends RecyclerView.Adapter<ChatTopicAdapter.ChatTopicViewHolder> {

    private Context context;
    private List<ChatTopic> chatTopicList;

    public ChatTopicAdapter(Context context, List<ChatTopic> chatTopicList) {
        this.context = context;
        this.chatTopicList = chatTopicList;
    }

    @NonNull
    @Override
    public ChatTopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_topic, parent, false);
        return new ChatTopicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatTopicViewHolder holder, int position) {
        ChatTopic topic = chatTopicList.get(position);

        holder.textViewTopicTitle.setText(topic.getName());
        holder.textViewLastMessage.setText(topic.getLastMessage());

        if (topic.getUnreadCount() > 0) {
            holder.textViewUnreadCount.setVisibility(View.VISIBLE);
            holder.textViewUnreadCount.setText(String.valueOf(topic.getUnreadCount()));
        } else {
            holder.textViewUnreadCount.setVisibility(View.GONE);
        }

        // Aquí podrías configurar el ícono del tema si tienes íconos específicos
        // holder.imageViewTopicIcon.setImageResource(R.drawable.ic_baches);

        holder.itemView.setOnClickListener(v -> {
            // Abrir la actividad de chat individual
            Intent intent = new Intent(context, IndividualChatActivity.class);
            intent.putExtra("chatTopicId", topic.getId());
            intent.putExtra("chatTopicName", topic.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chatTopicList.size();
    }

    public static class ChatTopicViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewTopicIcon;
        TextView textViewTopicTitle, textViewLastMessage, textViewUnreadCount;

        public ChatTopicViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewTopicIcon = itemView.findViewById(R.id.imageViewTopicIcon);
            textViewTopicTitle = itemView.findViewById(R.id.textViewTopicTitle);
            textViewLastMessage = itemView.findViewById(R.id.textViewLastMessage);
            textViewUnreadCount = itemView.findViewById(R.id.textViewUnreadCount);
        }
    }

    public void updateChatTopics(List<ChatTopic> newTopics) {
        chatTopicList.clear();
        chatTopicList.addAll(newTopics);
        notifyDataSetChanged();
    }

    private class ChatTopic {
        public CharSequence getName() {
            return "Chat Topic Name";
        }

        public int getLastMessage() {
            return 0;
        }

        public int getUnreadCount() {
            return 0;
        };

        public boolean getId() {
            return false;
        }
    }
}