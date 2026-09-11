package com.gamehub.gamehub.integration.steam;

import com.gamehub.gamehub.DTO.SteamGameResponse;
import java.util.List;

public interface SteamClient {

    List<SteamGameResponse> getOwnedGames(String steamId);
}
