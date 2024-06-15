package com.sparta.newsfeed.dto.BoardDto;

import com.sparta.newsfeed.entity.Board;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class BoardResponseDto {

    private final Long boardId;
    private final Long boardUserId;
    private final String boardContents;
    private final LocalDateTime createdTime;
    private final LocalDateTime modifiedTime;
    private final Long Likecounts;
    private String message;

    public BoardResponseDto(Board board) {
        this.boardId = board.getId();
        this.boardUserId = board.getUser().getId();
        this.boardContents = board.getContents();
        this.createdTime = board.getCreatedTime();
        this.modifiedTime = board.getModifiedTime();
        this.Likecounts = board.getLikecounts();
        this.message = "";
    }

    public BoardResponseDto(Board board, long Likecounts) {
        this.boardId = board.getId();
        this.boardUserId = board.getUser().getId();
        this.boardContents = board.getContents();
        this.createdTime = board.getCreatedTime();
        this.modifiedTime = board.getModifiedTime();
        this.Likecounts = Likecounts;
    }

    public BoardResponseDto(Board board, long Likecounts, String message) {
        this.boardId = board.getId();
        this.boardUserId = board.getUser().getId();
        this.boardContents = board.getContents();
        this.createdTime = board.getCreatedTime();
        this.modifiedTime = board.getModifiedTime();
        this.Likecounts = Likecounts;
        this.message = message;
    }

}
