package com.sparta.newsfeed.service;

import com.sparta.newsfeed.dto.EmailDto.EmailRequestDto;
import com.sparta.newsfeed.dto.EmailDto.ReVerifyEMailRequestDto;
import com.sparta.newsfeed.dto.UserDto.LoginUpRequestDto;
import com.sparta.newsfeed.dto.UserDto.SignUpRequestDto;
import com.sparta.newsfeed.entity.EmailVerification;
import com.sparta.newsfeed.entity.Users.User;
import com.sparta.newsfeed.entity.Users.UserStatus;
import com.sparta.newsfeed.jwt.util.JwtTokenProvider;
import com.sparta.newsfeed.repository.EmailVerificationRepository;
import com.sparta.newsfeed.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("회원가입 태스트")
class SignUpServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private EmailVerificationRepository emailVerificationRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private SignUpService signUpService;



    @Test
    @DisplayName("회원가입")
    void addUser() throws MessagingException {
        // given
        SignUpRequestDto requestDto = new SignUpRequestDto();
        requestDto.setUsername("username");
        requestDto.setPassword("password");
        requestDto.setEmail("nuher038@gmail.com");

        when(userRepository.findByUserId(requestDto.getUserId())).thenReturn(null);
        when(passwordEncoder.encode(requestDto.getPassword())).thenReturn("encodedPassword");

        when(emailVerificationRepository.save(any(EmailVerification.class))).thenAnswer(invocation -> invocation.getArgument(0));;
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // when
        String addUser = signUpService.addUser(requestDto);

        // then
        assertEquals(requestDto.getEmail() + " 로 발송된 인증코드를 확인해주세요.", addUser);
        System.out.println("메시지: " + addUser);
    }

    @Test
    @DisplayName("이메일 재인증")
    void reverifyEmail() throws MessagingException {
        // given
        ReVerifyEMailRequestDto requestDto = new ReVerifyEMailRequestDto();
        requestDto.setEmail("nuher038@gmail.com");
        requestDto.setPassword("password123");

        User user = new User();
        user.setEmail(requestDto.getEmail());
        user.setPassword("$2a$10$TNg1tqsb0v43yGak7Ra3uOmUzTCqCw6ReCfCq3JvRG3wEq0T.8VZa");

        String newVerificationCode = "123456";

        when(userRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(requestDto.getPassword(), user.getPassword())).thenReturn(true);
        when(emailVerificationRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.of(new EmailVerification(requestDto.getEmail(), newVerificationCode)));
        doAnswer(invocation -> {
            EmailVerification updatedVerification = invocation.getArgument(0);
            updatedVerification.setCode(newVerificationCode);
            return updatedVerification;
        }).when(emailVerificationRepository).save(any(EmailVerification.class));

        // when
        String reverifyEmail = signUpService.reverifyEmail(requestDto);

        // then
        assertEquals(requestDto.getEmail() + "로 발송한 인증 코드를 확인해 주세요.", reverifyEmail);
        System.out.println(reverifyEmail);
    }

    @Test
    @DisplayName("이매일 인증 완료")
    void verifyEmail() {
        // given
        EmailRequestDto requestDto = new EmailRequestDto();
        requestDto.setEmail("nuher038@gmail.com");
        requestDto.setCode("123456");

        User user = new User();
        user.setEmail(requestDto.getEmail());
        user.setUserStatus(UserStatus.WAIT_EMAIL);
        user.setSend_email_time(LocalDateTime.now().minusSeconds(120));

        EmailVerification emailVerification = new EmailVerification(requestDto.getEmail(), "123456");
        emailVerification.setVerified(false);

        // Mock 설정
        when(emailVerificationRepository.findByEmailAndCode(requestDto.getEmail(), requestDto.getCode())).thenReturn(Optional.of(emailVerification));
        when(userRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(emailVerificationRepository.save(any(EmailVerification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        String verifyEmail = signUpService.verifyEmail(requestDto);

        // then
        assertEquals("이메일 : " + requestDto.getEmail() + " 님의 인증이 완료되었습니다.", verifyEmail);
        assertEquals(UserStatus.ACTIVE, user.getUserStatus());
    }

    @Test
    @DisplayName("로그인 태스트")
    void loginUser() {
        // given
        LoginUpRequestDto requestDto = new LoginUpRequestDto();
        requestDto.setUserId("testuser");
        requestDto.setPassword("password123");

        User user = new User();
        user.setUserId(requestDto.getUserId());
        user.setPassword("$2a$10$TNg1tqsb0v43yGak7Ra3uOmUzTCqCw6ReCfCq3JvRG3wEq0T.8VZa");
        user.setUserStatus(UserStatus.ACTIVE);
        user.setRefresh_token("mockAccessToken");

        String accessToken = "mockAccessToken";

        // Mock 설정
        when(userRepository.findByUserId(requestDto.getUserId())).thenReturn(user);
        when(passwordEncoder.matches(requestDto.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateToken(user.getUserId())).thenReturn(accessToken);

        // when
        String result = signUpService.loginUser(requestDto, mock(HttpServletResponse.class));

        // then
        assertEquals("어서오세요 " + user.getUsername() + "님 로그인이 완료되었습니다", result);
    }


    @Test
    @DisplayName("로그아웃 태스트")
    void logoutUser() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        User user = new User();
        user.setUsername("testuser");
        user.setRefresh_token("mockRefreshToken");

        // Mock 설정
        when(jwtTokenProvider.getTokenUser(request)).thenReturn(user);

        // when
        String result = signUpService.logoutUser(request, response);

        // then
        assertEquals("로그아웃 완료", result);
        verify(userRepository, times(1)).save(user);
        assertEquals(null, user.getRefresh_token());
    }

    @Test
    @DisplayName("회원 탈퇴 태스트")
    void deleteUser() {
        // given
        LoginUpRequestDto requestDto = new LoginUpRequestDto();
        requestDto.setUserId("testuser123");
        requestDto.setPassword("password1234");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        User user = new User();
        user.setUserId(requestDto.getUserId());
        user.setPassword("$2a$10$TNg1tqsb0v43yGak7Ra3uOmUzTCqCw6ReCfCq3JvRG3wEq0T.8VZa");
        user.setUserStatus(UserStatus.ACTIVE);

        // Mock 설정
        when(jwtTokenProvider.getTokenUser(request)).thenReturn(user);
        when(passwordEncoder.matches(requestDto.getPassword(), user.getPassword())).thenReturn(true);

        // when
        String result = signUpService.deleteUser(requestDto, request, response);

        // then
        assertEquals("회원탈퇴가 완료되었습니다 " + user.getUsername() + "님\n 안녕을 기원합니다.", result);
        assertEquals(UserStatus.WITHDRAWAL, user.getUserStatus());
        verify(userRepository, times(1)).save(user);
        verify(jwtTokenProvider, times(1)).deleteCookie(response);
    }


}