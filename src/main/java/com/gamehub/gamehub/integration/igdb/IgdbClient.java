package com.gamehub.gamehub.integration.igdb;

import com.gamehub.gamehub.dto.IgdbGameResponse;
import java.util.List;

public interface IgdbClient {

    List<IgdbGameResponse> searchGames(String query);
}
