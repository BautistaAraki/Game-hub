package com.gamehub.gamehub.integration.gamelegend;

import com.gamehub.gamehub.dto.ExternalGameSearchResponse;
import com.gamehub.gamehub.exception.ExternalGameIntegrationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class GameLegendWebApiClient implements GameLegendClient {

    private static final String SOURCE = "GAMELEGEND";

    private final RestClient restClient;

    public GameLegendWebApiClient(
            RestClient.Builder restClientBuilder,
            @Value("${gamelegend.api.base-url}") String baseUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public List<ExternalGameSearchResponse> searchGames(String query) {
        try {
            Map<?, ?> body = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/games")
                            .queryParam("q", query)
                            .queryParam("limit", 12)
                            .build())
                    .retrieve()
                    .body(Map.class);

            return mapGames(body);
        } catch (RestClientException exception) {
            throw new ExternalGameIntegrationException("No se pudo consultar GameLegend");
        }
    }

    private List<ExternalGameSearchResponse> mapGames(Map<?, ?> body) {
        if (body == null) {
            return List.of();
        }

        Object games = body.get("games");
        if (!(games instanceof List<?> gameList)) {
            return List.of();
        }

        List<ExternalGameSearchResponse> responses = new ArrayList<>();

        for (Object game : gameList) {
            if (game instanceof Map<?, ?> gameMap) {
                ExternalGameSearchResponse response = toResponse(gameMap);
                if (response.externalId() != null && response.title() != null) {
                    responses.add(response);
                }
            }
        }

        return responses;
    }

    private ExternalGameSearchResponse toResponse(Map<?, ?> game) {
        return new ExternalGameSearchResponse(
                SOURCE,
                toString(game.get("slug")),
                toString(game.get("title")),
                toString(game.get("description")),
                toString(game.get("coverImageUrl")),
                toString(game.get("releaseDate"))
        );
    }

    private String toString(Object value) {
        if (value instanceof String string && !string.isBlank()) {
            return string;
        }

        return null;
    }
}
