package com.example.miprimeraaplicacion;

import java.util.List;

public class CommentAdapterBuilder {
    private List<Comment> commentList;

    public CommentAdapterBuilder setCommentList(List<Comment> commentList) {
        this.commentList = commentList;
        return this;
    }

    public CommentAdapter createCommentAdapter() {
        return new CommentAdapter(commentList);
    }
}