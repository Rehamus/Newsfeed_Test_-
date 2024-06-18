package com.sparta.newsfeed.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.newsfeed.config.WebSecurityConfig;
import com.sparta.newsfeed.dto.EmailDto.EmailRequestDto;
import com.sparta.newsfeed.dto.EmailDto.ReVerifyEMailRequestDto;
import com.sparta.newsfeed.dto.UserDto.LoginUpRequestDto;
import com.sparta.newsfeed.dto.UserDto.SignUpRequestDto;
import com.sparta.newsfeed.dto.UserDto.UserResponseDto;
import com.sparta.newsfeed.entity.Users.User;
import com.sparta.newsfeed.filter.TestMockFilter;
import com.sparta.newsfeed.service.SignUpService;
import com.sparta.newsfeed.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {UserController.class},
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = WebSecurityConfig.class
                )
        }
)
@DisplayName("유져 테스트")
class UserControllerTest {

    @MockBean
    private UserService userService;

    @MockBean
    private SignUpService signUpService;

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
    @DisplayName("유저 생성 테스트")
    void addUser() throws Exception {
        // given
        SignUpRequestDto requestDto = new SignUpRequestDto();
        requestDto.setUserId("testUser123");
        requestDto.setPassword("Test1234!@");
        requestDto.setUsername("testUser");
        requestDto.setEmail("testuser@example.com");
        requestDto.setOne_liner("Hello, I am testUser!");

        // when
        when(signUpService.addUser(any())).thenReturn(requestDto.getEmail() + " 로 발송된 인증코드를 확인해주세요."
        );

        // then
        mockMvc.perform(post("/api/user/sign")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("testuser@example.com 로 발송된 인증코드를 확인해주세요."))
                .andDo(print());
    }

    @Test
    @DisplayName("유저 이메일 인증 테스트")
    void verifyEmail() throws Exception {
        // given
        EmailRequestDto requestDto = new EmailRequestDto();
        requestDto.setEmail("testuser@example.com");
        requestDto.setCode("12345");

        // when
        when(signUpService.verifyEmail(any())).thenReturn("이메일 인증 성공");

        // then
        mockMvc.perform(post("/api/user/verify")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("이메일 인증 성공"))
                .andDo(print());
    }

    @Test
    @DisplayName("유저 이메일 인증 재요청 테스트")
    void reverifyEmail() throws Exception {
        // given
        ReVerifyEMailRequestDto requestDto = new ReVerifyEMailRequestDto();
        requestDto.setEmail("testuser@example.com");
        requestDto.setPassword("Test1234!@");


        // when
        when(signUpService.reverifyEmail(any())).thenReturn(requestDto.getEmail() + "로 발송한 인증 코드를 확인해 주세요.");

        // then
        mockMvc.perform(post("/api/user/reverify")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string(requestDto.getEmail() + "로 발송한 인증 코드를 확인해 주세요."))
                .andDo(print());
    }

    @Test
    @DisplayName("유저 로그인 테스트")
    void loginUser() throws Exception {
        // given
        User user = getUser();
        user.setUsername("태스터");
        LoginUpRequestDto requestDto = new LoginUpRequestDto();
        requestDto.setUserId("testUser123");
        requestDto.setPassword("Test1234!@");

        // when
        when(signUpService.loginUser(any(),any())).thenReturn("어서오세요 " + user.getUsername() + "님 로그인이 완료되었습니다");

        // then
        mockMvc.perform(post("/api/user/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("어서오세요 " + user.getUsername() + "님 로그인이 완료되었습니다"))
                .andDo(print());
    }

    @Test
    @DisplayName("유저 로그아웃 테스트")
    void logoutUser() throws Exception {
        // When
        when(signUpService.logoutUser(any(HttpServletRequest.class), any(HttpServletResponse.class)))
                .thenReturn("로그아웃 완료");

        // Then
        mockMvc.perform(post("/api/user/logout")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("로그아웃 완료"))
                .andDo(print());
    }

    @Test
    @DisplayName("유저 회원 탈퇴 테스트")
    void deleteUser() throws Exception {
        // given
        User user = getUser();
        user.setUsername("태스터");
        LoginUpRequestDto requestDto = new LoginUpRequestDto();
        requestDto.setUserId("testUser123");
        requestDto.setPassword("Test1234!@");

        // when
        when(signUpService.deleteUser(any(),any(HttpServletRequest.class), any(HttpServletResponse.class)))
                .thenReturn("회원탈퇴가 완료되었습니다 " + user.getUsername() + "님\n 안녕을 기원합니다.");

        // then
        mockMvc.perform(post("/api/user/delete")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "회원탈퇴가 완료되었습니다 " + user.getUsername() + "님\n 안녕을 기원합니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("유저 프로필 보기 테스트")
    void getUserProfile() throws Exception {
        // given
        User user = getUser();
        user.setUsername("태스터");
        UserResponseDto responseDto = new UserResponseDto(user);

        // when
        when(userService.getUserProfile(any(HttpServletRequest.class)))
                .thenReturn(responseDto);

        // then
        mockMvc.perform(get("/api/user/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(responseDto)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("유저 프로필 수정 테스트")
    void updateUserProfile() throws Exception {
        // given
        User user = getUser();
        user.setUsername("태스터");
        UserResponseDto responseDto = new UserResponseDto(user);

        // when
        when(userService.updateUserProfile(any(HttpServletRequest.class),any()))
                .thenReturn("수정완료 'Get' 으로 확인해 주세요");

        // then
        mockMvc.perform(patch("/api/user/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(responseDto)))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "수정완료 'Get' 으로 확인해 주세요"))
                .andDo(print());
    }

    @Test
    @DisplayName("유저 프로필 사진 삽입 테스트")
    void pictureUserProfile() throws Exception {
        // Given
        MockMultipartFile imageFile = new MockMultipartFile("Pictur", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy_image_data".getBytes());
        // When
        when(userService.PictureUserProfile(any(HttpServletRequest.class), any()))
                .thenReturn("프로필 사진 삽입완료");

        // Then
        mockMvc.perform(multipart("/api/user/profile/m")
                                .file(imageFile)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .with(request -> {
                                    request.setMethod("PATCH");
                                    return request;
                                }))
                .andExpect(status().isOk())
                .andExpect(content().string("프로필 사진 삽입완료"))
                .andDo(print());
    }

    //:::::::::::::// 도구 상자 //:::::::::::::://


    private static User getUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("사용자1");
        return user;
    }
}