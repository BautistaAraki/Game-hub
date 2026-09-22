package com.gamehub.gamehub.dto;

public record LibraryStatsResponse(
        Long userId,
        int totalGames,
        int completedGames,
        int backlogGames,
        int playingGames,
        int onHoldGames,
        int droppedGames,
        int favoriteGames,
        int totalPlaytimeMinutes,
        int totalPlaytimeHours
) {
}
