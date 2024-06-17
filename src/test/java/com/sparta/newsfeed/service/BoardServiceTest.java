package com.sparta.newsfeed.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.newsfeed.dto.BoardDto.BoardRequestDto;
import com.sparta.newsfeed.dto.BoardDto.BoardResponseDto;
import com.sparta.newsfeed.dto.UserDto.SignUpRequestDto;
import com.sparta.newsfeed.entity.Board;
import com.sparta.newsfeed.entity.Likes.ContentsLike;
import com.sparta.newsfeed.entity.Likes.LikeContents;
import com.sparta.newsfeed.entity.Multimedia;
import com.sparta.newsfeed.entity.Users.User;
import com.sparta.newsfeed.jwt.util.JwtTokenProvider;
import com.sparta.newsfeed.repository.BoardRepository;
import com.sparta.newsfeed.repository.ContentsLikeRepository;
import com.sparta.newsfeed.repository.MultimediaRepository;
import com.sparta.newsfeed.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("개시판 태스트")
class BoardServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BoardRepository boardRepository;
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
    BoardService boardService;

    User user;
    BoardRequestDto boardRequestDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("유저");
        boardRequestDto = new BoardRequestDto();
        boardRequestDto.setId(1L);
        boardRequestDto.setUser_id(1L);
        boardRequestDto.setContents("개시판 내용");
        boardService = new BoardService(
                boardRepository, multimediaRepository, objectMapper, jwt, contentsLikeRepository);
    }

    @Test
    @DisplayName("개시판 생성 태스트")
    void createBoard() {
        // given
        when(jwt.getTokenUser(servletRequest)).thenReturn(user);
        when(boardRepository.save(any(Board.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // when
        String board = boardService.createBoard(servletRequest, boardRequestDto);

        // then
        assertEquals("개시판 내용 생성 완료", board);
        System.out.println("결과 반환: " + board);
        verify(boardRepository, times(1)).save(any(Board.class));

    }

  /*  @Test
    @DisplayName("개시판 + 미디어 생성 완료")
    void createMBoard() {
        MockitoAnnotations.openMocks(this);

        // given
        MockMultipartFile imageFile = new MockMultipartFile("image", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy_image_data".getBytes());
        MockMultipartFile movieFile = new MockMultipartFile("movie", "movie.mp4", MediaType.APPLICATION_OCTET_STREAM_VALUE, "dummy_movie_data".getBytes());
        String boardJson = "{\"id\":1,\"contents\":\"Test Content\"}";

        // when
        when(jwt.getTokenUser(servletRequest)).thenReturn(user);
        doNothing().when(boardService).uploadFileToS3(anyString(), any(), anyString());
        when(boardService.getS3Url(anyString())).thenReturn("https://onebytenewsfeed.s3.amazonaws.com/images/" + UUID.randomUUID());
        String board = boardService.createMBoard(servletRequest, imageFile, movieFile, boardJson);

        // then
        System.out.println("개시판 + 미디어 내용: " + board);
        assertEquals("생성 완료", board);
        verify(boardRepository, times(1)).save(any(Board.class));
        verify(multimediaRepository, times(1)).save(any(Multimedia.class));
    }*/

    @Test
    @DisplayName("게시판 목록 조회 테스트")
    void getAllBoard() {
        // given
        int page = 0;
        int view = 1;
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        LocalDateTime end = LocalDateTime.now();

        User user = new User();
        user.setId(1L);
        user.setUsername("test_user");

        Board board1 = new Board();
        board1.setId(1L);
        board1.setContents("Test Content 1");
        board1.setLikecounts(10L);
        board1.setUser(user);

        Board board2 = new Board();
        board2.setId(2L);
        board2.setContents("Test Content 2");
        board2.setLikecounts(5L);
        board2.setUser(user);

        List<Board> mockBoards = Arrays.asList(board1, board2);
        Page<Board> mockBoardPage = new PageImpl<>(mockBoards);

        // when
        when(boardRepository.findAll(any(Pageable.class))).thenReturn(mockBoardPage);
        Page<BoardResponseDto> resultPage = boardService.getAllBoard(servletRequest, page, view, start, end);

        // then
        assertNotNull(resultPage);
        assertEquals(mockBoardPage.getTotalElements(), resultPage.getTotalElements());
        assertEquals(mockBoards.size(), resultPage.getContent().size());

        BoardResponseDto responseDto = resultPage.getContent().get(0);
        assertEquals(board1.getId(), responseDto.getBoardId());
        assertEquals(board1.getContents(), responseDto.getBoardContents());
        assertEquals(board1.getLikecounts(), responseDto.getLikecounts());
        assertEquals(board1.getCreatedTime(), responseDto.getCreatedTime());
        assertEquals(board1.getUser().getId(), responseDto.getBoardUserId());

        System.out.println("테스트 결과:");
        System.out.println("총 게시물 수: " + resultPage.getTotalElements());
        System.out.println("페이지 내 게시물 수: " + resultPage.getContent().size());

        BoardResponseDto responseDtoone = resultPage.getContent().get(0);
        ResultMessage(responseDtoone);

        verify(boardRepository, times(1)).findAll(any(Pageable.class));
    }


    @Test
    @DisplayName("개시판 전체 가져오기 태스트")
    void getBoard() {
        // given
        User user = new User();
        user.setId(1L);
        user.setUsername("test_user");

        long boardId = 1L;
        Board board = new Board();
        board.setId(boardId);
        board.setContents("Test Board");
        board.setUser(user);

        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
        when(contentsLikeRepository.countByLikeContentsAndContents(LikeContents.BOARD, 1L)).thenReturn(10L);

        // when
        BoardResponseDto resultDto = boardService.getBoard(boardId);

        // then
        assertEquals(boardId, resultDto.getBoardId());
        assertEquals("Test Board", resultDto.getBoardContents());
        assertEquals(10L, resultDto.getLikecounts());

        ResultMessage(resultDto);
    }

    @Test
    @DisplayName("게시판 좋아요 태스트")
    void getBoardLike() {
        // given
        List<ContentsLike> contentsLikeList = new ArrayList<>();
        User user = new User();
        user.setId(1L);
        user.setUserId("tester123");
        user.setUsername("test_user");
        user.setContentsLikeList(contentsLikeList);

        long boardId = 1L;
        Board board = new Board();
        board.setId(boardId);
        board.setContents("Test Board");
        board.setUser(user);

        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(contentsLikeRepository.countByLikeContentsAndContents(LikeContents.BOARD, boardId)).thenReturn(10L);
        when(contentsLikeRepository.existsByUser_IdAndLikeContentsAndContents(1L, LikeContents.BOARD, boardId)).thenReturn(false);
        when(jwt.getTokenUser(servletRequest)).thenReturn(user);
        // when
        BoardResponseDto resultDto = boardService.getBoardLike(servletRequest, boardId);

        // then
        assertEquals(boardId, resultDto.getBoardId());
        assertEquals("Test Board", resultDto.getBoardContents());
        assertEquals(10L, resultDto.getLikecounts());
        assertEquals("좋아요를 누르셨습니다.", resultDto.getMessage());

        ResultMessage(resultDto);
        System.out.println("메세지 : " + resultDto.getMessage());
    }

    @Test
    @DisplayName("게시판 좋아요 실패 테스트 - 이미 좋아요를 누른 경우")
    public void getBoardLikeFail() {
        // given
        List<ContentsLike> contentsLikeList = new ArrayList<>();
        User user = new User();
        user.setId(1L);
        user.setUserId("tester123");
        user.setUsername("test_user");
        user.setContentsLikeList(contentsLikeList);

        long boardId = 1L;
        Board board = new Board();
        board.setId(boardId);
        board.setContents("Test Board");
        board.setUser(user);

        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(jwt.getTokenUser(servletRequest)).thenReturn(user);
        when(contentsLikeRepository.existsByUser_IdAndLikeContentsAndContents(user.getId(), LikeContents.BOARD, boardId))
                .thenReturn(true);

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            boardService.getBoardLike(servletRequest, boardId);
        });

        // then
        assertEquals("이미 좋아요를 눌렀습니다", exception.getMessage());
        System.out.println("메세지 :" + exception.getMessage());

    }


    @Test
    @DisplayName("게시판 좋아요 취소 태스트")
    void getBoardNolike() {
        // given
        List<ContentsLike> contentsLikeList = new ArrayList<>();
        User user = new User();
        user.setId(1L);
        user.setUserId("tester123");
        user.setUsername("test_user");
        user.setContentsLikeList(contentsLikeList);

        long boardId = 1L;
        Board board = new Board();
        board.setId(boardId);
        board.setContents("Test Board");
        board.setUser(user);

        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(contentsLikeRepository.countByLikeContentsAndContents(LikeContents.BOARD, boardId)).thenReturn(0L);
        when(contentsLikeRepository.existsByUser_IdAndLikeContentsAndContents(1L, LikeContents.BOARD, boardId)).thenReturn(true);
        when(jwt.getTokenUser(servletRequest)).thenReturn(user);
        // when
        BoardResponseDto resultDto = boardService.getBoardNolike(servletRequest, boardId);

        // then
        assertEquals(boardId, resultDto.getBoardId());
        assertEquals("Test Board", resultDto.getBoardContents());
        assertEquals(0L, resultDto.getLikecounts());
        assertEquals("좋아요가 취소되었습니다.", resultDto.getMessage());

        ResultMessage(resultDto);
        System.out.println("메세지 : " + resultDto.getMessage());
    }

    @Test
    @DisplayName("게시판 좋아요 취소 태스트")
    void getBoardNolikeFailone() {
        // given
        List<ContentsLike> contentsLikeList = new ArrayList<>();
        User user = new User();
        user.setId(1L);
        user.setUserId("tester123");
        user.setUsername("test_user");
        user.setContentsLikeList(contentsLikeList);

        long boardId = 1L;
        Board board = new Board();
        board.setId(boardId);
        board.setContents("Test Board");
        board.setUser(user);

        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(contentsLikeRepository.existsByUser_IdAndLikeContentsAndContents(1L, LikeContents.BOARD, boardId)).thenReturn(false);
        when(jwt.getTokenUser(servletRequest)).thenReturn(user);
        // when // then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            boardService.getBoardNolike(servletRequest, boardId);
        });

        assertEquals("좋아요를 안눌렀습니다", exception.getMessage());
        System.out.println("메세지 :" + exception.getMessage());
    }

    @Test
    @DisplayName("게시판 좋아요 취소 실패 테스트")
    public void getBoardNolikeFailtwo() {
        // given
        long boardId = 1L;
        User user = new User();
        user.setId(1L);
        user.setUserId("tester123");
        user.setUsername("test_user");

        // when // then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            boardService.getBoardNolike(servletRequest, boardId);
        });

        assertEquals("1번의 개시판은 없습니다", exception.getMessage());
        System.out.println("메세지 :" + exception.getMessage());

    }

    @Test
    @DisplayName("게시판 삭제 테스트")
    public void deleteBoard() {
        // given
        SignUpRequestDto requestDto = new SignUpRequestDto();
        requestDto.setUsername("test_user");

        User user = new User(requestDto);
        user.setId(1L);

        when(jwt.getTokenUser(servletRequest)).thenReturn(user);

        BoardRequestDto boardRequestDto = new BoardRequestDto();
        boardRequestDto.setId(1L);

        Board mockBoard = new Board();
        mockBoard.setId(1L);
        mockBoard.setUser(user);
        when(boardRepository.findById(1L)).thenReturn(Optional.of(mockBoard));

        // when
        String result = boardService.deleteBoard(servletRequest, boardRequestDto);

        // then
        assertEquals("삭제 완료", result);
        verify(boardRepository, times(1)).delete(mockBoard);
        System.out.println("메세지 :" + result);
    }

    @Test
    @DisplayName("게시판 삭제 실패 - 게시판이 존재하지 않는 경우")
    void deleteBoardFail() {
        // given
        long nonExistingBoardId = 999L;
        BoardRequestDto boardRequestDto = new BoardRequestDto();
        boardRequestDto.setId(nonExistingBoardId);

        when(boardRepository.findById(nonExistingBoardId)).thenReturn(Optional.empty());

        // when
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            boardService.deleteBoard(servletRequest, boardRequestDto);
        });

        // then
        assertEquals("사용자의 개시물이 없습니다.", exception.getMessage());
        verify(boardRepository, never()).delete(any());
    }

    @Test
    @DisplayName("개시판 업데이트 태스트")
    void updateBoard() {
        // given
        SignUpRequestDto requestDto = new SignUpRequestDto();
        requestDto.setUsername("test_user");

        User user = new User(requestDto);
        user.setId(1L);

        when(jwt.getTokenUser(servletRequest)).thenReturn(user);

        BoardRequestDto boardRequestDto = new BoardRequestDto();
        boardRequestDto.setId(1L);

        Board mockBoard = new Board();
        mockBoard.setId(1L);
        mockBoard.setUser(user);
        when(boardRepository.findById(1L)).thenReturn(Optional.of(mockBoard));

        // when
        String result = boardService.updateBoard(servletRequest, boardRequestDto);

        // then
        assertEquals("수정완료", result);
        System.out.println("메세지 :" + result);
    }

    /*@Test
    @DisplayName("개시판 + 미디어 업데이트 태스트")
    void updateMBoard() throws JsonProcessingException {
        // given
        User user = new User();
        user.setId(1L);
        Board board = new Board();
        board.setId(1L);
        board.setUser_id(1L);

        Multimedia multimedia = new Multimedia();
        multimedia.setBoard(board);
        multimedia.setId(1L);

        when(jwt.getTokenUser(servletRequest)).thenReturn(user);
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
        when(multimediaRepository.findById(1L)).thenReturn(Optional.of(multimedia));
        String boardJson = "{\"id\":1,\"user_id\":1,\"content\":\"Updated content\"}";
        BoardRequestDto boardRequestDto = new BoardRequestDto();
        boardRequestDto.setId(1L);
        boardRequestDto.setUser_id(1L);
        boardRequestDto.setContents("Updated content");

        when(boardService.getStringBoard(boardJson)).thenReturn(boardRequestDto);
        when(multimediaRepository.findById(1L)).thenReturn(Optional.of(multimedia));

        MultipartFile imageFile = new MockMultipartFile("image", "image.jpg", "image/jpeg", "image content".getBytes());
        MultipartFile movieFile = new MockMultipartFile("movie", "movie.mp4", "video/mp4", "movie content".getBytes());

        // when
        String result = boardService.updateMBoard(servletRequest, imageFile, movieFile, boardJson);

        // then
        assertEquals("수정 완료", result);
        verify(multimediaRepository, times(1)).save(any(Multimedia.class));
    }*/


    private static void ResultMessage(BoardResponseDto resultDto) {
        System.out.println("게시판 내용:");
        System.out.println("ID: " + resultDto.getBoardId());
        System.out.println("사용자 ID: " + resultDto.getBoardUserId());
        System.out.println("내용: " + resultDto.getBoardContents());
        System.out.println("좋아요 수: " + resultDto.getLikecounts());
    }
}