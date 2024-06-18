package com.sparta.newsfeed.entity;

import com.sparta.newsfeed.NewsFeedApplication;
import com.sparta.newsfeed.dto.BoardDto.BoardRequestDto;
import com.sparta.newsfeed.dto.BoardDto.BoardResponseDto;
import com.sparta.newsfeed.dto.CommentDto.CommentRequestDto;
import com.sparta.newsfeed.dto.CommentDto.CommentResponseDto;
import com.sparta.newsfeed.dto.UserDto.SignUpRequestDto;
import com.sparta.newsfeed.dto.UserDto.UserResponseDto;
import com.sparta.newsfeed.entity.Likes.ContentsLike;
import com.sparta.newsfeed.entity.Users.User;
import com.sparta.newsfeed.repository.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = NewsFeedApplication.class)
@DisplayName("DTO,Entity 테스트")
public class DtoEntityTest {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ContentsLikeRepository ContentsLikeRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private Validator validator;

    private User user;
    private BoardRequestDto boardRequestDto;
    private CommentRequestDto commentRequestDto;




    @BeforeEach
    public void setUp() {
        user = new User();
        user.setUsername("login1234");
        user.setPassword("password123");

        userRepository.save(user);

        boardRequestDto = new BoardRequestDto();
        boardRequestDto.setContents("Test contents");

        commentRequestDto = new CommentRequestDto();
        commentRequestDto.setContents("Test contents");

    }

    @Test
    @DisplayName("유저 회원 가입 dto 테스트")
    public void testSignUpRequestDto() {
        // given
        SignUpRequestDto dto = new SignUpRequestDto();
        dto.setUserId("login123");
        dto.setPassword("Password!123");
        dto.setUsername("username");
        dto.setEmail("user@example.com");
        dto.setOne_liner("Intro");

        // when
        User sign = new User(dto);

        // then
        assertThat(sign.getUserId()).isEqualTo("login123");
        assertThat(sign.getPassword()).isEqualTo("Password!123");
        assertThat(sign.getUsername()).isEqualTo("username");
        assertThat(sign.getEmail()).isEqualTo("user@example.com");
        assertThat(sign.getOne_liner()).isEqualTo("Intro");
    }

    @Test
    @DisplayName("유저 회원 가입 dto 테스트")
    public void testUserResponseDto() {
        // given
        SignUpRequestDto dto = new SignUpRequestDto();
        dto.setUserId("log");
        dto.setPassword("pass");
        dto.setUsername("");
        dto.setEmail("invalid-email");
        dto.setOne_liner("Intro");

        // when
        Set<ConstraintViolation<SignUpRequestDto>> violations = validator.validate(dto);

        // then
        assertFalse(violations.isEmpty());
        for (ConstraintViolation<SignUpRequestDto> violation : violations) {
            System.out.println(violation.getMessage());
        }
    }




    @Test
    @DisplayName("게시판 생성 테스트")
    public void testCreateBoard() {
        // given
        Board Boards = getBoard();

        // when
        Board createdBoard = boardRepository.findById(Boards.getId()).get();

        // then
        assertNotNull(Boards.getId());
        assertEquals(Boards.getContents(), createdBoard.getContents());
        assertEquals(Boards.getUser().getId(), createdBoard.getUser().getId());
    }


    @Test
    @DisplayName("게시판 찾기 테스트")
    public void testFindBoard() {
        // given
        Board Boards = getBoard();

        // when
        Optional<Board> foundBoard = boardRepository.findById(Boards.getId());


        // then
        assertTrue(foundBoard.isPresent());
        assertEquals(foundBoard.get().getContents(), boardRequestDto.getContents());
    }

    @Test
    @DisplayName("게시판 찾기 실패 테스트")
    public void testFindFailBoard() {
        // given
        getBoard();

        // when
        Optional<Board> foundBoard = boardRepository.findById(2L);


        // then
        assertFalse(foundBoard.isPresent());
    }


    @Test
    @Transactional
    @DisplayName("게시판 업데이트 테스트")
    public void testUpdateBoard() {
        // given
        Board Boards = getBoard();
        BoardRequestDto updatedBoardRequestDto = new BoardRequestDto();
        updatedBoardRequestDto.setContents("업데이트 테스트");

        // when
        Boards.update(updatedBoardRequestDto);

        String updatedBoard = boardRepository.findById(Boards.getId()).get().getContents();

        // then
        assertEquals(updatedBoard, "업데이트 테스트");
    }

    @Test
    @DisplayName("게시판 업데이트 실패 테스트")
    public void testUpdateFailBoard() {
        // given
        Board Boards = getBoard();
        BoardRequestDto updatedBoardRequestDto = new BoardRequestDto();
        updatedBoardRequestDto.setContents("업데이트 테스트");

        // when
        Boards.update(updatedBoardRequestDto);

        String updatedBoard = boardRepository.findById(Boards.getId()).get().getContents();

        // then
        // Transactional이 없어서 바뀌지 않아야 정상
        assertEquals(updatedBoard, "Test contents");
    }

    @Test
    @DisplayName("게시판 삭제 테스트")
    public void testDeleteBoard() {
        // given
        Board Boards = getBoard();

        // when
        boardRepository.delete(Boards);
        Optional<Board> deletedBoard = boardRepository.findById(Boards.getId());

        // then
        assertFalse(deletedBoard.isPresent());
    }

    @Test
    @DisplayName("게시판 좋아요 테스트")
    public void testSetLikecounts() {
        // given
        Board board = getBoard();
        board.setLikecounts(100L);

        // when
        Board Boards = boardRepository.save(board);

        // then
        assertEquals(Boards.getLikecounts(), 100L);
    }



    @Test
    @DisplayName("게시판 보기 테스트")
    @Transactional
    public void BoardResponseDtoTest() {
        // given
        Board Boards = getBoard();

        // when
        BoardResponseDto boardResponseDto = new BoardResponseDto(Boards,5L,"테스트 확인");

        // then
        assertEquals(boardResponseDto.getLikecounts(), 5L);
        assertEquals(boardResponseDto.getMessage(), "테스트 확인");
    }


    @Test
    @DisplayName("게시판 관계 테스트")
    @Transactional
    public void testUserRelationship() {
        // given
        Board board = getBoard();
        Comment comment = new Comment();
        commentRepository.save(comment);

        List<Comment> comments = new ArrayList<>();
        comments.add(comment);
        board.setCommentList(comments);

        // when
        Board savedBoard = boardRepository.save(board);

        // then
        assertNotNull(savedBoard.getUser_id());
        assertEquals(savedBoard.getUser().getId(), user.getId());
        assertEquals(savedBoard.getCommentList().get(0).getId(), comment.getId());
    }

    @Test
    @DisplayName("댓글 생성 테스트")
    public void CommentRequestDtoTest() {
        // given
        Board Boards = getBoard();
        Comment comment = new Comment(commentRequestDto,Boards,user);

        // when
        Comment comments = commentRepository.save(comment);

        // then
        assertNotNull(comments.getUser_id());
        assertEquals(comments.getUser_id(), user.getId());
        assertEquals(comments.getBoard_user_id(), Boards.getUser().getId());
    }

    @Test
    @DisplayName("댓글 수정 테스트")
    @Transactional
    public void CommentUpdateTest() {
        // given
        Comment comment = new Comment();
        comment.setContents("테스트 전");
        commentRepository.save(comment);

        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setContents("테스트");

        // when
        comment.update(commentRequestDto);

        // then
        assertNotNull(comment.getId());
        assertEquals(comment.getContents(), "테스트");
    }

    @Test
    @DisplayName("댓글 보기 테스트")
    @Transactional
    public void CommentResponseDtoTest() {
        // given
        Board Boards = getBoard();
        Comment comment = new Comment();
        comment.setUser(Boards.getUser());
        comment.setBoard(Boards);
        commentRepository.save(comment);

        // when
        CommentResponseDto commentResponseDto = new CommentResponseDto(comment, 5L, "테스트 확인");

        // then
        assertEquals(commentResponseDto.getLike_count(), 5L);
        assertEquals(commentResponseDto.getMessage(), "테스트 확인");
    }

    @Test
    @DisplayName("댓글 보기 실패 테스트")
    @Transactional
    public void CommentResponseDtoFailTest() {
        // given
        Comment comment = new Comment();
        commentRepository.save(comment);

        // when // then
        // null값이 나오면 정상
        NullPointerException commentResponseDto = assertThrows(NullPointerException.class, () -> {
            new CommentResponseDto(comment, 5L, "테스트 확인");
        });
    }

    @Test
    @DisplayName("게시판 좋아요 테스트")
    public void BoardContentsLikeTest() {
        // given
        Board Boards = getBoard();
        ContentsLike contentsLike = new ContentsLike(user,Boards);

        // when
        ContentsLike contentsLikeTest = ContentsLikeRepository.save(contentsLike);

        // then
        assertEquals(contentsLikeTest.getContents(), contentsLike.getContents());
    }

    @Test
    @DisplayName("댓글 좋아요 테스트")
    public void CommentContentsLikeTest() {
        // given
        Comment comment = new Comment();
        ContentsLike contentsLike = new ContentsLike(user,comment);
        ContentsLikeRepository.save(contentsLike);
        // when
        ContentsLike contentsLikeTest = ContentsLikeRepository.save(contentsLike);

        // then
        assertEquals(contentsLikeTest.getContents(), contentsLike.getContents());
    }

    //보드 가져오기
    private Board getBoard() {
        BoardRequestDto boardRequestDto = new BoardRequestDto();
        boardRequestDto.setContents("Test contents");
        Board board = new Board(user,boardRequestDto);
        board.setContents("Test contents");
        board.setUser(user);
        Board Boards = boardRepository.save(board);
        return board;
    }






}
