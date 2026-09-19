package com.gamehub.gamehub.service;

import com.gamehub.gamehub.dto.IgdbGameResponse;
import com.gamehub.gamehub.integration.igdb.IgdbClient;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class IgdbService {

    private final IgdbClient igdbClient;

    public IgdbService(IgdbClient igdbClient) {
        this.igdbClient = igdbClient;
    }

    public List<IgdbGameResponse> searchGames(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("El texto de busqueda es obligatorio");
        }

        String trimmed = query.trim();
        if (trimmed.length() > 200 || trimmed.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("La busqueda debe tener hasta 200 caracteres y no contener caracteres de control");
        }
        return igdbClient.searchGames(trimmed);
    }
}
