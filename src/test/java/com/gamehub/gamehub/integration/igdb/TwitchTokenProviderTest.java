package com.gamehub.gamehub.integration.igdb;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import com.gamehub.gamehub.exception.IgdbIntegrationException;
import java.time.Clock;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class TwitchTokenProviderTest {
    private final RestClient.Builder builder = RestClient.builder().baseUrl("https://id.twitch.tv/oauth2");
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final Clock clock = mock(Clock.class);
    private final TwitchTokenProvider provider = new TwitchTokenProvider(builder.build(), "id", "secret", clock);

    @Test
    void cachesTokenAndRenewsBeforeExpiry() {
        when(clock.instant()).thenReturn(Instant.EPOCH);
        server.expect(requestTo("https://id.twitch.tv/oauth2/token"))
                .andExpect(content().string("client_id=id&client_secret=secret&grant_type=client_credentials"))
                .andRespond(withSuccess("{\"access_token\":\"first\",\"expires_in\":3600}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://id.twitch.tv/oauth2/token"))
                .andRespond(withSuccess("{\"access_token\":\"second\",\"expires_in\":3600}", MediaType.APPLICATION_JSON));
        assertEquals("first", provider.getToken());
        assertEquals("first", provider.getToken());
        when(clock.instant()).thenReturn(Instant.EPOCH.plusSeconds(3540));
        assertEquals("second", provider.getToken());
        server.verify();
    }

    @Test
    void rejectsMissingCredentialsWithoutNetworkCall() {
        var missing = new TwitchTokenProvider(builder.build(), "", "", clock);
        assertThrows(IgdbIntegrationException.class, missing::getToken);
        server.verify();
    }

    @Test
    void rejectsMalformedTokenResponse() {
        when(clock.instant()).thenReturn(Instant.EPOCH);
        server.expect(requestTo("https://id.twitch.tv/oauth2/token"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        assertThrows(IgdbIntegrationException.class, provider::getToken);
        server.verify();
    }

    @Test
    void sanitizesAuthenticationFailure() {
        when(clock.instant()).thenReturn(Instant.EPOCH);
        server.expect(requestTo("https://id.twitch.tv/oauth2/token"))
                .andRespond(withBadRequest().body("secret"));
        var error = assertThrows(IgdbIntegrationException.class, provider::getToken);
        assertFalse(error.getMessage().contains("secret"));
        server.verify();
    }
}
