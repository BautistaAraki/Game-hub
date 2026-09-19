package com.gamehub.gamehub.integration.igdb;

import com.gamehub.gamehub.exception.IgdbIntegrationException;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class TwitchTokenProvider {
    private final RestClient client;
    private final String clientId;
    private final String clientSecret;
    private final Clock clock;
    private String token;
    private Instant expiresAt = Instant.EPOCH;

    @org.springframework.beans.factory.annotation.Autowired
    public TwitchTokenProvider(
            @Value("${igdb.client-id:}") String clientId,
            @Value("${igdb.client-secret:}") String clientSecret
    ) {
        this(IgdbHttpClients.builder().baseUrl("https://id.twitch.tv/oauth2").build(),
                clientId, clientSecret, Clock.systemUTC());
    }

    TwitchTokenProvider(RestClient client, String clientId, String clientSecret, Clock clock) {
        this.client = client;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.clock = clock;
    }

    public synchronized String getToken() {
        if (clientId.isBlank() || clientSecret.isBlank()) {
            throw new IgdbIntegrationException("IGDB no esta configurado en el servidor");
        }
        if (token != null && clock.instant().isBefore(expiresAt)) return token;

        var form = new LinkedMultiValueMap<String, String>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("grant_type", "client_credentials");
        try {
            Instant requestedAt = clock.instant();
            Map<?, ?> response = client.post().uri("/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form).retrieve().body(Map.class);
            if (response == null || !(response.get("access_token") instanceof String value)
                    || value.isBlank() || !(response.get("expires_in") instanceof Number seconds)
                    || seconds.longValue() <= 0) {
                throw new IgdbIntegrationException("Twitch devolvio una autenticacion invalida");
            }
            token = value;
            expiresAt = requestedAt.plusSeconds(Math.max(0, seconds.longValue() - 60));
            return token;
        } catch (RestClientException exception) {
            // Do not expose upstream bodies or credentials in the public error response.
            throw new IgdbIntegrationException("No se pudo autenticar con Twitch para consultar IGDB");
        }
    }

    public synchronized void invalidate(String rejectedToken) {
        if (rejectedToken.equals(token)) {
            token = null;
            expiresAt = Instant.EPOCH;
        }
    }
}
