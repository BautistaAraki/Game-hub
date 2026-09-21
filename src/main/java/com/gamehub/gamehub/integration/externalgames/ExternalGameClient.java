package com.gamehub.gamehub.integration.externalgames;

import com.gamehub.gamehub.dto.ExternalGameSearchResponse;
import java.util.List;

public interface ExternalGameClient {

    List<ExternalGameSearchResponse> searchGames(String query);
}
