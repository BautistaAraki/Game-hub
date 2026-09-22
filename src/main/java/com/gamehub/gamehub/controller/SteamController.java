package com.gamehub.gamehub.controller;

import com.gamehub.gamehub.dto.SteamGameResponse;
import com.gamehub.gamehub.dto.SteamImportResponse;
import com.gamehub.gamehub.security.GameHubPrincipal;
import com.gamehub.gamehub.service.SteamImportService;
import com.gamehub.gamehub.service.SteamService;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @PathVariable Long userId,
            @AuthenticationPrincipal GameHubPrincipal principal
    ) {
        if (!principal.id().equals(userId)) {
            throw new AccessDeniedException("No tenes permiso para importar la biblioteca de otro usuario");
        }

        return steamImportService.importLibrary(userId);
    }

    @PostMapping("/me/import-library")
    public SteamImportResponse importMyLibrary(
            @AuthenticationPrincipal GameHubPrincipal principal
    ) {
        return steamImportService.importLibrary(principal.id());
    }
}
