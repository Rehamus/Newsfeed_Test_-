package com.sparta.newsfeed.service;


import com.sparta.newsfeed.dto.CommentDto.CommentRequestDto;
import com.sparta.newsfeed.dto.CommentDto.CommentResponseDto;
import com.sparta.newsfeed.entity.Board;
import com.sparta.newsfeed.entity.Comment;
import com.sparta.newsfeed.entity.Likes.ContentsLike;
import com.sparta.newsfeed.entity.Likes.LikeContents;
import com.sparta.newsfeed.entity.Users.User;
import com.sparta.newsfeed.jwt.util.JwtTokenProvider;
import com.sparta.newsfeed.repository.BoardRepository;
import com.sparta.newsfeed.repository.CommentRepository;
import com.sparta.newsfeed.repository.ContentsLikeRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class CommentService {


    private final ContentsLikeRepository contentsLikeRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final JwtTokenProvider jwt;

    //댓글 생성
    @Transactional
    public String createComment(HttpServletRequest servletRequest, Long boardId, CommentRequestDto CommentRequestDto) {
        Board board = getBoard(boardId);
        User user = jwt.getTokenUser(servletRequest);
        Comment comment = new Comment(CommentRequestDto, board, user);
        board.getCommentList().add(comment);
        comment.setBoard_user_id(board.getUser_id());
        comment.setBoard(board);
        commentRepository.save(comment);

        return "개시판 ::" + board.getContents() + "의\n " + comment.getContents() + "라는 댓글이 입력되었습니다.";
    }


    //보드의 댓글 전부 조회
    @Transactional
    public List<CommentResponseDto> boardComment(Long boardId) {
        Board board = getBoard(boardId);
        List<Comment> comments = commentRepository.findAllByBoard(board);
        if (board.getCommentList().isEmpty()) throw new IllegalArgumentException("댓글이 없습니다.");
        List<CommentResponseDto> commentResponseDtos = new ArrayList<>();
        for (Comment comment : comments) {
            long likeCount = getLikeCount(comment.getId());
            commentResponseDtos.add(new CommentResponseDto(comment, likeCount));
        }
        return commentResponseDtos;
    }

    // 특정 댓글 가져오기
    public CommentResponseDto boardCommentView(Long boardId, Long commentid) {
        long likeCount = getLikeCount(commentid);
        return new CommentResponseDto(getComment(commentid), likeCount);
    }

    // 댓글 좋아요
    @Transactional
    public CommentResponseDto boardCommentLike(HttpServletRequest servletRequest, Long boardId, Long commentid) {
        Comment comment = getComment(commentid);
        User user = jwt.getTokenUser(servletRequest);

        if (contentsLikeRepository.existsByUser_IdAndLikeContentsAndContents(user.getId(), LikeContents.COMMENT , comment.getId())) {
            throw new IllegalArgumentException("이미 좋아요를 눌렀습니다");
        }
        ContentsLike contentsLike = new ContentsLike(user, comment);
        user.getContentsLikeList().add(contentsLike);
        contentsLikeRepository.save(contentsLike);
        long likeCount = getLikeCount(commentid);
        comment.setLikecounts(likeCount);
        String like_m = "좋아요를 누르셨습니다.";
        return new CommentResponseDto(comment, likeCount, like_m);
    }

    // 댓글 좋아요 취소
    @Transactional
    public CommentResponseDto boardCommentNolike(HttpServletRequest servletRequest, Long boardId, Long commentid) {
        Comment comment = getComment(commentid);
        User user = jwt.getTokenUser(servletRequest);

        if (!contentsLikeRepository.existsByUser_IdAndLikeContentsAndContents(user.getId(), LikeContents.COMMENT , comment.getId())) {
            throw new IllegalArgumentException("좋아요를 누르지 않았습니다");
        }

        ContentsLike contentsLike = contentsLikeRepository.findByUserAndContents(user, comment.getId());
        contentsLikeRepository.delete(contentsLike);
        long likeCount = getLikeCount(commentid);
        comment.setLikecounts(likeCount);
        String like_m = "좋아요가 취소되었습니다.";
        return new CommentResponseDto(comment, likeCount, like_m);
    }


    //댓글 수정
    @Transactional
    public String updateComment(HttpServletRequest servletRequest, Long boardId, Long commentId, CommentRequestDto commentRequestDto) {
        Board board = getBoard(boardId);
        if (board.getCommentList().isEmpty()) throw new IllegalArgumentException("댓글이 없습니다.");
        Comment comment = getComment(servletRequest, commentId);
        String content = comment.getContents();
        comment.update(commentRequestDto);
        return "변경 전 :" + content + "\n변경 후 :" + commentRequestDto.getContents();
    }


    //댓글 삭제
    public String delete(HttpServletRequest servletRequest, Long commentId) {
        Comment comment = getComment(servletRequest, commentId);
        commentRepository.delete(comment);
        return "댓글 삭제 완료";
    }


    //:::::::::::::::::::/* 도구 상자 */:::::::::::::::::::

    // 댓글 유저 확인
    private Comment getComment(HttpServletRequest servletRequest, Long commentId) {
        User user = jwt.getTokenUser(servletRequest);
        Comment comment = getComment(commentId);
        if (!user.getId().equals(comment.getUser_id())) {
            throw new IllegalArgumentException("소유한 댓글이 아닙니다.");
        }
        return comment;
    }

    // id로 Board 가져오기
    public Board getBoard(Long id) {
        return boardRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 개시판이 없습니다."));

    }

    private long getLikeCount(Long commentid) {
        long likeCount = contentsLikeRepository.countByLikeContentsAndContents(LikeContents.COMMENT, commentid);
        return likeCount;
    }


    // id로 Comment 가져오기
    private Comment getComment(Long commentId) {
        return commentRepository
                .findById(commentId).orElseThrow(
                        () -> new IllegalArgumentException("해당 개시판이 없습니다."));
    }
}