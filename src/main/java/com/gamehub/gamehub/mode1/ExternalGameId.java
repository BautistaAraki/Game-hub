package com.gamehub.gamehub.mode1;


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
    void setGame(Game game) {
    this.game = game;
    }
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
}
