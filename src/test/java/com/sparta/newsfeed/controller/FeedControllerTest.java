package com.sparta.newsfeed.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.newsfeed.config.WebSecurityConfig;
import com.sparta.newsfeed.dto.BoardDto.BoardResponseDto;
import com.sparta.newsfeed.dto.UserDto.SignUpRequestDto;
import com.sparta.newsfeed.entity.Board;
import com.sparta.newsfeed.entity.Users.User;
import com.sparta.newsfeed.filter.TestMockFilter;
import com.sparta.newsfeed.service.FeedService;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(
        controllers = {FeedController.class},
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = WebSecurityConfig.class
                )
        }
)
@DisplayName("피드 데스트")
class FeedControllerTest {

    @MockBean
    private FeedService feedService;

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
    @DisplayName("피드 해둔 개시물 데스트")
    void getFeed() throws Exception {
        // given
        Board board = getBoard(getUser());
        List<BoardResponseDto> boardResponseDtoList = new ArrayList<>();
        BoardResponseDto responseDto = new BoardResponseDto(board);
        boardResponseDtoList.add(responseDto);

        // when
        when(feedService.getFeed(any(HttpServletRequest.class))).thenReturn(boardResponseDtoList);

        // then
        mockMvc.perform(get("/api/feed")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(boardResponseDtoList)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].boardId").value(1L))
                .andExpect(jsonPath("$[0].boardUserId").value(1L))
                .andExpect(jsonPath("$[0].boardContents").value("게시글 내용"))
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