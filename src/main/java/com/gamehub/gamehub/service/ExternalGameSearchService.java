package com.gamehub.gamehub.service;

import com.gamehub.gamehub.dto.AddExternalGameToLibraryRequest;
import com.gamehub.gamehub.dto.ExternalGameSearchResponse;
import com.gamehub.gamehub.dto.UserGameResponse;
import com.gamehub.gamehub.exception.GameAlreadyInLibraryException;
import com.gamehub.gamehub.exception.ResourceNotFoundException;
import com.gamehub.gamehub.integration.externalgames.ExternalGameClient;
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

    private final ExternalGameClient externalGameClient;
    private final ExternalGameIdRepository externalGameIdRepository;
    private final GameRepository gameRepository;
    private final UserGameRepository userGameRepository;
    private final UserRepository userRepository;

    public ExternalGameSearchService(
            ExternalGameClient externalGameClient,
            ExternalGameIdRepository externalGameIdRepository,
            GameRepository gameRepository,
            UserGameRepository userGameRepository,
            UserRepository userRepository
    ) {
        this.externalGameClient = externalGameClient;
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

        return externalGameClient.searchGames(query.trim());
    }

    @Transactional
    public UserGameResponse addToLibrary(Long authenticatedUserId, AddExternalGameToLibraryRequest request) {
        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Platform platform = toPlatform(request.source());
        String externalId = request.externalId().trim();
        String title = request.title().trim();
        String imageUrl = normalizeBlank(request.imageUrl());
        String description = normalizeBlank(request.description());
        String releaseDate = normalizeBlank(request.releaseDate());
        String platforms = normalizePlatforms(request.platforms());

        Game game = externalGameIdRepository.findByPlatformAndExternalId(platform, externalId)
                .map(ExternalGameId::getGame)
                .orElseGet(() -> createGame(
                        platform,
                        externalId,
                        title,
                        imageUrl,
                        description,
                        releaseDate,
                        platforms
                ));

        game.updateExternalMetadata(
                imageUrl,
                description,
                releaseDate,
                platforms,
                platform.name()
        );

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
            String imageUrl,
            String description,
            String releaseDate,
            String platforms
    ) {
        Game game = gameRepository.findByNameIgnoreCase(title)
                .orElseGet(() -> new Game(title, imageUrl));

        game.updateExternalMetadata(
                imageUrl,
                description,
                releaseDate,
                platforms,
                platform.name()
        );
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

    private String normalizePlatforms(List<String> platforms) {
        if (platforms == null || platforms.isEmpty()) {
            return null;
        }

        String value = String.join(", ", platforms.stream()
                .filter(platform -> platform != null && !platform.isBlank())
                .map(String::trim)
                .distinct()
                .toList());

        return normalizeBlank(value);
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
