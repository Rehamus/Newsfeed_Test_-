package com.sparta.newsfeed.dto.EmailDto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReVerifyEMailRequestDto {
    private String email;
    private String password;
}