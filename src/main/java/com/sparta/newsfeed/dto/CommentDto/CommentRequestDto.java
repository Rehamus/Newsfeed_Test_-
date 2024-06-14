package com.sparta.newsfeed.dto.CommentDto;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CommentRequestDto {

    private Long id;
    private Long user_id;
    private String contents;

}
