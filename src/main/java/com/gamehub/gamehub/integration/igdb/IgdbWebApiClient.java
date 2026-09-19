package com.gamehub.gamehub.integration.igdb;

import com.gamehub.gamehub.dto.IgdbGameResponse;
import com.gamehub.gamehub.exception.IgdbIntegrationException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class IgdbWebApiClient implements IgdbClient {
    private final RestClient client;
    private final TwitchTokenProvider tokens;
    private final String clientId;

    @org.springframework.beans.factory.annotation.Autowired
    public IgdbWebApiClient(TwitchTokenProvider tokens,
            @Value("${igdb.api.base-url}") String baseUrl,
            @Value("${igdb.client-id:}") String clientId) {
        this(IgdbHttpClients.builder().baseUrl(baseUrl).build(), tokens, clientId);
    }

    IgdbWebApiClient(RestClient client, TwitchTokenProvider tokens, String clientId) {
        this.client = client;
        this.tokens = tokens;
        this.clientId = clientId;
    }

    @Override
    public List<IgdbGameResponse> searchGames(String query) {
        String escaped = query.replace("\\", "\\\\").replace("\"", "\\\"");
        String body = "search \"" + escaped + "\"; fields name,cover.image_id,first_release_date,rating; limit 10;";
        String token = tokens.getToken();
        try {
            return request(body, token);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() != 401) throw unavailable();
            tokens.invalidate(token);
            try {
                return request(body, tokens.getToken());
            } catch (RestClientException retryFailure) {
                throw unavailable();
            }
        } catch (RestClientException exception) {
            throw unavailable();
        }
    }

    private List<IgdbGameResponse> request(String query, String token) {
        List<?> response = client.post().uri("/games")
                .header("Client-ID", clientId)
                .headers(headers -> headers.setBearerAuth(token))
                .contentType(MediaType.TEXT_PLAIN).accept(MediaType.APPLICATION_JSON)
                .body(query).retrieve().body(List.class);
        if (response == null) throw unavailable();
        var games = new ArrayList<IgdbGameResponse>();
        for (Object item : response) {
            if (!(item instanceof Map<?, ?> game)
                    || !(game.get("id") instanceof Number id)
                    || !(game.get("name") instanceof String name) || name.isBlank()) continue;
            String image = null;
            if (game.get("cover") instanceof Map<?, ?> cover
                    && cover.get("image_id") instanceof String imageId
                    && imageId.matches("[a-zA-Z0-9_-]+")) {
                image = "https://images.igdb.com/igdb/image/upload/t_cover_big/" + imageId + ".jpg";
            }
            String released = null;
            if (game.get("first_release_date") instanceof Number timestamp) {
                try {
                    released = Instant.ofEpochSecond(timestamp.longValue()).atOffset(ZoneOffset.UTC).toLocalDate().toString();
                } catch (DateTimeException ignored) { /* Missing or unusable optional date. */ }
            }
            Double rating = game.get("rating") instanceof Number number ? number.doubleValue() : null;
            games.add(new IgdbGameResponse(id.longValue(), name, image, released, rating));
        }
        return games;
    }

    private IgdbIntegrationException unavailable() {
        return new IgdbIntegrationException("No se pudo consultar IGDB. Intenta nuevamente mas tarde");
    }
}
