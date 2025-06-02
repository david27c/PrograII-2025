package com.example.miprimeraaplicacion;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.ObjectInputStream;
import java.util.List;
import com.squareup.picasso.Picasso;

public class ChatTopicAdapter extends RecyclerView.Adapter<ChatTopicAdapter.ChatTopicViewHolder> {

    private Context context;
    private List<ChatTopic> chatTopicList;
    private OnChatTopicClickListener listener;
    private ObjectInputStream.GetField Picasso;

    public interface OnChatTopicClickListener {
        void onChatTopicClick(ChatTopic topic);
    }

    public ChatTopicAdapter(Context context, List<ChatTopic> chatTopicList, OnChatTopicClickListener listener) {
        this.context = context;
        this.chatTopicList = chatTopicList;
        this.listener = listener;
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

        holder.topicTitleTextView.setText(topic.getTitle());
        holder.lastMessageTextView.setText(topic.getLastMessage());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            if (topic.getIconUrl() != null && !topic.getIconUrl().isEmpty())
                Picasso.get().load(topic.getIconUrl())
                        .placeholder(R.drawable.ic_default_profile)
                        .error(R.drawable.ic_chat)
                        .into(holder.topicIcon);
            else {
                holder.topicIcon.setImageResource(R.drawable.ic_chat);
            }
        }

        // Ajuste para usar el campo 'unreadMessagesCount' de tu modelo ChatTopic
        if (topic.getUnreadMessagesCount() > 0) {
            holder.unreadMessagesCountTextView.setVisibility(View.VISIBLE);
            holder.unreadMessagesCountTextView.setText(String.valueOf(topic.getUnreadMessagesCount()));
        } else {
            holder.unreadMessagesCountTextView.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onChatTopicClick(topic));
    }

    @Override
    public int getItemCount() {
        return chatTopicList.size();
    }

    public static class ChatTopicViewHolder extends RecyclerView.ViewHolder {
        ImageView topicIcon;
        TextView topicTitleTextView;
        TextView lastMessageTextView;
        TextView unreadMessagesCountTextView;

        public ChatTopicViewHolder(@NonNull View itemView) {
            super(itemView);
            topicIcon = itemView.findViewById(R.id.topic_icon);
            topicTitleTextView = itemView.findViewById(R.id.topic_title_tv);
            lastMessageTextView = itemView.findViewById(R.id.last_message_tv);
            unreadMessagesCountTextView = itemView.findViewById(R.id.unread_messages_count_tv);
        }
    }

    private class ChatTopic {
        public int getTitle() {
            return 0;
        }

        public int getUnreadMessagesCount() {
            return 0;
        }

        public int getLastMessage() {
            return 0;
        }

        public CharSequence getIconUrl() {
            return null;
        }
    }
}