package com.gamehub.gamehub.service;

import com.gamehub.gamehub.dto.SteamGameResponse;
import com.gamehub.gamehub.dto.SteamImportResponse;
import com.gamehub.gamehub.exception.ResourceNotFoundException;
import com.gamehub.gamehub.integration.steam.SteamClient;
import com.gamehub.gamehub.model.ExternalGameId;
import com.gamehub.gamehub.model.Game;
import com.gamehub.gamehub.model.Platform;
import com.gamehub.gamehub.model.User;
import com.gamehub.gamehub.model.UserExternalAccount;
import com.gamehub.gamehub.model.UserGame;
import com.gamehub.gamehub.repository.ExternalGameIdRepository;
import com.gamehub.gamehub.repository.GameRepository;
import com.gamehub.gamehub.repository.UserExternalAccountRepository;
import com.gamehub.gamehub.repository.UserGameRepository;
import com.gamehub.gamehub.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SteamImportService {

    private final UserRepository userRepository;
    private final UserExternalAccountRepository userExternalAccountRepository;
    private final ExternalGameIdRepository externalGameIdRepository;
    private final GameRepository gameRepository;
    private final UserGameRepository userGameRepository;
    private final SteamClient steamClient;

    public SteamImportService(
            UserRepository userRepository,
            UserExternalAccountRepository userExternalAccountRepository,
            ExternalGameIdRepository externalGameIdRepository,
            GameRepository gameRepository,
            UserGameRepository userGameRepository,
            SteamClient steamClient
    ) {
        this.userRepository = userRepository;
        this.userExternalAccountRepository = userExternalAccountRepository;
        this.externalGameIdRepository = externalGameIdRepository;
        this.gameRepository = gameRepository;
        this.userGameRepository = userGameRepository;
        this.steamClient = steamClient;
    }

    @Transactional
    public SteamImportResponse importLibrary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        UserExternalAccount steamAccount = userExternalAccountRepository
                .findByUser_IdAndPlatform(userId, Platform.STEAM)
                .orElseThrow(() ->
                        new ResourceNotFoundException("El usuario no tiene Steam vinculado")
                );

        List<SteamGameResponse> steamGames =
                steamClient.getOwnedGames(steamAccount.getExternalUserId());

        int gamesCreated = 0;
        int gamesMatched = 0;
        int libraryEntriesCreated = 0;
        int libraryEntriesSkipped = 0;

        for (SteamGameResponse steamGame : steamGames) {
            String steamAppId = String.valueOf(steamGame.appId());

            Game game = findGameBySteamAppId(steamAppId);

            if (game == null) {
                game = createGameFromSteamGame(steamGame, steamAppId);
                gamesCreated++;
            } else {
                gamesMatched++;
            }

            if (userGameRepository.existsByUser_IdAndGame_Id(userId, game.getId())) {
                libraryEntriesSkipped++;
                continue;
            }

            userGameRepository.save(new UserGame(user, game));
            libraryEntriesCreated++;
        }

        return new SteamImportResponse(
                userId,
                steamGames.size(),
                gamesCreated,
                gamesMatched,
                libraryEntriesCreated,
                libraryEntriesSkipped
        );
    }

    private Game findGameBySteamAppId(String steamAppId) {
        return externalGameIdRepository.findByPlatformAndExternalId(
                Platform.STEAM,
                steamAppId
        )
                .map(ExternalGameId::getGame)
                .orElse(null);
    }

    private Game createGameFromSteamGame(
            SteamGameResponse steamGame,
            String steamAppId
    ) {
        Game game = new Game(steamGame.name(), steamGame.iconUrl());
        game.addExternalID(new ExternalGameId(Platform.STEAM, steamAppId));
        return gameRepository.save(game);
    }
}
