package com.sparta.newsfeed.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("이매일 태스트")
class EmailServiceTest {

    @Mock
    JavaMailSender mailSender;
    @Mock
    EmailService emailService;

    @Test
    @DisplayName("이메일 발송 태스트")
    void sendEmail() {
        // given
        mailSender = Mockito.mock(JavaMailSender.class);
        emailService = new EmailService(mailSender);

        String to = "test@example.com";
        String subject = "Test Subject";
        String text = "Test Text";

        MimeMessage mimeMessage = Mockito.mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // when // then
        assertDoesNotThrow(() -> emailService.sendEmail(to, subject, text));

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}