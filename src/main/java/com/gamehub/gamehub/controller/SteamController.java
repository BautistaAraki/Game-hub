package com.gamehub.gamehub.controller;

import com.gamehub.gamehub.dto.SteamGameResponse;
import com.gamehub.gamehub.dto.SteamImportResponse;
import com.gamehub.gamehub.service.SteamImportService;
import com.gamehub.gamehub.service.SteamService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/steam")
public class SteamController {

    private final SteamService steamService;
    private final SteamImportService steamImportService;

    public SteamController(
            SteamService steamService,
            SteamImportService steamImportService
    ) {
        this.steamService = steamService;
        this.steamImportService = steamImportService;
    }

    @GetMapping("/users/{steamId}/games")
    public List<SteamGameResponse> getOwnedGames(
            @PathVariable String steamId
    ) {
        return steamService.getOwnedGames(steamId);
    }

    @PostMapping("/users/{userId}/import-library")
    public SteamImportResponse importLibrary(
            @PathVariable Long userId
    ) {
        return steamImportService.importLibrary(userId);
    }
}
