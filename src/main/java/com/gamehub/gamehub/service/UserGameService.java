package com.gamehub.gamehub.service;

import com.gamehub.gamehub.dto.UserGameResponse;
import com.gamehub.gamehub.exception.GameAlreadyInLibraryException;
import com.gamehub.gamehub.exception.ResourceNotFoundException;
import com.gamehub.gamehub.model.Game;
import com.gamehub.gamehub.model.GameStatus;
import com.gamehub.gamehub.model.User;
import com.gamehub.gamehub.model.UserGame;
import com.gamehub.gamehub.repository.GameRepository;
import java.util.List;
import com.gamehub.gamehub.repository.UserGameRepository;
import com.gamehub.gamehub.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserGameService {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final UserGameRepository userGameRepository;

    public UserGameService(
            UserGameRepository userGameRepository,
            UserRepository userRepository,
            GameRepository gameRepository
    ) {
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.userGameRepository = userGameRepository;
    }

    @Transactional
    public UserGameResponse addGame(Long userId, Long gameId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario no encontrado")
                );

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Juego no encontrado")
                );

        if (userGameRepository.existsByUser_IdAndGame_Id(userId, gameId)) {
            throw new GameAlreadyInLibraryException();
        }

        UserGame userGame = new UserGame(user, game);
        UserGame savedUserGame = userGameRepository.save(userGame);

        return toResponse(savedUserGame);
    }

    @Transactional(readOnly = true)
    public List<UserGameResponse> getLibrary(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        return userGameRepository.findByUser_Id(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public UserGameResponse updateRating(Long authenticatedUserId, Long userGameId, Integer rating) {
        UserGame userGame = findOwnedUserGame(authenticatedUserId, userGameId);

        userGame.setRating(rating);
        UserGame savedUserGame = userGameRepository.save(userGame);

        return toResponse(savedUserGame);
    }

    @Transactional
    public UserGameResponse updateStatus(Long authenticatedUserId, Long userGameId, GameStatus status) {
        UserGame userGame = findOwnedUserGame(authenticatedUserId, userGameId);

        userGame.changeStatus(status);
        UserGame savedUserGame = userGameRepository.save(userGame);

        return toResponse(savedUserGame);
    }

    @Transactional
    public UserGameResponse addToFavorites(Long authenticatedUserId, Long userGameId) {
        UserGame userGame = findOwnedUserGame(authenticatedUserId, userGameId);

        userGame.markAsFavorite();
        UserGame savedUserGame = userGameRepository.save(userGame);

        return toResponse(savedUserGame);
    }

    @Transactional
    public UserGameResponse removeFromFavorites(Long authenticatedUserId, Long userGameId) {
        UserGame userGame = findOwnedUserGame(authenticatedUserId, userGameId);

        userGame.removeFromFavorites();
        UserGame savedUserGame = userGameRepository.save(userGame);

        return toResponse(savedUserGame);
    }

    @Transactional
    public void removeGame(Long authenticatedUserId, Long userGameId) {
        UserGame userGame = findOwnedUserGame(authenticatedUserId, userGameId);

        userGameRepository.delete(userGame);
    }

    private UserGame findUserGame(Long userGameId) {
        return userGameRepository.findById(userGameId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Juego de biblioteca no encontrado")
                );
    }

    private UserGame findOwnedUserGame(Long authenticatedUserId, Long userGameId) {
        UserGame userGame = findUserGame(userGameId);

        if (!userGame.getUserId().equals(authenticatedUserId)) {
            throw new AccessDeniedException("No tenes permiso para modificar este juego");
        }

        return userGame;
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
