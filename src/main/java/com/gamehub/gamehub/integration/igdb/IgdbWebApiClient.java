package com.gamehub.gamehub.integration.igdb;

import com.gamehub.gamehub.dto.ExternalGameSearchResponse;
import com.gamehub.gamehub.exception.ExternalGameIntegrationException;
import com.gamehub.gamehub.integration.externalgames.ExternalGameClient;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class IgdbWebApiClient implements ExternalGameClient {

    private static final String SOURCE = "IGDB";

    private final RestClient igdbRestClient;
    private final RestClient twitchRestClient;
    private final String clientId;
    private final String clientSecret;

    private String accessToken;
    private Instant accessTokenExpiresAt = Instant.EPOCH;

    public IgdbWebApiClient(
            RestClient.Builder restClientBuilder,
            @Value("${igdb.api.base-url}") String igdbBaseUrl,
            @Value("${igdb.auth.base-url}") String twitchAuthBaseUrl,
            @Value("${igdb.client-id:}") String clientId,
            @Value("${igdb.client-secret:}") String clientSecret
    ) {
        this.igdbRestClient = restClientBuilder
                .baseUrl(igdbBaseUrl)
                .build();
        this.twitchRestClient = restClientBuilder
                .baseUrl(twitchAuthBaseUrl)
                .build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public List<ExternalGameSearchResponse> searchGames(String query) {
        validateCredentials();

        try {
            List<?> body = igdbRestClient.post()
                    .uri("/games")
                    .header("Client-ID", clientId)
                    .header("Authorization", "Bearer " + getAccessToken())
                    .header("Accept", "application/json")
                    .body(buildSearchBody(query))
                    .retrieve()
                    .body(List.class);

            return mapGames(body);
        } catch (RestClientException exception) {
            throw new ExternalGameIntegrationException("No se pudo consultar IGDB");
        }
    }

    private String buildSearchBody(String query) {
        return """
                search "%s";
                fields name,summary,first_release_date,cover.url,platforms.name;
                where version_parent = null;
                limit 12;
                """.formatted(escapeQuery(query));
    }

    private String escapeQuery(String query) {
        return query.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private synchronized String getAccessToken() {
        if (accessToken != null && Instant.now().isBefore(accessTokenExpiresAt)) {
            return accessToken;
        }

        try {
            Map<?, ?> body = twitchRestClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/oauth2/token")
                            .queryParam("client_id", clientId)
                            .queryParam("client_secret", clientSecret)
                            .queryParam("grant_type", "client_credentials")
                            .build())
                    .retrieve()
                    .body(Map.class);

            String token = toString(body == null ? null : body.get("access_token"));
            Long expiresIn = toLong(body == null ? null : body.get("expires_in"));

            if (token == null || expiresIn == null) {
                throw new ExternalGameIntegrationException("IGDB no devolvio un token valido");
            }

            accessToken = token;
            accessTokenExpiresAt = Instant.now().plusSeconds(Math.max(60, expiresIn - 60));

            return accessToken;
        } catch (RestClientException exception) {
            throw new ExternalGameIntegrationException("No se pudo autenticar con IGDB");
        }
    }

    private List<ExternalGameSearchResponse> mapGames(List<?> games) {
        if (games == null) {
            return List.of();
        }

        List<ExternalGameSearchResponse> responses = new ArrayList<>();

        for (Object game : games) {
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
                toString(game.get("id")),
                toString(game.get("name")),
                toString(game.get("summary")),
                coverUrl(game.get("cover")),
                releaseDate(game.get("first_release_date")),
                platforms(game.get("platforms"))
        );
    }

    private String coverUrl(Object value) {
        if (!(value instanceof Map<?, ?> cover)) {
            return null;
        }

        String url = toString(cover.get("url"));
        if (url == null) {
            return null;
        }

        if (url.startsWith("//")) {
            url = "https:" + url;
        }

        return url.replace("t_thumb", "t_cover_big");
    }

    private String releaseDate(Object value) {
        Long epochSeconds = toLong(value);
        if (epochSeconds == null) {
            return null;
        }

        LocalDate date = Instant.ofEpochSecond(epochSeconds)
                .atZone(ZoneOffset.UTC)
                .toLocalDate();

        return DateTimeFormatter.ISO_LOCAL_DATE.format(date);
    }

    private List<String> platforms(Object value) {
        if (!(value instanceof List<?> platformList)) {
            return List.of();
        }

        List<String> names = new ArrayList<>();

        for (Object platform : platformList) {
            if (platform instanceof Map<?, ?> platformMap) {
                String name = toString(platformMap.get("name"));
                if (name != null) {
                    names.add(name);
                }
            }
        }

        return names;
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }

        if (value instanceof String string && !string.isBlank()) {
            return Long.parseLong(string);
        }

        return null;
    }

    private String toString(Object value) {
        if (value instanceof Number number) {
            return number.toString();
        }

        if (value instanceof String string && !string.isBlank()) {
            return string;
        }

        return null;
    }

    private void validateCredentials() {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            throw new ExternalGameIntegrationException(
                    "IGDB_CLIENT_ID e IGDB_CLIENT_SECRET deben estar configurados"
            );
        }
    }
}
