package com.gamehub.gamehub.service;

import com.gamehub.gamehub.DTO.GameResponse;
import com.gamehub.gamehub.mode1.Game;
import com.gamehub.gamehub.repository.GameRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GameService {

    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Transactional(readOnly = true)
    public List<GameResponse> getCatalog() {
        return gameRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GameResponse> searchByName(String query) {
        if (query == null || query.isBlank()) {
            return getCatalog();
        }

        return gameRepository.findByNameContainingIgnoreCase(query.trim())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private GameResponse toResponse(Game game) {
        return new GameResponse(
                game.getId(),
                game.getName(),
                game.getImageUrl()
        );
    }
}
