package com.gamehub.gamehub.repository;

import com.gamehub.gamehub.mode1.UserGame;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGameRepository extends JpaRepository<UserGame, Long> {

    boolean existsByUser_IdAndGame_Id(Long userId, Long gameId);

    List<UserGame> findByUser_Id(Long userId);
    
}