package com.sparta.newsfeed.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.newsfeed.dto.BoardDto.BoardResponseDto;
import com.sparta.newsfeed.dto.CommentDto.CommentRequestDto;
import com.sparta.newsfeed.dto.CommentDto.CommentResponseDto;
import com.sparta.newsfeed.entity.Board;
import com.sparta.newsfeed.entity.Comment;
import com.sparta.newsfeed.entity.Likes.ContentsLike;
import com.sparta.newsfeed.entity.Likes.LikeContents;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

        User user = getUser();
        Board board = getBoard(user);

        when(jwt.getTokenUser(servletRequest)).thenReturn(user);
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // when
        String comment = commentService.createComment(servletRequest,1L ,commentRequestDto);

        // then
        assertEquals("개시판 ::게시글 내용의\n test comment라는 댓글이 입력되었습니다.", comment);
        System.out.println("결과 반환: " + comment);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }



    @Test
    @DisplayName("개시판의 댓글 전채 조회 태스트")
    void boardComment() {
        // given
        User user = getUser();
        Board board = getBoard(user);
        List<Comment> comments = new ArrayList<>();
        board.setCommentList(comments);

        Comment comment = getComment();
        Comment comment1 = getComment();
        comment1.setId(2L);
        Comment comment2 = getComment();
        comment2.setId(3L);

        comments.add(comment);
        comments.add(comment1);
        comments.add(comment2);

        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
        when(commentRepository.findAllByBoard(board)).thenReturn(comments);

        // when
        List<CommentResponseDto>  list = commentService.boardComment(1L);

        // then
        assertEquals(2L, list.get(1).getId());
        assertEquals("댓글 내용", list.get(1).getContents());
        ResultMessage(list.get(1));
    }

    @Test
    @DisplayName("특정 댓글 보기 태스트")
    void boardCommentView() {
        // given
        Comment comment = getComment();
        when(contentsLikeRepository.countByLikeContentsAndContents(LikeContents.COMMENT, comment.getId())).thenReturn(1L);
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        // when
        CommentResponseDto  responseDto = commentService.boardCommentView(1L,comment.getId());
        // then
        assertEquals(1L, responseDto.getId());
        ResultMessage(responseDto);

    }

    @Test
    @DisplayName("댓글 좋아요 태스트")
    void boardCommentLike() {
        // given
        User user = getUser();
        Board board = getBoard(user);
        Comment comment = getComment();

        when(jwt.getTokenUser(servletRequest)).thenReturn(user);
        when(contentsLikeRepository.countByLikeContentsAndContents(LikeContents.COMMENT, comment.getId())).thenReturn(1L);
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        when(contentsLikeRepository.existsByUser_IdAndLikeContentsAndContents(user.getId(), LikeContents.COMMENT , comment.getId()))
                .thenReturn(false);
        ContentsLike contentsLike = new ContentsLike(user, comment);
        when(contentsLikeRepository.save(any(ContentsLike.class))).thenAnswer(invocation -> invocation.getArgument(0));
        comment.setLikecounts(1L);
        // when
        CommentResponseDto responseDto = commentService.boardCommentLike(servletRequest,board.getId() ,comment.getId());
        // then
        assertEquals(1L, responseDto.getId());
        assertEquals("좋아요를 누르셨습니다.", responseDto.getMessage());

        ResultMessage(responseDto);
        System.out.println(responseDto.getMessage());
    }

    @Test
    @DisplayName("댓글 좋아요 실패 태스트")
    void boardCommentLikeFail() {
        // given
        User user = getUser();
        Board board = getBoard(user);
        Comment comment = getComment();

        when(jwt.getTokenUser(servletRequest)).thenReturn(user);
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        when(contentsLikeRepository.existsByUser_IdAndLikeContentsAndContents(user.getId(), LikeContents.COMMENT , comment.getId()))
                .thenReturn(true);
        ContentsLike contentsLike = new ContentsLike(user, comment);

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            commentService.boardCommentLike(servletRequest,board.getId() ,comment.getId());
        });
        // then
        assertEquals("이미 좋아요를 눌렀습니다", exception.getMessage());
        System.out.println(exception.getMessage());
    }

    @Test
    @DisplayName("댓글 좋아요 취소 태스트")
    void boardCommentNolike() {
        // given
        User user = getUser();
        Board board = getBoard(user);
        Comment comment = getComment();

        when(jwt.getTokenUser(servletRequest)).thenReturn(user);
        when(contentsLikeRepository.countByLikeContentsAndContents(LikeContents.COMMENT, comment.getId())).thenReturn(1L);
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        when(contentsLikeRepository.existsByUser_IdAndLikeContentsAndContents(user.getId(), LikeContents.COMMENT , comment.getId()))
                .thenReturn(true);
        ContentsLike contentsLike = new ContentsLike(user, comment);
        comment.setLikecounts(1L);

        // when
        CommentResponseDto responseDto = commentService.boardCommentNolike(servletRequest,board.getId() ,comment.getId());

        // then
        assertEquals(1L, responseDto.getId());
        assertEquals("좋아요가 취소되었습니다.", responseDto.getMessage());

        ResultMessage(responseDto);
        System.out.println(responseDto.getMessage());
    }


    @Test
    @DisplayName("댓글 좋아요 취소 실패 태스트")
    void boardCommentNolikeFail() {
        // given
        User user = getUser();
        Board board = getBoard(user);
        Comment comment = getComment();

        when(jwt.getTokenUser(servletRequest)).thenReturn(user);
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        when(contentsLikeRepository.existsByUser_IdAndLikeContentsAndContents(user.getId(), LikeContents.COMMENT , comment.getId()))
                .thenReturn(false);
        ContentsLike contentsLike = new ContentsLike(user, comment);
        comment.setLikecounts(1L);

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            commentService.boardCommentNolike(servletRequest,board.getId() ,comment.getId());
        });
        // then
        assertEquals("좋아요를 누르지 않았습니다", exception.getMessage());
        System.out.println(exception.getMessage());
    }

    @Test
    @DisplayName("댓글 업데이트 태스트")
    void updateComment() {
        // given
        User user = getUser();
        Board board = getBoard(user);
        Comment comment = getComment();
        comment.setContents("업데이트 전");
        comment.setUser_id(1L);

        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setId(1L);
        commentRequestDto.setContents("업데이트 됌");

        when(jwt.getTokenUser(servletRequest)).thenReturn(user);
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));


        // when
        String commentr = commentService.updateComment(servletRequest,1L,1L,commentRequestDto);
        // then
        assertEquals("변경 전 :업데이트 전\n변경 후 :" + commentRequestDto.getContents(), commentr);
        System.out.println(commentr);
    }

    @Test
    @DisplayName("댓글 업데이트 실페 태스트")
    void updateCommentFail() {
        // given
        User user = getUser();
        Board board = getBoard(user);
        Comment comment = getComment();
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setContents("업데이트 이후");

        when(jwt.getTokenUser(servletRequest)).thenReturn(user);
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));


        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            commentService.updateComment(servletRequest,board.getId(),comment.getId(),commentRequestDto);
        });
        // then
        assertEquals("소유한 댓글이 아닙니다.", exception.getMessage());
        System.out.println(exception.getMessage());
    }

    @Test
    @DisplayName("댓글")
    void delete() {
        // given
        User user = getUser();
        Board board = getBoard(user);
        Comment comment = getComment();
        comment.setUser_id(1L);

        when(jwt.getTokenUser(servletRequest)).thenReturn(user);
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));


        // when
        String commentr = commentService.delete(servletRequest,1L);
        // then
        assertEquals("댓글 삭제 완료", commentr);
        System.out.println(commentr);
    }

    //:::::::::::::// 도구 상자 //:::::::::::::://


    private static Comment getComment(User user, Board board) {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setContents("댓글 내용");
        comment.setUser(user);
        comment.setBoard(board);
        comment.setLikecounts(0L);
        return comment;
    }

    private static Comment getComment() {
        User user = getUser();
        Board board = getBoard(user);
        return getComment(user, board);
    }

    private static Board getBoard(User user) {
        Board board = new Board();
        board.setId(1L);
        board.setContents("게시글 내용");
        List<Comment> commentList = new ArrayList<>();
        commentList.add(getComment(user, board));
        board.setCommentList(commentList);
        board.setLikecounts(0L);
        board.setUser(user);
        return board;
    }

    private static User getUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("사용자1");
        user.setContentsLikeList(new ArrayList<>());
        user.setBoardList(new ArrayList<>());
        return user;
    }

    private static void ResultMessage(CommentResponseDto resultDto) {
        System.out.println("댓글 내용:");
        System.out.println("ID: " + resultDto.getId());
        System.out.println("사용자 ID: " + getUser().getId());
        System.out.println("내용: " + resultDto.getContents());
        System.out.println("좋아요 수: " + resultDto.getLike_count());
    }
}