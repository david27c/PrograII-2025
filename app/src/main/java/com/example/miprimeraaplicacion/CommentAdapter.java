package com.example.miprimeraaplicacion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;

    // Constructor único y correcto para el adaptador
    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.authorTextView.setText(comment.getAuthorName());
        holder.commentTextView.setText(comment.getText());

        // Formato de fecha para mostrar el comentario
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        holder.dateTextView.setText(sdf.format(new Date(comment.getTimestamp())));
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView authorTextView;
        TextView commentTextView;
        TextView dateTextView;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            authorTextView = itemView.findViewById(R.id.comment_author_tv);
            commentTextView = itemView.findViewById(R.id.comment_text_tv);
            dateTextView = itemView.findViewById(R.id.comment_date_tv);
        }
    }

    // Método para actualizar los datos del adaptador (útil si los comentarios cambian dinámicamente)
    public void updateComments(List<Comment> newCommentList) {
        this.commentList = newCommentList;
        notifyDataSetChanged(); // Notifica al RecyclerView que los datos han cambiado
    }

    private class Comment {
        public int getAuthorName() {
            int i = 0;
            return i;
        }

        public int getText() {
            int i = 0;
            return i;
        }

        public long getTimestamp() {
            return 0;
        }

        public void setTimestamp(long timestamp) {
        }
    }
}