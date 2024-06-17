package com.sparta.newsfeed.service;

import com.sparta.newsfeed.dto.UserDto.UserRequestDto;
import com.sparta.newsfeed.dto.UserDto.UserResponseDto;
import com.sparta.newsfeed.entity.Users.User;
import com.sparta.newsfeed.jwt.util.JwtTokenProvider;
import com.sparta.newsfeed.repository.MultimediaRepository;
import com.sparta.newsfeed.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private MultimediaRepository multimediaRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("유저 프로필 가져오기 테스트")
    public void testGetUserProfile() {
        // given
        User user = new User();
        user.setUsername("testUser");
        when(jwtTokenProvider.getTokenUser(request)).thenReturn(user);

        // when
        UserResponseDto userProfile = userService.getUserProfile(request);

        // then
        assertEquals(user.getUsername(), userProfile.getUsername());
    }

    @Test
    @DisplayName("유저 프로필 수정 - 비밀번호 변경")
    public void testUpdateUserProfile_PasswordChange() {
        // given
        User user = new User();
        user.setUsername("testUser");
        when(jwtTokenProvider.getTokenUser(request)).thenReturn(user);

        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setPassword("Password123!");
        requestDto.setNewpassword("newPassword123!");

        when(passwordEncoder.matches(requestDto.getPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.matches(requestDto.getNewpassword(), user.getPassword())).thenReturn(false);
        when(passwordEncoder.encode(requestDto.getNewpassword())).thenReturn("encodedNewPassword");

        // when
        String result = userService.updateUserProfile(request, requestDto);

        // then
        assertEquals("수정완료 'Get' 으로 확인해 주세요", result);
        assertEquals("encodedNewPassword", user.getPassword());
    }

    @Test
    @DisplayName("프로필 사진 올리기 테스트 - 이미지 업로드 성공")
    public void testPictureUserProfile_UploadImageSuccess() throws IOException {
        // given
        User user = new User();
        user.setUsername("testUser");
        when(jwtTokenProvider.getTokenUser(request)).thenReturn(user);

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(multipartFile.getBytes()).thenReturn(new byte[10]);

        String expectedKey = "Pictur/" + UUID.randomUUID().toString();
        String expectedUrl = "https://onebytenewsfeed.s3.amazonaws.com/" + expectedKey;

        PutObjectResponse putObjectResponse = PutObjectResponse.builder().build();
        when(s3Client.putObject(any(PutObjectRequest.class), (Path) any())).thenReturn(putObjectResponse);

        // when
        String result = userService.PictureUserProfile(request, multipartFile);

        // then
        assertEquals("프로필 사진 삽입완료", result);
        assertEquals(expectedUrl, user.getPicturUrl());
    }

}
