package com.sparta.newsfeed.aop;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Objects;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ControllerAOP {

    @Pointcut("execution(* com.sparta.newsfeed.controller..*(..))")
    public void Controller() {
    }

    @Before("Controller()")
    public void logBeforeMethodExecution(JoinPoint joinPoint) {
        HttpServletRequest request = ((ServletRequestAttributes)
                        Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                        .getRequest();
        String requestUrl = request.getRequestURL().toString();
        String requestUri = request.getRequestURI();
        String methodMapping = request.getMethod();

        log.info("" +
                 "\n 요청 URL: " + requestUrl +
                 "\n 요청 URI: " + requestUri +
                 "\n 요청 메서드: " + methodMapping
        );
    }

}
