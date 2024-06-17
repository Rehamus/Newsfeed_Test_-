package com.sparta.newsfeed.service;

import com.sparta.newsfeed.dto.EmailDto.EmailRequestDto;
import com.sparta.newsfeed.dto.EmailDto.ReVerifyEMailRequestDto;
import com.sparta.newsfeed.dto.UserDto.LoginUpRequestDto;
import com.sparta.newsfeed.dto.UserDto.SignUpRequestDto;
import com.sparta.newsfeed.entity.EmailVerification;
import com.sparta.newsfeed.repository.EmailVerificationRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceTest {

    @Autowired
    SignUpService signUpService;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Test
    @Order(1)
    @DisplayName("회원가입")
    void test1() {
        // given
        SignUpRequestDto requestDto = new SignUpRequestDto();
        requestDto.setUserId("chokyuseong12121212");
        requestDto.setPassword("Password1234");
        requestDto.setUsername("한정운");
        requestDto.setEmail("nuher038@gmail.com");

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
        requestDto.setPassword("Password1234");
        requestDto.setEmail("nuher038@gmail.com");

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
        requestDto.setPassword("Password1234");

        HttpServletResponse HttpServletResponse = null;

        // when
        String loginUser = signUpService.loginUser(requestDto, HttpServletResponse);

        // then
        assertEquals("어서오세요 한정운님 로그인이 완료되었습니다", loginUser);
    }
/*
    @Test
    @Order(2)
    @DisplayName("회원가입")
    void test2() {

    }

    @Test
    @Order(2)
    @DisplayName("회원가입")
    void test2() {

    }

    @Test
    @Order(2)
    @DisplayName("회원가입")
    void test2() {

    }

    @Test
    @Order(2)
    @DisplayName("회원가입")
    void test2() {

    }

    @Test
    @Order(2)
    @DisplayName("회원가입")
    void test2() {

    }

    @Test
    @Order(2)
    @DisplayName("회원가입")
    void test2() {

    }

    @Test
    @Order(2)
    @DisplayName("회원가입")
    void test2() {

    }

    @Test
    @Order(2)
    @DisplayName("회원가입")
    void test2() {

    }

    @Test
    @Order(2)
    @DisplayName("회원가입")
    void test2() {

    }

    @Test
    @Order(2)
    @DisplayName("회원가입")
    void test2() {

    }*/



}
