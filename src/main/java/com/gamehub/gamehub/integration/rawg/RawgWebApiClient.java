package com.gamehub.gamehub.integration.rawg;

import com.gamehub.gamehub.dto.RawgGameResponse;
import com.gamehub.gamehub.exception.RawgIntegrationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class RawgWebApiClient implements RawgClient {

    private final RestClient restClient;
    private final String apiKey;

    public RawgWebApiClient(
            RestClient.Builder restClientBuilder,
            @Value("${rawg.api.base-url}") String baseUrl,
            @Value("${rawg.api.key:}") String apiKey
    ) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
        this.apiKey = apiKey;
    }

    @Override
    public List<RawgGameResponse> searchGames(String query) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RawgIntegrationException("RAWG_API_KEY no esta configurada");
        }

        try {
            Map<?, ?> body = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/games")
                            .queryParam("key", apiKey)
                            .queryParam("search", query)
                            .queryParam("page_size", 10)
                            .build())
                    .retrieve()
                    .body(Map.class);

            return mapGames(body);
        } catch (RestClientException exception) {
            throw new RawgIntegrationException("No se pudo consultar RAWG");
        }
    }

    private List<RawgGameResponse> mapGames(Map<?, ?> body) {
        if (body == null) {
            return List.of();
        }

        Object results = body.get("results");
        if (!(results instanceof List<?> gameList)) {
            return List.of();
        }

        List<RawgGameResponse> responses = new ArrayList<>();

        for (Object game : gameList) {
            if (game instanceof Map<?, ?> gameMap) {
                responses.add(toResponse(gameMap));
            }
        }

        return responses;
    }

    private RawgGameResponse toResponse(Map<?, ?> game) {
        return new RawgGameResponse(
                toLong(game.get("id")),
                toString(game.get("name")),
                toString(game.get("background_image")),
                toString(game.get("released")),
                toDouble(game.get("rating"))
        );
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }

        return null;
    }

    private Double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
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
