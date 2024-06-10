package com.sparta.newsfeed.repository;

import com.sparta.newsfeed.entity.Like_entity.ContentsLike;
import com.sparta.newsfeed.entity.User_entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentsLikeRepository extends JpaRepository<ContentsLike, Long> {
    boolean existsByUserAndContents(User user, Long contentsId);
    ContentsLike findByUserAndContents(User user, Long id);
}
