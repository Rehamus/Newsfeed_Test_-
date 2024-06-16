package com.sparta.newsfeed.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.newsfeed.config.WebSecurityConfig;
import com.sparta.newsfeed.dto.BoardDto.BoardRequestDto;
import com.sparta.newsfeed.dto.BoardDto.BoardResponseDto;
import com.sparta.newsfeed.entity.Board;
import com.sparta.newsfeed.entity.Users.User;
import com.sparta.newsfeed.filter.TestMockFilter;
import com.sparta.newsfeed.service.BoardService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = {BoardController.class},
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = WebSecurityConfig.class
                )
        }
)
@DisplayName("개시판 태스트")
class BoardControllerTest {

    @MockBean
    private BoardService boardService;

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
    @DisplayName("게시글 생성 테스트")
    void createBoard() throws Exception {
        // given
        BoardRequestDto boardRequestDto = new BoardRequestDto();
        boardRequestDto.setContents("태스트 개시판");
        Board board = new Board(getUser(), boardRequestDto);
        // when
        when(boardService.createBoard(any(), any())).thenReturn(board.getContents() + " 생성 완료");

        // then
        mockMvc.perform(post("/api/board")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(boardRequestDto)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("게시물 + 미디어 생성 테스트")
    void createMBoardTest() throws Exception {
        // Given
        Board board = getBoard(getUser());
        String boardJson = objectMapper.writeValueAsString(board);
        MockMultipartFile imageFile = new MockMultipartFile("image", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy_image_data".getBytes());
        MockMultipartFile movieFile = new MockMultipartFile("movie", "movie.mp4", MediaType.APPLICATION_OCTET_STREAM_VALUE, "dummy_movie_data".getBytes());

        // When
        when(boardService.createMBoard(any(), any(), any(), any()))
                .thenReturn("게시물 + 미디어 생성 완료");

        // Then
        mockMvc.perform(multipart("/api/board/m")
                                .file(imageFile)
                                .file(movieFile)
                                .part(new MockPart("board", boardJson.getBytes()))
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andDo(print());
    }


    @Test
    @DisplayName("게시글 전부 가져오기 테스트")
    void getAllBoard() throws Exception {
        // given
        int page = 1;
        int view = 1;

        Board board = getBoard(getUser());
        List<BoardResponseDto> mockBoardResponseDtoList = new ArrayList<>();
        mockBoardResponseDtoList.add(new BoardResponseDto(board));
        Page<BoardResponseDto> boardResponseDtoList = new PageImpl<>(mockBoardResponseDtoList);

        // when
        when(boardService.getAllBoard(any(), eq(page - 1), eq(view), any(),any())).thenReturn(boardResponseDtoList);


        // then
        mockMvc.perform(get("/api/board/{page}/{view}", page,view))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].boardId").value(1L))
                .andExpect(jsonPath("$[0].boardUserId").value(1L))
                .andExpect(jsonPath("$[0].boardContents").value("게시글 내용"))
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 가져오기 테스트")
    void getBoard() throws Exception {
        // given
        Long boardId = 1L;
        Board board = getBoard(getUser());
        BoardResponseDto responseDto = new BoardResponseDto(board);

        // when
        when(boardService.getBoard(anyLong())).thenReturn(responseDto);

        // then
        mockMvc.perform(get("/api/board/v/{boardId}", boardId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.boardId").value(1L))
                .andExpect(jsonPath("$.boardUserId").value(1L))
                .andExpect(jsonPath("$.boardContents").value("게시글 내용"))
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 좋아요 테스트")
    void getBoardLike() throws Exception {
        // given
        Long boardId = 1L;
        Board board = getBoard(getUser());
        String like_m = "좋아요를 누르셨습니다.";
        BoardResponseDto responseDto = new BoardResponseDto(board, 1L, like_m);

        // when
        when(boardService.getBoardLike(any(),anyLong())).thenReturn(responseDto);

        // then
        mockMvc.perform(get("/api/board/v/{boardId}/like", boardId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.boardId").value(1L))
                .andExpect(jsonPath("$.boardUserId").value(1L))
                .andExpect(jsonPath("$.likecounts").value(1L))
                .andExpect(jsonPath("$.boardContents").value("게시글 내용"))
                .andExpect(jsonPath("$.message").value("좋아요를 누르셨습니다."))
                .andDo(print());
    }

    @RepeatedTest(value = 3, name = "좋아요 실패 테스트 {currentRepetition}/{totalRepetitions}")
    @DisplayName("게시글 좋아요 실패 테스트")
    void getBoardLikeFail(RepetitionInfo repetitionInfo) throws Exception {
        // given
        Long boardId = 1L;
        Board board = getBoard(getUser());
        String like_m = "좋아요를 누르셨습니다.";
        BoardResponseDto responseDto = new BoardResponseDto(board, 1L, like_m);

        // when
        if (repetitionInfo.getCurrentRepetition() > 1) {
            when(boardService.getBoardLike(any(),anyLong()))
                    .thenThrow( new IllegalArgumentException("이미 좋아요를 눌렀습니다"));

        } else {
            when(boardService.getBoardLike(any(),anyLong())).thenReturn(responseDto);
        }

        // then
        ResultActions resultActions =  mockMvc.perform(get("/api/board/v/{boardId}/like", boardId));
        if (repetitionInfo.getCurrentRepetition() > 1) {
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("이미 좋아요를 눌렀습니다"))
                .andDo(print());
        } else {
            resultActions.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.likecounts").value(1L))
                    .andExpect(jsonPath("$.message").value("좋아요를 누르셨습니다."))
                    .andDo(print());
        }

    }

    @Test
    @DisplayName("게시글 좋아요 취소 테스트")
    void getBoardNolike() throws Exception {
        // given
        Long boardId = 1L;
        Board board = getBoard(getUser());
        String like_m = "좋아요가 취소되었습니다.";
        BoardResponseDto responseDto = new BoardResponseDto(board, 0L, like_m);

        // when
        when(boardService.getBoardNolike(any(),anyLong())).thenReturn(responseDto);

        // then
        mockMvc.perform(get("/api/board/v/{boardId}/nolike", boardId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.boardId").value(1L))
                .andExpect(jsonPath("$.boardUserId").value(1L))
                .andExpect(jsonPath("$.likecounts").value(0L))
                .andExpect(jsonPath("$.boardContents").value("게시글 내용"))
                .andExpect(jsonPath("$.message").value("좋아요가 취소되었습니다."))
                .andDo(print());
    }

    @RepeatedTest(value = 3, name = "좋아요 실패 테스트 {currentRepetition}/{totalRepetitions}")
    @DisplayName("게시글 좋아요 취소 실패 테스트")
    void getBoardNolikeFail(RepetitionInfo repetitionInfo) throws Exception {
        // given
        Long boardId = 1L;
        Board board = getBoard(getUser());
        String like_m =  "좋아요가 취소되었습니다.";
        BoardResponseDto responseDto = new BoardResponseDto(board, 0L, like_m);

        // when
        if (repetitionInfo.getCurrentRepetition() > 1) {
            when(boardService.getBoardNolike(any(),anyLong()))
                    .thenThrow( new IllegalArgumentException("좋아요를 안눌렀습니다"));

        } else {
            when(boardService.getBoardNolike(any(),anyLong())).thenReturn(responseDto);
        }
        // then
        ResultActions resultActions =   mockMvc.perform(get("/api/board/v/{boardId}/nolike", boardId));
        if (repetitionInfo.getCurrentRepetition() > 1) {
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("좋아요를 안눌렀습니다"))
                    .andDo(print());
        } else {
            resultActions.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.likecounts").value(0L))
                    .andExpect(jsonPath("$.message").value( "좋아요가 취소되었습니다."))
                    .andDo(print());
        }
    }

    @Test
    @DisplayName("게시글 삭제 테스트")
    void deleteBoard() throws Exception {
        // given
        BoardRequestDto RequestDto = new BoardRequestDto();
        // when
        when(boardService.deleteBoard(any(), any())).thenReturn("삭제 완료");

        // then
        mockMvc.perform(delete("/api/board")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(RequestDto)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 업데이트 테스트")
    void updateBoard() throws Exception {
        // given
        BoardRequestDto RequestDto = new BoardRequestDto();
        // when
        when(boardService.updateBoard(any(), any())).thenReturn("수정 완료");

        // then
        mockMvc.perform(patch("/api/board")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(RequestDto)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 + 미디어 업데이트 테스트")
    void updateMBoard() throws Exception {
        // Given
        Board board = getBoard(getUser());
        String boardJson = objectMapper.writeValueAsString(board);
        MockMultipartFile imageFile = new MockMultipartFile("image", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy_image_data".getBytes());
        MockMultipartFile movieFile = new MockMultipartFile("movie", "movie.mp4", MediaType.APPLICATION_OCTET_STREAM_VALUE, "dummy_movie_data".getBytes());

        Map<String, Object> boardMap = new HashMap<>();
        boardMap.put("id", 1L);
        boardMap.put("contents", "수정된 게시글 내용");

        String boardJsonUpdate = objectMapper.writeValueAsString(boardMap);

        // When
        when(boardService.updateMBoard(any(), any(), any(), any()))
                .thenReturn("게시물 + 미디어 수정 완료");

        // Then
        mockMvc.perform(multipart("/api/board/m")
                                .file(imageFile)
                                .file(movieFile)
                                .part(new MockPart("board", boardJsonUpdate.getBytes()))
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 + 미디어 업데이트 테스트 실패 케이스")
    void updateMBoardFailure() throws Exception {
        // Given
        Map<String, Object> boardMap = new HashMap<>();
        boardMap.put("id", 1L);
        boardMap.put("contents", "수정된 게시글 내용");

        String boardJsonUpdate = objectMapper.writeValueAsString(boardMap);

        MockMultipartFile imageFile = new MockMultipartFile("image", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy_image_data".getBytes());
        MockMultipartFile movieFile = new MockMultipartFile("movie", "movie.mp4", MediaType.APPLICATION_OCTET_STREAM_VALUE, "dummy_movie_data".getBytes());

        // When
        when(boardService.updateMBoard(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("업데이트 실패"));

        // Then
        mockMvc.perform(multipart("/api/board/m")
                                .file(imageFile)
                                .file(movieFile)
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }


    //:::::::::::::// 도구 상자 //:::::::::::::://


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