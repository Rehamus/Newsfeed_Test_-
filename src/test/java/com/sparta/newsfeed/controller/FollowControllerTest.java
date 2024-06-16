package com.sparta.newsfeed.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.newsfeed.config.WebSecurityConfig;
import com.sparta.newsfeed.dto.BoardDto.BoardResponseDto;
import com.sparta.newsfeed.dto.FollowDto.FollowRequestDto;
import com.sparta.newsfeed.dto.FollowDto.FollowResponseDto;
import com.sparta.newsfeed.dto.FollowDto.FollowStatusResponseDto;
import com.sparta.newsfeed.entity.Board;
import com.sparta.newsfeed.entity.Users.User;
import com.sparta.newsfeed.filter.TestMockFilter;
import com.sparta.newsfeed.service.FollowService;
import com.sparta.newsfeed.service.SignUpService;
import com.sparta.newsfeed.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@WebMvcTest(
        controllers = {FollowController.class},
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = WebSecurityConfig.class
                )
        }
)
@DisplayName("팔로우 태스트")
class FollowControllerTest {

    @MockBean
    private FollowService followService;

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
    @DisplayName(" 팔로우 하기 태스트 ")
    void followUser() throws Exception {
        // given
        Long followeeId = 1L;
        FollowRequestDto requestDto = new FollowRequestDto(1L);
        FollowResponseDto followResponseDto = new FollowResponseDto();
        followResponseDto.setMessage("팔로우 되었습니다");
        // when
        when(followService.followUser(any(), any(HttpServletRequest.class))).thenReturn(followResponseDto);

        // then
        mockMvc.perform(post("/api/follow/{followeeId}",followeeId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("팔로우 되었습니다"))
                .andDo(print());
    }

    @Test
    @DisplayName("언팔로우 하기 태스트")
    void unfollowUser() throws Exception {
        // given
        Long followeeId = 1L;
        FollowRequestDto requestDto = new FollowRequestDto(1L);
        FollowResponseDto followResponseDto = new FollowResponseDto();
        followResponseDto.setMessage("언팔로우 되었습니다");
        // when
        when(followService.unfollowUser(any(), any(HttpServletRequest.class))).thenReturn(followResponseDto);

        // then
        mockMvc.perform(delete("/api/follow/{followeeId}",followeeId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("언팔로우 되었습니다"))

                .andDo(print());
    }

    @Test
    @DisplayName("팔로우 상태 태스트")
    void checkFollowStatus() throws Exception {
        // given
        Long followeeId = 1L;
        FollowRequestDto requestDto = new FollowRequestDto(1L);
        FollowStatusResponseDto followResponseDto = new FollowStatusResponseDto();
        followResponseDto.setStatusMessage("false");

        // when
        when(followService.checkFollowStatus(any(), any(HttpServletRequest.class))).thenReturn(followResponseDto);

        // then
        mockMvc.perform(get("/api/follow/status/{followeeId}",followeeId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusMessage").value("false"))
                .andDo(print());
    }

    private static User getUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("사용자1");
        return user;
    }
}