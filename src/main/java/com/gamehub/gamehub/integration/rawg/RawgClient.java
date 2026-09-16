package com.gamehub.gamehub.integration.rawg;

import com.gamehub.gamehub.dto.RawgGameResponse;
import java.util.List;

public interface RawgClient {

    List<RawgGameResponse> searchGames(String query);
}
