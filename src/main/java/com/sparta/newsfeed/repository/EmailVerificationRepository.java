package com.sparta.newsfeed.repository;

import com.sparta.newsfeed.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByEmail(String email);
    Optional<String> findCodeByEmail(String email);
    Optional<EmailVerification> findByEmailAndCode(String email, String code);
}