package com.sparta.newsfeed.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.newsfeed.dto.CommentDto.CommentRequestDto;
import com.sparta.newsfeed.entity.Board;
import com.sparta.newsfeed.entity.Comment;
import com.sparta.newsfeed.entity.Users.User;
import com.sparta.newsfeed.jwt.util.JwtTokenProvider;
import com.sparta.newsfeed.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("댓글 태스트")
class CommentServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BoardRepository boardRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private MultimediaRepository multimediaRepository;
    @Mock
    private ContentsLikeRepository contentsLikeRepository;
    @Mock
    private HttpServletRequest servletRequest;
    @Mock
    private HttpServletResponse servletResponse;

    @Mock
    ObjectMapper objectMapper;
    @Mock
    JwtTokenProvider jwt;
    @InjectMocks
    CommentService commentService;



    @Test
    @DisplayName("댓글 생성 태스트")
    void createComment() {
        // given
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setId(1L);
        commentRequestDto.setContents("test comment");

        User user = new User();
        user.setId(1L);

        Board board = new Board();
        board.setId(1L);
        board.setContents("test board");
        board.setCommentList(new ArrayList<Comment>());

        when(jwt.getTokenUser(servletRequest)).thenReturn(user);
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // when
        String comment = commentService.createComment(servletRequest,1L ,commentRequestDto);

        // then
        assertEquals("개시판 ::test board의\n test comment라는 댓글이 입력되었습니다.", comment);
        System.out.println("결과 반환: " + comment);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글")

    void boardComment() {
    }

    @Test
    @DisplayName("댓글")
    void boardCommentView() {
    }

    @Test
    @DisplayName("댓글")
    void boardCommentLike() {
    }

    @Test
    @DisplayName("댓글")
    void boardCommentNolike() {
    }

    @Test
    @DisplayName("댓글")
    void updateComment() {
    }

    @Test
    @DisplayName("댓글")
    void delete() {
    }
}