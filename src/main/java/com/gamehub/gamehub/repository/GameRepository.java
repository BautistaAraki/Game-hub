package com.gamehub.gamehub.repository;

import com.gamehub.gamehub.mode1.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
}