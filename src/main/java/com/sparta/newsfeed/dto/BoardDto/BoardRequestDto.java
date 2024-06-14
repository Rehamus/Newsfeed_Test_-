package com.sparta.newsfeed.dto.BoardDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardRequestDto {

    private Long id;
    private Long user_id;
    private String contents;

}
