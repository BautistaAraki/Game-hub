package com.gamehub.gamehub.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gamehub.gamehub.dto.ApiErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFoundReturnsStructured404Response() {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET",
                "/api/library/users/99"
        );

        ResponseEntity<ApiErrorResponse> response = handler.handleNotFound(
                new ResourceNotFoundException("Usuario no encontrado"),
                request
        );

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Usuario no encontrado", response.getBody().message());
        assertEquals("/api/library/users/99", response.getBody().path());
    }

    @Test
    void handleInvalidCredentialsReturnsStructured401Response() {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST",
                "/api/auth/login"
        );

        ResponseEntity<ApiErrorResponse> response = handler.handleInvalidCredentials(
                new InvalidCredentialsException(),
                request
        );

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Credenciales invalidas", response.getBody().message());
        assertEquals("/api/auth/login", response.getBody().path());
    }

    @Test
    void handleSteamIntegrationReturnsStructured502Response() {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST",
                "/api/steam/users/1/import-library"
        );

        ResponseEntity<ApiErrorResponse> response = handler.handleSteamIntegration(
                new SteamIntegrationException("STEAM_API_KEY no esta configurada"),
                request
        );

        assertEquals(502, response.getStatusCode().value());
        assertEquals("STEAM_API_KEY no esta configurada", response.getBody().message());
        assertEquals("/api/steam/users/1/import-library", response.getBody().path());
    }

    @Test
    void handleRawgIntegrationReturnsStructured502Response() {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET",
                "/api/rawg/games/search"
        );

        ResponseEntity<ApiErrorResponse> response = handler.handleRawgIntegration(
                new RawgIntegrationException("RAWG_API_KEY no esta configurada"),
                request
        );

        assertEquals(502, response.getStatusCode().value());
        assertEquals("RAWG_API_KEY no esta configurada", response.getBody().message());
        assertEquals("/api/rawg/games/search", response.getBody().path());
    }
}
