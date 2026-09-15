package com.gamehub.gamehub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String username;

    @Column(nullable = false,unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    protected User() {
    }

    public User(String username, String email, String passwordHash) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio");
        }

        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("La contrasena es obligatoria");
        }

        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
