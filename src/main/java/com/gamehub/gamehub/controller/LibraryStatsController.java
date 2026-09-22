package com.gamehub.gamehub.controller;

import com.gamehub.gamehub.dto.LibraryStatsResponse;
import com.gamehub.gamehub.security.GameHubPrincipal;
import com.gamehub.gamehub.service.LibraryStatsService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/library")
public class LibraryStatsController {

    private final LibraryStatsService libraryStatsService;

    public LibraryStatsController(LibraryStatsService libraryStatsService) {
        this.libraryStatsService = libraryStatsService;
    }

    @GetMapping("/users/{userId}/stats")
    public LibraryStatsResponse getStats(
            @PathVariable Long userId,
            @AuthenticationPrincipal GameHubPrincipal principal
    ) {
        if (!principal.id().equals(userId)) {
            throw new AccessDeniedException("No tenes permiso para acceder a estas estadisticas");
        }

        return libraryStatsService.getStats(userId);
    }

    @GetMapping("/me/stats")
    public LibraryStatsResponse getMyStats(
            @AuthenticationPrincipal GameHubPrincipal principal
    ) {
        return libraryStatsService.getStats(principal.id());
    }
}
