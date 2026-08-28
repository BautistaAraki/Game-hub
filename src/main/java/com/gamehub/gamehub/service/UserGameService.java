package com.gamehub.gamehub.service;
import com.gamehub.gamehub.mode1.User;
import com.gamehub.gamehub.mode1.Game;
import com.gamehub.gamehub.mode1.UserGame;
import com.gamehub.gamehub.repository.UserGameRepository;

import java.util.List;

import org.springframework.stereotype.Service;
import com.gamehub.gamehub.exception.GameAlreadyInLibraryException;
public class UserGameService {
    private final UserGameRepository userGameRepository;
    public UserGameService(UserGameRepository userGameRepository) {
        this.userGameRepository = userGameRepository;
    }
    public UserGame addGame(User user, Game game) {

    if (userGameRepository.existsByUserIdAndGameId(
            user.getId(),
            game.getId()
    )) {
        throw new GameAlreadyInLibraryException();
    }

    UserGame userGame = new UserGame(user, game);

    return userGameRepository.save(userGame);
}

public List<UserGame> getLibrary(Long userId) {
    return userGameRepository.findByUserid(userId);
}
    
}
