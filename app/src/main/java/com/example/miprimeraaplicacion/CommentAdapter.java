package com.example.miprimeraaplicacion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.miprimeraaplicacion.R;
import com.example.miprimeraaplicacion.models.Comment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    public CommentAdapter(List<com.example.miprimeraaplicacion.Comment> commentList) {

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
}