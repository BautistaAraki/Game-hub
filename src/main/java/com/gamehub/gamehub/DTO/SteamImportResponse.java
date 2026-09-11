package com.gamehub.gamehub.DTO;

public record SteamImportResponse(
        Long userId,
        int steamGamesFound,
        int gamesCreated,
        int gamesMatched,
        int libraryEntriesCreated,
        int libraryEntriesSkipped
) {
}
