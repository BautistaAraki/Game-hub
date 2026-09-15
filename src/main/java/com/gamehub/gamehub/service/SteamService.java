package com.gamehub.gamehub.service;

import com.gamehub.gamehub.dto.SteamGameResponse;
import com.gamehub.gamehub.integration.steam.SteamClient;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SteamService {

    private final SteamClient steamClient;

    public SteamService(SteamClient steamClient) {
        this.steamClient = steamClient;
    }

    public List<SteamGameResponse> getOwnedGames(String steamId) {
        if (steamId == null || steamId.isBlank()) {
            throw new IllegalArgumentException("El Steam ID es obligatorio");
        }

        return steamClient.getOwnedGames(steamId.trim());
    }
}
