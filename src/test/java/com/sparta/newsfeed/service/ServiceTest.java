package com.sparta.newsfeed.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.newsfeed.dto.BoardDto.BoardRequestDto;
import com.sparta.newsfeed.dto.BoardDto.BoardResponseDto;
import com.sparta.newsfeed.dto.CommentDto.CommentRequestDto;
import com.sparta.newsfeed.dto.CommentDto.CommentResponseDto;
import com.sparta.newsfeed.dto.EmailDto.EmailRequestDto;
import com.sparta.newsfeed.dto.EmailDto.ReVerifyEMailRequestDto;
import com.sparta.newsfeed.dto.FollowDto.FollowRequestDto;
import com.sparta.newsfeed.dto.FollowDto.FollowResponseDto;
import com.sparta.newsfeed.dto.FollowDto.FollowStatusResponseDto;
import com.sparta.newsfeed.dto.UserDto.LoginUpRequestDto;
import com.sparta.newsfeed.dto.UserDto.SignUpRequestDto;
import com.sparta.newsfeed.dto.UserDto.UserRequestDto;
import com.sparta.newsfeed.dto.UserDto.UserResponseDto;
import com.sparta.newsfeed.entity.Board;
import com.sparta.newsfeed.entity.EmailVerification;
import com.sparta.newsfeed.jwt.util.JwtTokenProvider;
import com.sparta.newsfeed.repository.EmailVerificationRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ServiceTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    SignUpService signUpService;
    @Autowired
    UserService userService;
    @Autowired
    private BoardService boardService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private FollowService followService;
    @Autowired
    private FeedService feedService;
    @Autowired
    private CommentService commentService;


    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    private MockHttpServletResponse response = new MockHttpServletResponse();
    private MockHttpServletRequest request = new MockHttpServletRequest();

    @RepeatedTest(value = 2, name = "개시판 생성 {currentRepetition}/{totalRepetitions}")
    @Order(1)
    @DisplayName("회원가입")
    void test1(RepetitionInfo repetitionInfo) {
        // given
        SignUpRequestDto requestDto = new SignUpRequestDto();
        if (repetitionInfo.getCurrentRepetition() == 1) {
            requestDto.setUserId("chokyuseong12121212");
            requestDto.setEmail("nuher038@gmail.com");
            requestDto.setUsername("한정운");
        }else{
            requestDto.setUserId("chokyuseong343433434");
            requestDto.setEmail("nuher@gmail.com");
            requestDto.setUsername("삼정운");
        }
        requestDto.setPassword("Password1234!");


        // when
        String addUser = signUpService.addUser(requestDto);

        // then
        assertEquals(requestDto.getEmail() + " 로 발송된 인증코드를 확인해주세요.", addUser);
        System.out.println("메시지: " + addUser);

    }

    @Test
    @Order(2)
    @DisplayName("이매일 재발송")
    void test2() {
        // given
        ReVerifyEMailRequestDto requestDto = new ReVerifyEMailRequestDto();
        requestDto.setEmail("nuher038@gmail.com");
        requestDto.setPassword("Password1234!");

        // when
        String reverifyEmail = signUpService.reverifyEmail(requestDto);

        // then
        assertEquals("nuher038@gmail.com로 발송한 인증 코드를 확인해 주세요.", reverifyEmail);
        System.out.println("메시지: " + reverifyEmail);

    }

    @Test
    @Order(3)
    @DisplayName("이매일 인증 완료")
    void test3() {
        // given
        String email = "nuher038@gmail.com";
        EmailRequestDto requestDto = new EmailRequestDto();
        requestDto.setEmail(email);
        EmailVerification emailv = emailVerificationRepository.findById(1L).get();
        requestDto.setCode(emailv.getCode());


        // when
        String verifyEmail = signUpService.verifyEmail(requestDto);

        // then
        assertEquals("이메일 : " + requestDto.getEmail() + " 님의 인증이 완료되었습니다.", verifyEmail);
        System.out.println("메시지: " + verifyEmail);
    }

    @Test
    @Order(4)
    @DisplayName("로그인")
    void test4() {
        // given
        LoginUpRequestDto requestDto = new LoginUpRequestDto();
        requestDto.setUserId("chokyuseong12121212");
        requestDto.setPassword("Password1234!");

        // when
        String loginUser = signUpService.loginUser(requestDto, response);


        // then
        assertEquals("어서오세요 한정운님 로그인이 완료되었습니다", loginUser);
        makecookie();
    }

    @Test
    @Order(5)
    @DisplayName("로그아웃")
    void test5() {
        //given
        makecookie();

        // when
        String logoutUser = signUpService.logoutUser(request, response);

        // then
        assertEquals("로그아웃 완료", logoutUser);
    }

    @Test
    @Order(6)
    @DisplayName("다시 로그인")
    void test6() {
        // given
        LoginUpRequestDto requestDto = new LoginUpRequestDto();
        requestDto.setUserId("chokyuseong12121212");
        requestDto.setPassword("Password1234!");

        // when
        String loginUser = signUpService.loginUser(requestDto, response);

        // then
        assertEquals("어서오세요 한정운님 로그인이 완료되었습니다", loginUser);
        makecookie();

    }


    @Test
    @Order(7)
    @DisplayName("프로필 확인")
    void test7() {
        // given
        makecookie();

        // when
        UserResponseDto getUserProfile = userService.getUserProfile(request);

        // then
        assertEquals("한정운", getUserProfile.getUsername());
        assertEquals("nuher038@gmail.com", getUserProfile.getEmail());
    }

    @Test
    @Order(8)
    @DisplayName("프로필 수정")
    void test8() {
        // given
        makecookie();
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setUsername("투정운");
        requestDto.setPassword("Password1234!");
        requestDto.setNewpassword("Password123456!");


        // when
        String updateUserProfile = userService.updateUserProfile(request, requestDto);

        // then
        assertEquals("수정완료 'Get' 으로 확인해 주세요", updateUserProfile);
    }

    @Test
    @Order(9)
    @DisplayName("프로필 사진 올리기")
    void test9() throws IOException {
        // given
        makecookie();
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setUsername("투정운");
        requestDto.setPassword("Password1234!");
        requestDto.setNewpassword("Password123456!");

        MockMultipartFile File = new MockMultipartFile("image", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy_image_data".getBytes());


        // when
        String updateUserProfile = userService.PictureUserProfile(request, File);

        // then
        assertEquals("프로필 사진 삽입완료", updateUserProfile);

    }
    @RepeatedTest(value = 3, name = "개시판 생성 {currentRepetition}/{totalRepetitions}")
    @Order(10)
    @DisplayName("개시판 생성")
    void test10() {
        // given
        makecookie();
        BoardRequestDto boardRequestDto = new BoardRequestDto();
        boardRequestDto.setContents("개시판 생성");

        // when
        String createBoard = boardService.createBoard(request,boardRequestDto);

        // then
        assertEquals(boardRequestDto.getContents()+" 생성 완료", createBoard);
    }

    @Test
    @Order(11)
    @DisplayName("개시판 수정")
    void test11() {
        // given
        makecookie();
        BoardRequestDto boardRequestDto = new BoardRequestDto();
        boardRequestDto.setId(1L);
        boardRequestDto.setContents("개시판 수정");

        // when
        String updateBoard = boardService.updateBoard(request,boardRequestDto);

        // then
        assertEquals("수정완료", updateBoard);
    }

    @Test
    @Order(12)
    @DisplayName("개시판 좋아요")
    void test12() {
        // given
        makecookie();
        Long boardId = 1L;
        BoardRequestDto boardRequestDto = new BoardRequestDto();
        boardRequestDto.setId(1L);
        boardRequestDto.setContents("개시판 수정");

        // when
        BoardResponseDto getBoardLike = boardService.getBoardLike(request, boardId);

        // then
        assertEquals(boardId, getBoardLike.getBoardId());
        assertEquals(boardId, getBoardLike.getLikecounts());
    }

    @Test
    @Order(13)
    @DisplayName("미디어 개시판 생성")
    void test13() throws JsonProcessingException {
        // given
        makecookie();
        Board board = new Board();
        String boardJson = objectMapper.writeValueAsString(board);

        MockMultipartFile File = new MockMultipartFile("image", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy_image_data".getBytes());
        // when
        String createMBoard = boardService.createMBoard(request, File, null, boardJson);

        // then
        assertEquals("생성 완료", createMBoard);

    }

    @Test
    @Order(14)
    @DisplayName("개시판 전체 조회")
    void test14()  {
        // given
        makecookie();
        int page = 0;
        int view = 1;
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        LocalDateTime end = LocalDateTime.now();
        // when
        Page<BoardResponseDto> getAllBoard = boardService.getAllBoard(request, page, view, start, end);

        // then
        BoardResponseDto responseDto = getAllBoard.getContent().get(0);
        assertEquals(1L, responseDto.getBoardId());

    }

    @Test
    @Order(15)
    @DisplayName("개시판 특정 조회")
    void test15()  {
        // given
        makecookie();

        // when
        BoardResponseDto getBoard = boardService.getBoard(1L);

        // then
        assertEquals(1L, getBoard.getBoardId());

    }

    @Test
    @Order(16)
    @DisplayName("개시판 좋아요 취소")
    void test16() {
        // given
        makecookie();
        Long boardId = 1L;

        // when
        BoardResponseDto getBoardNolike = boardService.getBoardNolike(request, boardId);

        // then
        assertEquals(boardId, getBoardNolike.getBoardId());
        assertEquals(0L, getBoardNolike.getLikecounts());
    }

    @Test
    @Order(17)
    @DisplayName("개시판 삭제")
    void test17() {
        // given
        makecookie();
        Long boardId = 1L;
        BoardRequestDto boardRequestDto = new BoardRequestDto();
        boardRequestDto.setId(1L);

        // when
        String deleteBoard = boardService.deleteBoard(request, boardRequestDto);

        // then
        assertEquals("삭제 완료", deleteBoard);
    }

    @Test
    @Order(18)
    @DisplayName("팔로우")
    void test18() {
        // given
        makecookie();
        Long boardId = 2L;
        FollowRequestDto followRequestDto = new FollowRequestDto();
        followRequestDto.setFolloweeId(2L);

        // when
        FollowResponseDto followUser = followService.followUser(followRequestDto, request);

        // then
        assertEquals("삼정운을(를) 팔로우했습니다.", followUser.getMessage());
    }

    @Test
    @Order(19)
    @DisplayName("언팔로우")
    void test19() {
        // given
        makecookie();
        Long boardId = 2L;
        FollowRequestDto followRequestDto = new FollowRequestDto();
        followRequestDto.setFolloweeId(2L);

        // when
        FollowResponseDto unfollowUser = followService.unfollowUser(followRequestDto, request);

        // then
        assertEquals("삼정운을(를) 언팔로우했습니다.", unfollowUser.getMessage());
    }

    @Test
    @Order(20)
    @DisplayName("팔로우 체크")
    void test20() {
        // given
        makecookie();
        Long boardId = 2L;
        FollowRequestDto followRequestDto = new FollowRequestDto();
        followRequestDto.setFolloweeId(2L);

        // when
        FollowStatusResponseDto checkFollowStatus = followService.checkFollowStatus(2L, request);

        // then
        assertEquals("투정운은(는) 삼정운을(를) 팔로우하고 있지 않습니다.", checkFollowStatus.getStatusMessage());
    }

    @RepeatedTest(value = 3, name = "개시판 생성 {currentRepetition}/{totalRepetitions}")
    @Order(21)
    @DisplayName("댓글 생성")
    void test21() {
        // given
        makecookie();
        Long boardId = 2L;
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setContents("댓글 내용");

        // when
        String createComment = commentService.createComment(request,2L,commentRequestDto);

        // then
        assertEquals("개시판 ::개시판 생성의\n" +
                             " 댓글 내용라는 댓글이 입력되었습니다.", createComment);
    }

    @Test
    @Order(22)
    @DisplayName("댓글 개시판 전체 조회")
    void test22() {
        // given
        makecookie();

        // when
        List<CommentResponseDto> boardComment = commentService.boardComment( 2L);

        // then
        CommentResponseDto Comment = boardComment.get(0);
        assertEquals(1L, Comment.getId());
    }

    @Test
    @Order(23)
    @DisplayName("댓글 개시판 특정 조회")
    void test23() {
        // given
        makecookie();

        // when
        CommentResponseDto boardCommentView = commentService.boardCommentView( 2L,1L);

        // then
        assertEquals(1L, boardCommentView.getId());
    }

    @Test
    @Order(24)
    @DisplayName("댓글 개시판 특정 좋아요")
    void test24() {
        // given
        makecookie();

        // when
        CommentResponseDto boardCommentLike = commentService.boardCommentLike(request, 2L,1L);

        // then
        assertEquals(1L, boardCommentLike.getId());
        assertEquals("좋아요를 누르셨습니다.", boardCommentLike.getMessage());
    }

    @Test
    @Order(25)
    @DisplayName("댓글 개시판 특정 좋아요 취소")
    void test25() {
        // given
        makecookie();

        // when
        CommentResponseDto boardCommentNolike = commentService.boardCommentNolike(request, 2L,1L);

        // then
        assertEquals(1L, boardCommentNolike.getId());
        assertEquals("좋아요가 취소되었습니다.", boardCommentNolike.getMessage());
    }

    @Test
    @Order(26)
    @DisplayName("댓글 업데이트")
    void test26() {
        // given
        makecookie();
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setContents("업데이트 댓글");
        // when
        String updateComment = commentService.updateComment(request, 2L,1L,commentRequestDto);

        // then
        assertEquals("변경 전 :댓글 내용\n변경 후 :" + commentRequestDto.getContents(), updateComment);
    }

    @Test
    @Order(27)
    @DisplayName("댓글 삭제")
    void test27() {
        // given
        makecookie();
        // when
        String delete = commentService.delete(request,  1L);

        // then
        assertEquals("댓글 삭제 완료", delete);
    }

    @Test
    @Order(28)
    @DisplayName("회원탈퇴")
    void test28() {
        // given
        makecookie();
        LoginUpRequestDto loginUpRequestDto = new LoginUpRequestDto();
        loginUpRequestDto.setUserId("chokyuseong12121212");
        loginUpRequestDto.setPassword("Password123456!");

        // when
        String deleteUser = signUpService.deleteUser(loginUpRequestDto,request,  response);

        // then
        assertEquals("회원탈퇴가 완료되었습니다 투정운님\n" +
                             " 안녕을 기원합니다.", deleteUser);
    }

    @Test
    @Order(29)
    @DisplayName("회원 탈퇴 후 로그인")
    void test29() {
        // given
        LoginUpRequestDto requestDto = new LoginUpRequestDto();
        requestDto.setUserId("chokyuseong12121212");
        requestDto.setPassword("Password123456!");

        // when
        IllegalArgumentException loginUser = assertThrows(IllegalArgumentException.class, () -> {
            signUpService.loginUser(requestDto, response);
        });
        // then
        assertEquals("이미 탈퇴한 사용자 입니다.", loginUser.getMessage());
    }



    private void makecookie() {
        String accessToken = jwtTokenProvider.generateToken("chokyuseong12121212");
        Cookie cookie = new Cookie("AccessToken", accessToken);
        request.setCookies(cookie);
    }
}
