package com.example.miprimeraaplicacion;

import android.content.Context;
import android.content.SharedPreferences; // Importación necesaria
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
// *** Se elimina importación de Firebase Auth ya no necesaria ***
// import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private Context context;
    private List<Message> messageList;
    private String currentUserId; // Este ID debe venir de SharedPreferences ahora

    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
        // *** CAMBIO CRÍTICO: Obtener currentUserId de SharedPreferences ***
        SharedPreferences sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        this.currentUserId = sharedPref.getString("current_user_id", null);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        if (currentUserId != null && message.getSenderId() != null && message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT; // Mensaje enviado por el usuario actual
        } else {
            return VIEW_TYPE_RECEIVED; // Mensaje recibido de otro usuario
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm a", Locale.getDefault());
        String formattedTime = sdf.format(message.getTimestamp());

        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            SentMessageViewHolder sentHolder = (SentMessageViewHolder) holder;
            sentHolder.textViewMessageSent.setText(message.getText());
            sentHolder.textViewTimeSent.setText(formattedTime);
        } else {
            ReceivedMessageViewHolder receivedHolder = (ReceivedMessageViewHolder) holder;
            receivedHolder.textViewSenderName.setText(message.getSenderName());
            receivedHolder.textViewMessageReceived.setText(message.getText());
            receivedHolder.textViewTimeReceived.setText(formattedTime);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessageSent, textViewTimeSent;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessageSent = itemView.findViewById(R.id.textViewMessageSent);
            textViewTimeSent = itemView.findViewById(R.id.textViewTimeSent);
        }
    }

    public static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewSenderName, textViewMessageReceived, textViewTimeReceived;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSenderName = itemView.findViewById(R.id.textViewSenderName);
            textViewMessageReceived = itemView.findViewById(R.id.textViewMessageReceived);
            textViewTimeReceived = itemView.findViewById(R.id.textViewTimeReceived);
        }
    }

    // Método para agregar un nuevo mensaje y notificar al adaptador
    public void addMessage(Message newMessage) {
        messageList.add(newMessage);
        notifyItemInserted(messageList.size() - 1);
    }

    // Método para actualizar la lista de mensajes (por ejemplo, al cargar historial)
    public void setMessages(List<Message> newMessages) {
        messageList.clear();
        messageList.addAll(newMessages);
        notifyDataSetChanged();
    }
}