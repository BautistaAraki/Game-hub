package com.gamehub.gamehub.service;

import com.gamehub.gamehub.DTO.LibraryStatsResponse;
import com.gamehub.gamehub.mode1.GameStatus;
import com.gamehub.gamehub.mode1.UserGame;
import com.gamehub.gamehub.repository.UserGameRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LibraryStatsService {

    private final UserGameRepository userGameRepository;

    public LibraryStatsService(UserGameRepository userGameRepository) {
        this.userGameRepository = userGameRepository;
    }

    @Transactional(readOnly = true)
    public LibraryStatsResponse getStats(Long userId) {
        List<UserGame> library = userGameRepository.findByUser_Id(userId);

        int totalPlaytimeMinutes = library.stream()
                .map(UserGame::getPlaytimeMinutes)
                .filter(playtime -> playtime != null)
                .mapToInt(Integer::intValue)
                .sum();

        return new LibraryStatsResponse(
                userId,
                library.size(),
                countByStatus(library, GameStatus.COMPLETED),
                countByStatus(library, GameStatus.BACKLOG),
                countByStatus(library, GameStatus.PLAYING),
                countByStatus(library, GameStatus.ON_HOLD),
                countByStatus(library, GameStatus.DROPPED),
                countFavorites(library),
                totalPlaytimeMinutes,
                totalPlaytimeMinutes / 60
        );
    }

    private int countByStatus(List<UserGame> library, GameStatus status) {
        return (int) library.stream()
                .filter(userGame -> userGame.getStatus() == status)
                .count();
    }

    private int countFavorites(List<UserGame> library) {
        return (int) library.stream()
                .filter(UserGame::isFavorite)
                .count();
    }
}
