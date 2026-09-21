package com.gamehub.gamehub.integration.gamelegend;

import com.gamehub.gamehub.dto.ExternalGameSearchResponse;
import java.util.List;

public interface GameLegendClient {

    List<ExternalGameSearchResponse> searchGames(String query);
}
