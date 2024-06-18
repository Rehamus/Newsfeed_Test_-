package com.sparta.newsfeed.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.newsfeed.config.WebSecurityConfig;
import com.sparta.newsfeed.dto.CommentDto.CommentRequestDto;
import com.sparta.newsfeed.dto.CommentDto.CommentResponseDto;
import com.sparta.newsfeed.entity.Board;
import com.sparta.newsfeed.entity.Comment;
import com.sparta.newsfeed.entity.Users.User;
import com.sparta.newsfeed.filter.TestMockFilter;
import com.sparta.newsfeed.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(
        controllers = {CommentController.class},
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = WebSecurityConfig.class
                )
        }
)
@DisplayName("댓글 테스트")
class CommentControllerTest {

    @MockBean
    private CommentService commentService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity(new TestMockFilter()))
                .build();
    }


    @Test
    @DisplayName("댓글 작성")
    void createComment() throws Exception {
        // given
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setContents("테스트 댓글");
        Board board = getBoard(getUser());

        when(commentService.createComment(any(), anyLong(), any())).thenReturn("개시판 ::" + board.getContents() + "의\n " + commentRequestDto.getContents() + "라는 댓글이 입력되었습니다.");
        // when - then
        mockMvc.perform(post("/api/board/{boardId}/comment", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("개시판 ::게시글 내용의\n" +
                                                    " 테스트 댓글라는 댓글이 입력되었습니다."))
                .andDo(print());
    }


    @Test
    @DisplayName("게시글 댓글 조회 테스트")
    public void testBoardComment() throws Exception {
        // given
        Long boardId = 1L;
        List<CommentResponseDto> commentResponseDtoList = new ArrayList<>();
        Comment comment = getComment();
        commentResponseDtoList.add(new CommentResponseDto(comment));

        // when
        when(commentService.boardComment(anyLong())).thenReturn(commentResponseDtoList);

        // then
        mockMvc.perform(get("/api/board/{boardId}/comment", boardId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].contents").value("댓글 내용"))
                .andExpect(jsonPath("$[0].board_user_id").value(1L))
                .andExpect(jsonPath("$[0].like_count").value(0L))
                .andExpect(jsonPath("$[0].user_id").value(1L)
                )
                .andDo(print());
    }


    @Test
    @DisplayName("특정 댓글 조회 테스트")
    void boardCommentView() throws Exception {
        // given
        Long boardId = 1L;
        Long commentId = 1L;
        Comment comment = getComment();

        CommentResponseDto commentResponseDto = new CommentResponseDto(comment);

        // when
        when(commentService.boardCommentView(anyLong(),anyLong())).thenReturn(commentResponseDto);

        // then
        mockMvc.perform(get("/api/board/{boardId}/comment/{commentId}", boardId,commentId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.contents").value("댓글 내용"))
                .andExpect(jsonPath("$.board_user_id").value(1L))
                .andExpect(jsonPath("$.like_count").value(0L))
                .andExpect(jsonPath("$.user_id").value(1L))
                .andDo(print());
    }

    @Test
    @DisplayName("특정 댓글 조회 실패 테스트")
    void boardCommentFailView() throws Exception {
        // given
        Long boardId = 1L;
        Long commentId = 2L;

        // mock
        when(commentService.boardCommentView(anyLong(), anyLong()))
                .thenThrow(new IllegalArgumentException("해당 개시판이 없습니다."));

        // when - then
        mockMvc.perform(get("/api/board/{boardId}/comment/{commentId}", boardId, commentId))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.httpStatusCode").value(400))
                .andExpect(jsonPath("$.message").value("해당 개시판이 없습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("좋아요 테스트")
    void boardCommentLike() throws Exception {
        // given
        Long boardId = 1L;
        Long commentId = 1L;
        Comment comment = getComment();

        CommentResponseDto commentResponseDto = new CommentResponseDto(comment,1,"좋아요 테스트");

        // when
        when(commentService.boardCommentLike(any(), anyLong(), anyLong())).thenReturn(commentResponseDto);

        // then
        mockMvc.perform(get("/api/board/{boardId}/comment/{commentId}/like", boardId,commentId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.like_count").value(1L))
                .andExpect(jsonPath("$.message").value("좋아요 테스트"))
                .andDo(print());
    }

    @RepeatedTest(value = 3, name = "좋아요 실패 테스트 {currentRepetition}/{totalRepetitions}")
    @DisplayName("좋아요 실패 테스트")
    void boardCommentLikeFail(RepetitionInfo repetitionInfo) throws Exception {
        // given
        Long boardId = 1L;
        Long commentId = 1L;
        Comment comment = getComment();
        CommentResponseDto commentResponseDto = new CommentResponseDto(comment, 1L, "좋아요 테스트");

        // when
        if (repetitionInfo.getCurrentRepetition() > 1) {
            when(commentService.boardCommentLike(any(), anyLong(), anyLong()))
                    .thenThrow(new IllegalArgumentException("이미 좋아요를 눌렀습니다"));
        } else {
            when(commentService.boardCommentLike(any(), anyLong(), anyLong())).thenReturn(commentResponseDto);
        }

        // when - then
        ResultActions resultActions = mockMvc.perform(get("/api/board/{boardId}/comment/{commentId}/like", boardId, commentId));
        if (repetitionInfo.getCurrentRepetition() > 1) {
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("이미 좋아요를 눌렀습니다"))
                    .andDo(print());

        } else {
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.user_id").value(1L))
                    .andExpect(jsonPath("$.message").value("좋아요 테스트"))
                    .andDo(print());

        }
    }

    @Test
    @DisplayName("좋아요 취소 테스트")
    void boardCommentNolike() throws Exception {
        // given
        Long boardId = 1L;
        Long commentId = 1L;
        Comment comment = getComment();
        CommentResponseDto commentResponseDto = new CommentResponseDto(comment,0,"좋아요 테스트");
        commentResponseDto.setMessage("좋아요가 취소되 었습니다 테스트");

        // when
        when(commentService.boardCommentNolike(any(), anyLong(), anyLong())).thenReturn(commentResponseDto);

        // then
        mockMvc.perform(get("/api/board/{boardId}/comment/{commentId}/nolike", boardId,commentId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.like_count").value(0L))
                .andExpect(jsonPath("$.message").value("좋아요가 취소되 었습니다 테스트"))
                .andDo(print());
    }

    @RepeatedTest(value = 3, name = "좋아요 실패 테스트 {currentRepetition}/{totalRepetitions}")
    @DisplayName("좋아요 취소 실패 테스트")
    void boardCommentnoLikeFail(RepetitionInfo repetitionInfo) throws Exception {
        // given
        Long boardId = 1L;
        Long commentId = 1L;
        Comment comment = getComment();
        CommentResponseDto commentResponseDto = new CommentResponseDto(comment, 1L, "좋아요 테스트");

        if (repetitionInfo.getCurrentRepetition() > 1) {
            when(commentService.boardCommentNolike(any(), anyLong(), anyLong()))
                    .thenThrow(new IllegalArgumentException("좋아요를 누르지 않았습니다"));
        } else {
            when(commentService.boardCommentNolike(any(), anyLong(), anyLong())).thenReturn(commentResponseDto);
        }

        // when - then
        ResultActions resultActions = mockMvc.perform(get("/api/board/{boardId}/comment/{commentId}/nolike", boardId, commentId));
        if (repetitionInfo.getCurrentRepetition() > 1) {
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("좋아요를 누르지 않았습니다"))
                    .andDo(print());

        } else {
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.user_id").value(1L))
                    .andExpect(jsonPath("$.message").value("좋아요 테스트"))
                    .andDo(print());

        }
    }


    @Test
    @DisplayName("댓글 업데이트 테스트")
    void updateComment() throws Exception {
        // given
        Long boardId = 1L;
        Long commentId = 1L;
        Comment comment = getComment();
        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setContents("수정된 댓글 내용");

        comment.update(requestDto);

        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setId(comment.getId());
        commentRequestDto.setUser_id(comment.getUser_id());
        commentRequestDto.setContents(comment.getContents());


        // when
        when(commentService.updateComment(any(), anyLong(), anyLong(), any())).thenReturn("수정된 댓글 내용");

        // then
        mockMvc.perform(patch("/api/board/{boardId}/comment/{commentId}", boardId,commentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andExpect(status().isOk())
                .andDo(print());
    }


    @Test
    @DisplayName("댓글 삭제 테스트")
    void deleteComment() throws Exception {
        // given
        Long boardId = 1L;
        Long commentId = 1L;

        // when
        when(commentService.delete(any(), anyLong())).thenReturn("댓글 삭제 완료");

        // then
        mockMvc.perform(delete("/api/board/{boardId}/comment/{commentId}", boardId, commentId))
                .andExpect(status().isOk())
                .andDo(print());

        verify(commentService, times(1)).delete(any(HttpServletRequest.class), eq(commentId));
    }

    //:::::::::::::// 도구 상자 //:::::::::::::://



    private static Comment getComment(User user, Board board) {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setContents("댓글 내용");
        comment.setUser(user);
        comment.setBoard(board);
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
        board.setUser(user);
        return board;
    }

    private static User getUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("사용자1");
        return user;
    }




}