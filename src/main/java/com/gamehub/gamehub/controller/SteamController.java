package com.gamehub.gamehub.controller;

import com.gamehub.gamehub.DTO.SteamGameResponse;
import com.gamehub.gamehub.service.SteamService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/steam")
public class SteamController {

    private final SteamService steamService;

    public SteamController(SteamService steamService) {
        this.steamService = steamService;
    }

    @GetMapping("/users/{steamId}/games")
    public List<SteamGameResponse> getOwnedGames(
            @PathVariable String steamId
    ) {
        return steamService.getOwnedGames(steamId);
    }
}
