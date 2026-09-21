package com.gamehub.gamehub.repository;

import com.gamehub.gamehub.model.Game;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findByNameContainingIgnoreCase(String name);

    Optional<Game> findByNameIgnoreCase(String name);
}
