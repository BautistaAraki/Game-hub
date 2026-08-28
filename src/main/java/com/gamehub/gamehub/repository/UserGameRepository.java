package com.gamehub.gamehub.repository;

import com.gamehub.gamehub.mode1.UserGame;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGameRepository extends JpaRepository<UserGame, Long> {

    boolean existsByUserIdAndGameId(Long userId, Long gameId);
    List<UserGame> findByUserid(long userId);
    
}