package com.gamehub.gamehub.model;
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

    @Column(length = 4000)
    private String description;

    private String releaseDate;

    @Column(length = 1000)
    private String platforms;

    private String externalSource;

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

    public String getDescription() {
        return description;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getPlatforms() {
        return platforms;
    }

    public String getExternalSource() {
        return externalSource;
    }

    public void updateExternalMetadata(
            String imageUrl,
            String description,
            String releaseDate,
            String platforms,
            String externalSource
    ) {
        if (this.imageUrl == null || this.imageUrl.isBlank()) {
            this.imageUrl = imageUrl;
        }

        if (this.description == null || this.description.isBlank()) {
            this.description = description;
        }

        if (this.releaseDate == null || this.releaseDate.isBlank()) {
            this.releaseDate = releaseDate;
        }

        if (this.platforms == null || this.platforms.isBlank()) {
            this.platforms = platforms;
        }

        if (this.externalSource == null || this.externalSource.isBlank()) {
            this.externalSource = externalSource;
        }
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
