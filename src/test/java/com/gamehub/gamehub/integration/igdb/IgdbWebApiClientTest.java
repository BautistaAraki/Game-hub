package com.gamehub.gamehub.integration.igdb;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import com.gamehub.gamehub.exception.IgdbIntegrationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class IgdbWebApiClientTest {
    private final RestClient.Builder builder = RestClient.builder().baseUrl("https://api.igdb.com/v4");
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final TwitchTokenProvider tokens = mock(TwitchTokenProvider.class);
    private final IgdbWebApiClient client = new IgdbWebApiClient(builder.build(), tokens, "test-id");

    @Test
    void searchesWithHeadersAndMapsOptionalFields() {
        when(tokens.getToken()).thenReturn("test-token");
        server.expect(requestTo("https://api.igdb.com/v4/games"))
                .andExpect(header("Client-ID", "test-id"))
                .andExpect(header("Authorization", "Bearer test-token"))
                .andExpect(content().string("search \"Minecraft\"; fields name,cover.image_id,first_release_date,rating; limit 10;"))
                .andRespond(withSuccess("""
                        [{"id":1,"name":"Minecraft","cover":{"image_id":"co123"},"first_release_date":0,"rating":80},
                         {"id":2,"name":"Manual"},{"name":"Invalid"}]
                        """, MediaType.APPLICATION_JSON));
        var games = client.searchGames("Minecraft");
        assertEquals(2, games.size());
        assertEquals("https://images.igdb.com/igdb/image/upload/t_cover_big/co123.jpg", games.get(0).imageUrl());
        assertEquals("1970-01-01", games.get(0).released());
        assertEquals(80.0, games.get(0).rating());
        assertNull(games.get(1).imageUrl());
        assertNull(games.get(1).released());
        assertNull(games.get(1).rating());
        server.verify();
    }

    @Test
    void escapesQuotesAndBackslashes() {
        when(tokens.getToken()).thenReturn("token");
        server.expect(requestTo("https://api.igdb.com/v4/games"))
                .andExpect(content().string("search \"a\\\"b\\\\c\"; fields name,cover.image_id,first_release_date,rating; limit 10;"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        assertTrue(client.searchGames("a\"b\\c").isEmpty());
        server.verify();
    }

    @Test
    void renewsRejectedTokenAndRetriesOnce() {
        when(tokens.getToken()).thenReturn("old", "new");
        server.expect(requestTo("https://api.igdb.com/v4/games")).andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        server.expect(requestTo("https://api.igdb.com/v4/games"))
                .andExpect(header("Authorization", "Bearer new"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        assertTrue(client.searchGames("game").isEmpty());
        verify(tokens).invalidate("old");
        server.verify();
    }

    @Test
    void stopsAfterSecondUnauthorizedResponse() {
        when(tokens.getToken()).thenReturn("old", "new");
        server.expect(requestTo("https://api.igdb.com/v4/games")).andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        server.expect(requestTo("https://api.igdb.com/v4/games")).andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        assertThrows(IgdbIntegrationException.class, () -> client.searchGames("game"));
        server.verify();
    }

    @Test
    void hidesUpstreamErrorAndDoesNotRetryRateLimit() {
        when(tokens.getToken()).thenReturn("token");
        server.expect(requestTo("https://api.igdb.com/v4/games"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).body("secret upstream details"));
        var error = assertThrows(IgdbIntegrationException.class, () -> client.searchGames("game"));
        assertFalse(error.getMessage().contains("secret"));
        verify(tokens, never()).invalidate(anyString());
        server.verify();
    }
}
