package com.gamehub.gamehub.mode1;
import jakarta.persistence.GenerationType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;


@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;

    private String imageUrl;

    protected Game() {
    }

    public Game(String name, String imageUrl) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del juego es obligatorio");
        }

        this.name = name;
        this.imageUrl = imageUrl;
    }

    public Long getId() {
    return id;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @OneToMany(
    mappedBy = "game",
    cascade = CascadeType.ALL,
    orphanRemoval = true
)
    private Set<ExternalGameId> externalIds = new HashSet<>();
    public void addExternalID(ExternalGameId externalGameId){
        externalIds.add(externalGameId);
        externalGameId.setGame(this);
    }
}
