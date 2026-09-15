package com.gamehub.gamehub.integration.steam;

import com.gamehub.gamehub.dto.SteamGameResponse;
import com.gamehub.gamehub.exception.SteamIntegrationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class SteamWebApiClient implements SteamClient {

    private final RestClient restClient;
    private final String apiKey;

    public SteamWebApiClient(
            RestClient.Builder restClientBuilder,
            @Value("${steam.api.base-url}") String baseUrl,
            @Value("${steam.api.key:}") String apiKey
    ) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
        this.apiKey = apiKey;
    }

    @Override
    public List<SteamGameResponse> getOwnedGames(String steamId) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new SteamIntegrationException("STEAM_API_KEY no esta configurada");
        }

        try {
            Map<?, ?> body = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/IPlayerService/GetOwnedGames/v1/")
                            .queryParam("key", apiKey)
                            .queryParam("steamid", steamId)
                            .queryParam("include_appinfo", true)
                            .queryParam("include_played_free_games", true)
                            .build())
                    .retrieve()
                    .body(Map.class);

            return mapGames(body);
        } catch (RestClientException exception) {
            throw new SteamIntegrationException("No se pudo consultar la biblioteca de Steam");
        }
    }

    private List<SteamGameResponse> mapGames(Map<?, ?> body) {
        if (body == null) {
            return List.of();
        }

        Object response = body.get("response");
        if (!(response instanceof Map<?, ?> responseMap)) {
            return List.of();
        }

        Object games = responseMap.get("games");
        if (!(games instanceof List<?> gameList)) {
            return List.of();
        }

        List<SteamGameResponse> responses = new ArrayList<>();

        for (Object game : gameList) {
            if (game instanceof Map<?, ?> gameMap) {
                responses.add(toResponse(gameMap));
            }
        }

        return responses;
    }

    private SteamGameResponse toResponse(Map<?, ?> game) {
        return new SteamGameResponse(
                toLong(game.get("appid")),
                toString(game.get("name")),
                toInteger(game.get("playtime_forever")),
                toString(game.get("img_icon_url"))
        );
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }

        return null;
    }

    private Integer toInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }

        return null;
    }

    private String toString(Object value) {
        if (value instanceof String string) {
            return string;
        }

        return null;
    }
}
