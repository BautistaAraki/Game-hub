package com.gamehub.gamehub.service;

import com.gamehub.gamehub.dto.AddExternalGameToLibraryRequest;
import com.gamehub.gamehub.dto.ExternalGameSearchResponse;
import com.gamehub.gamehub.dto.UserGameResponse;
import com.gamehub.gamehub.exception.GameAlreadyInLibraryException;
import com.gamehub.gamehub.exception.ResourceNotFoundException;
import com.gamehub.gamehub.integration.gamelegend.GameLegendClient;
import com.gamehub.gamehub.model.ExternalGameId;
import com.gamehub.gamehub.model.Game;
import com.gamehub.gamehub.model.Platform;
import com.gamehub.gamehub.model.User;
import com.gamehub.gamehub.model.UserGame;
import com.gamehub.gamehub.repository.ExternalGameIdRepository;
import com.gamehub.gamehub.repository.GameRepository;
import com.gamehub.gamehub.repository.UserGameRepository;
import com.gamehub.gamehub.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExternalGameSearchService {

    private final GameLegendClient gameLegendClient;
    private final ExternalGameIdRepository externalGameIdRepository;
    private final GameRepository gameRepository;
    private final UserGameRepository userGameRepository;
    private final UserRepository userRepository;

    public ExternalGameSearchService(
            GameLegendClient gameLegendClient,
            ExternalGameIdRepository externalGameIdRepository,
            GameRepository gameRepository,
            UserGameRepository userGameRepository,
            UserRepository userRepository
    ) {
        this.gameLegendClient = gameLegendClient;
        this.externalGameIdRepository = externalGameIdRepository;
        this.gameRepository = gameRepository;
        this.userGameRepository = userGameRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ExternalGameSearchResponse> searchGames(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("El nombre del juego es obligatorio");
        }

        return gameLegendClient.searchGames(query.trim());
    }

    @Transactional
    public UserGameResponse addToLibrary(AddExternalGameToLibraryRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Platform platform = toPlatform(request.source());
        String externalId = request.externalId().trim();
        String title = request.title().trim();
        String imageUrl = normalizeBlank(request.imageUrl());

        Game game = externalGameIdRepository.findByPlatformAndExternalId(platform, externalId)
                .map(ExternalGameId::getGame)
                .orElseGet(() -> createGame(platform, externalId, title, imageUrl));

        if (userGameRepository.existsByUser_IdAndGame_Id(user.getId(), game.getId())) {
            throw new GameAlreadyInLibraryException();
        }

        UserGame userGame = new UserGame(user, game);
        UserGame savedUserGame = userGameRepository.save(userGame);

        return toResponse(savedUserGame);
    }

    private Game createGame(
            Platform platform,
            String externalId,
            String title,
            String imageUrl
    ) {
        Game game = gameRepository.findByNameIgnoreCase(title)
                .orElseGet(() -> new Game(title, imageUrl));

        game.addExternalID(new ExternalGameId(platform, externalId));

        return gameRepository.save(game);
    }

    private Platform toPlatform(String source) {
        try {
            return Platform.valueOf(source.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Fuente externa invalida: " + source);
        }
    }

    private String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private UserGameResponse toResponse(UserGame userGame) {
        return new UserGameResponse(
                userGame.getId(),
                userGame.getUserId(),
                userGame.getGameId(),
                userGame.getRating(),
                userGame.getPlaytimeMinutes(),
                userGame.isFavorite(),
                userGame.getStatus()
        );
    }
}
