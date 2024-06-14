package com.sparta.newsfeed.entity;

import com.sparta.newsfeed.NewsFeedApplication;
import com.sparta.newsfeed.dto.BoardDto.BoardRequestDto;
import com.sparta.newsfeed.entity.Users.User;
import com.sparta.newsfeed.repository.BoardRepository;
import com.sparta.newsfeed.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = NewsFeedApplication.class)
@DisplayName("개시판 태스트")
public class BoardEntityTest {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Comment comment;
    private BoardRequestDto boardRequestDto;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setUsername("login1234");
        user.setPassword("password123");

        userRepository.save(user);

        boardRequestDto = new BoardRequestDto();
        boardRequestDto.setContents("Test contents");
    }

    @Test
    @DisplayName("개시판 생성 성공 태스트")
    public void testCreateBoard() {
        // given
        Board board = getBoard();

        // when
        Board Boards = boardRepository.save(board);

        // then
        assertNotNull(Boards.getId());
        assertEquals(Boards.getContents(), boardRequestDto.getContents());
        assertEquals(Boards.getUser().getId(), user.getId());
    }


    @Test
    @DisplayName("개시판 찾기 태스트")
    public void testFindBoard() {
        // given
        Board board = getBoard();
        Board Boards = boardRepository.save(board);

        // when
        Optional<Board> foundBoard = boardRepository.findById(Boards.getId());


        // then
        assertTrue(foundBoard.isPresent());
        assertEquals(foundBoard.get().getContents(), boardRequestDto.getContents());
    }

    @Test
    @DisplayName("개시판 찾기 실패 태스트")
    public void testFindFailBoard() {
        // given
        Board board = getBoard();
        Board Boards = boardRepository.save(board);

        // when
        Optional<Board> foundBoard = boardRepository.findById(2L);


        // then
        assertFalse(foundBoard.isPresent());
    }



    @Test
    @DisplayName("개시판 업데이트 태스트")
    public void testUpdateBoard() {
        // given
        Board board = getBoard();
        Board Boards = boardRepository.save(board);
        BoardRequestDto updatedBoardRequestDto = new BoardRequestDto();
        updatedBoardRequestDto.setContents("업데이트 테스트");

        // when
        Boards.update(updatedBoardRequestDto);
        Board updatedBoard = boardRepository.save(Boards);

        // then
        assertEquals(updatedBoard.getContents(), "업데이트 테스트");
    }

    @Test
    @DisplayName("개시판 삭제 태스트")
    public void testDeleteBoard() {
        // given
        Board board = getBoard();
        Board Boards = boardRepository.save(board);

        // when
        boardRepository.delete(Boards);
        Optional<Board> deletedBoard = boardRepository.findById(Boards.getId());

        // then
        assertFalse(deletedBoard.isPresent());
    }

    @Test
    @DisplayName("개시판 좋아요 태스트")
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
    @DisplayName("개시판 관계 태스트")
    public void testUserRelationship() {
        // given
        Board board = getBoard();

        List<Comment> comments = new ArrayList<>();
        Comment comment = new Comment();
        comments.add(comment);
        board.setCommentList(comments);

        // when
        Board savedBoard = boardRepository.save(board);

        // then
        assertNotNull(savedBoard.getUser_id());
        assertEquals(savedBoard.getUser().getId(), user.getId());
        assertEquals(savedBoard.getCommentList().get(0).getId(), comment.getId());
    }

    // given // 태스트용 박스
    private Board getBoard() {
        Board board = new Board(user, boardRequestDto);
        board.setUser(user);
        return board;
    }

}
