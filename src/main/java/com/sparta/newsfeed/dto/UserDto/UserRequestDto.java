package com.sparta.newsfeed.dto.UserDto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserRequestDto {

    private String username;
    private String email;
    private String one_liner;
    private String password;
    private String newpassword;

}
