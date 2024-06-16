package com.sparta.newsfeed.dto.EmailDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EmailRequestDto {

    @Email
    @NotBlank(message = "email은 비워둘 수 없습니다")
    private String email;

    @NotBlank(message = "code 비워둘 수 없습니다")
    private String code;
}