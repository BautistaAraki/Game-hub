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
            name = "uk_user_game",
            columnNames = {"user_id","game_id"}
        )
    }
)
public class UserGame {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name="user_id",nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "game_id",nullable = false)
    private Game game;
    @Column
    private Integer rating;
    private Integer playtimeMinutes;
    private boolean favorite;
    protected UserGame() {
    }   

    public UserGame(User user, Game game) {
        if (user == null || game == null) {
            throw new IllegalArgumentException(
                "El usuario y el juego son obligatorios"
            );
        }

    this.user = user;
    this.game = game;
}
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status =GameStatus.BACKLOG;
    public void setRating(Integer rating){
        if (rating != null && (rating<1 || rating>10)){
            throw new IllegalArgumentException("El rating debe estar entre 1 y 10");
        }
        this.rating = rating;
    }
    public void setplaytimeMinutes(Integer playtimeMinutes){
        if (playtimeMinutes!=null && playtimeMinutes<0){
            throw new IllegalArgumentException(
                "El tiempo de juego no es correcto"
            );
        }
        this.playtimeMinutes=playtimeMinutes;
    }
    public void changeStatus(GameStatus gameStatus) {
    if (gameStatus == null) {
        throw new IllegalArgumentException("El estado no puede ser null");
    }

    this.status = gameStatus;
    }
    public void markAsFavorite() {
        this.favorite = true;
    }

    public void removeFromFavorites() {
        this.favorite = false;
    }
    public Long getId() {
    return id;
}

public Long getUserId() {
    return user.getId();
}

public Long getGameId() {
    return game.getId();
}

public Integer getRating() {
    return rating;
}

public Integer getPlaytimeMinutes() {
    return playtimeMinutes;
}

public boolean isFavorite() {
    return favorite;
}

public GameStatus getStatus() {
    return status;
}

}
