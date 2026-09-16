package com.gamehub.gamehub.service;

import com.gamehub.gamehub.dto.RawgGameResponse;
import com.gamehub.gamehub.integration.rawg.RawgClient;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RawgService {

    private final RawgClient rawgClient;

    public RawgService(RawgClient rawgClient) {
        this.rawgClient = rawgClient;
    }

    public List<RawgGameResponse> searchGames(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("El texto de busqueda es obligatorio");
        }

        return rawgClient.searchGames(query.trim());
    }
}
