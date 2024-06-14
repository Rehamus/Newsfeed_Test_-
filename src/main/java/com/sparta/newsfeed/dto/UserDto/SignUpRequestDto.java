package com.sparta.newsfeed.dto.UserDto;


import com.sparta.newsfeed.entity.Users.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class SignUpRequestDto {

    //아이디
    @NotBlank(message = "userId는 비워둘 수 없습니다.")
    @Size(min = 10, max = 20, message = "userId 는 최소 10글자 이상, 최대 20글자 이하여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "사용자 ID는 대소문자 포함 영문과 숫자만 허용됩니다.")
    private String userId;

    // 비밀번호
    @NotBlank(message = " password는 비워둘 수 없습니다.")
    @Size(min = 10, message = "password는 최소 10글자 이상이어야 합니다.")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[~!@#$%^&*()_+])[A-Za-z\\d~!@#$%^&*()_+]{10,}$"
            , message = "password는 대소문자 포함 영문, 숫자, 특수문자를 최소 1글자씩 포함해야 합니다.")
    private String password;

    //이름
    @NotBlank(message = " username 비워둘 수 없습니다.")
    @Pattern(regexp = "^[\\s\\S]*$", message = "username 공백을 포함할 수 없습니다.")
    private String username = "username";

    //이매일
    @Email
    @NotBlank(message = " email 비워둘 수 없습니다.")
    private String email;

    //한줄 소개
    @NotBlank(message = " one_liner 비워둘 수 없습니다.")
    private String one_liner = "one_liner ";

    //유저 상태 코드
    private final UserStatus userStatus = UserStatus.ACTIVE;
}