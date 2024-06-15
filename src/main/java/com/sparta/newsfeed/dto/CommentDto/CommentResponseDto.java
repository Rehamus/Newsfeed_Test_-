package com.sparta.newsfeed.dto.CommentDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sparta.newsfeed.entity.Comment;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class CommentResponseDto {

    private final Long id;

    private final Long user_id;

    private final Long board_user_id;

    private final String contents;

    private final Long like_count;

    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime created_at;
    private final LocalDateTime modified_at;


    public CommentResponseDto(Comment comment, long like_count) {
        this.id = comment.getId();
        this.user_id = comment.getUser().getId();
        this.board_user_id = comment.getBoard().getUser().getId();
        this.contents = comment.getContents();
        this.created_at = comment.getCreatedTime();
        this.modified_at = comment.getModifiedTime();
        this.like_count = like_count;
    }

    public CommentResponseDto(Comment comment, long like_count, String message) {
        this.id = comment.getId();
        this.user_id = comment.getUser().getId();
        this.board_user_id = comment.getBoard().getUser().getId();
        this.contents = comment.getContents();
        this.created_at = comment.getCreatedTime();
        this.modified_at = comment.getModifiedTime();
        this.like_count = like_count;
        this.message = message;
    }

    public CommentResponseDto(Comment comment) {
        this.id = comment.getId();
        this.user_id = comment.getUser().getId();
        this.board_user_id = comment.getBoard().getUser().getId();
        this.contents = comment.getContents();
        this.created_at = comment.getCreatedTime();
        this.modified_at = comment.getModifiedTime();
        this.like_count = 0L;
    }
}