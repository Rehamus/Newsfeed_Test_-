package com.sparta.newsfeed.controller;

import com.sparta.newsfeed.dto.CommentDto.CommentRequestDto;
import com.sparta.newsfeed.dto.CommentDto.CommentResponseDto;
import com.sparta.newsfeed.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{boardId}/comment")
    @Operation(summary = "댓글 생성", tags = {"댓글"})
    public String createComment(
            HttpServletRequest servletRequest,
            @PathVariable Long boardId,
            @RequestBody CommentRequestDto commentRequestDto) {
        return commentService.createComment(servletRequest, boardId, commentRequestDto);
    }

    @GetMapping("/{boardId}/comment")
    @Operation(summary = "개시판의 특정 댓글 조회", tags = {"댓글"})
    public List<CommentResponseDto> boardComment(
            @PathVariable Long boardId) {
        return commentService.boardComment(boardId);
    }

    @GetMapping("/{boardId}/comment/{commentid}")
    @Operation(summary = "개시판의 댓글 조회", tags = {"댓글"})
    public CommentResponseDto boardCommentView(
            @PathVariable Long boardId,
            @PathVariable Long commentid) {
        return commentService.boardCommentView(boardId, commentid);
    }

    @GetMapping("/{boardId}/comment/{commentid}/like")
    @Operation(summary = "개시판의 특정 댓글 좋아요", tags = {"댓글"})
    public CommentResponseDto boardCommentLike(
            HttpServletRequest servletRequest,
            @PathVariable Long boardId,
            @PathVariable Long commentid) {
        return commentService.boardCommentLike(servletRequest, boardId, commentid);
    }

    @GetMapping("/{boardId}/comment/{commentid}/nolike")
    @Operation(summary = "개시판의 특정 댓글 좋아요 취소", tags = {"댓글"})
    public CommentResponseDto boardCommentNolike(
            HttpServletRequest servletRequest,
            @PathVariable Long boardId,
            @PathVariable Long commentid) {
        return commentService.boardCommentNolike(servletRequest, boardId, commentid);
    }

    @PatchMapping("/{boardId}/comment/{commentId}")
    @Operation(summary = "댓글 수정", tags = {"댓글"})

    public String updateComment(
            HttpServletRequest servletRequest,
            @PathVariable Long commentId,
            @PathVariable Long boardId,
            @RequestBody CommentRequestDto commentRequestDto) {
        return commentService.updateComment(servletRequest, boardId, commentId, commentRequestDto);
    }

    @DeleteMapping("/{boardId}/comment/{commentId}")
    @Operation(summary = "댓글 삭재", tags = {"댓글"})
    public String deleteComment(
            HttpServletRequest servletRequest, @PathVariable long commentId, @PathVariable long boardId) {
        return commentService.delete(servletRequest, commentId);

    }
}