package com.gamehub.gamehub.service;
import com.gamehub.gamehub.mode1.User;
import com.gamehub.gamehub.mode1.Game;
import com.gamehub.gamehub.mode1.GameStatus;
import com.gamehub.gamehub.mode1.UserGame;
import com.gamehub.gamehub.repository.UserGameRepository;
import com.gamehub.gamehub.repository.UserRepository;
import com.gamehub.gamehub.repository.GameRepository; 
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gamehub.gamehub.DTO.UserGameResponse;
import com.gamehub.gamehub.exception.GameAlreadyInLibraryException;
import com.gamehub.gamehub.exception.ResourceNotFoundException;
@Service
public class UserGameService {
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final UserGameRepository userGameRepository;
    public UserGameService(UserGameRepository userGameRepository,UserRepository userRepository,GameRepository gameRepository) {
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
    return userGameRepository.findByUser_Id(userId)
            .stream()
            .map(this::toResponse)
            .toList();
    }
   @Transactional
   public UserGameResponse updateRating(Long userGameId, Integer rating) {

    UserGame userGame = userGameRepository.findById(userGameId)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Juego de biblioteca no encontrado"
                    )
            );

    userGame.setRating(rating);

    UserGame savedUserGame = userGameRepository.save(userGame);

    return toResponse(savedUserGame);
    }
    @Transactional
    public UserGameResponse updateStatus(Long userGameId, GameStatus status) {

    UserGame userGame = userGameRepository.findById(userGameId)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Juego de biblioteca no encontrado"
                    )
            );

    userGame.changeStatus(status);

    UserGame savedUserGame = userGameRepository.save(userGame);

    return toResponse(savedUserGame);
}
@Transactional
public UserGameResponse addToFavorites(Long userGameId) {

    UserGame userGame = userGameRepository.findById(userGameId)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Juego de biblioteca no encontrado"
                    )
            );

    userGame.markAsFavorite();

    UserGame savedUserGame = userGameRepository.save(userGame);

    return toResponse(savedUserGame);
}

@Transactional
public UserGameResponse removeFromFavorites(Long userGameId) {

    UserGame userGame = userGameRepository.findById(userGameId)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Juego de biblioteca no encontrado"
                    )
            );

    userGame.removeFromFavorites();

    UserGame savedUserGame = userGameRepository.save(userGame);

    return toResponse(savedUserGame);
}
@Transactional
public void removeGame(Long userGameId) {

    UserGame userGame = userGameRepository.findById(userGameId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Juego de biblioteca no encontrado")
            );

    userGameRepository.delete(userGame);
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
