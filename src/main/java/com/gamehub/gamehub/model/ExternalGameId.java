package com.gamehub.gamehub.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_external_game_platform_id",
            columnNames = {"platform", "external_id"}
        )
    }
)
public class ExternalGameId {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "game_id",nullable = false)
    private Game game;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    protected ExternalGameId() {
    }

    public ExternalGameId(Platform platform, String externalId) {
        if (platform == null) {
            throw new IllegalArgumentException("La plataforma es obligatoria");
        }

        if (externalId == null || externalId.isBlank()) {
            throw new IllegalArgumentException("El ID externo es obligatorio");
        }

        this.platform = platform;
        this.externalId = externalId;
    }

    void setGame(Game game) {
        this.game = game;
    }

    public Long getId() {
        return id;
    }

    public Game getGame() {
        return game;
    }

    public Platform getPlatform() {
        return platform;
    }

    public String getExternalId() {
        return externalId;
    }
}
