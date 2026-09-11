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
            name = "uk_user_external_account_user_platform",
            columnNames = {"user_id", "platform"}
        ),
        @UniqueConstraint(
            name = "uk_user_external_account_platform_external_user",
            columnNames = {"platform", "external_user_id"}
        )
    }
)
public class UserExternalAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    @Column(name = "external_user_id", nullable = false)
    private String externalUserId;

    protected UserExternalAccount() {
    }

    public UserExternalAccount(User user, Platform platform, String externalUserId) {
        if (user == null) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }

        if (platform == null) {
            throw new IllegalArgumentException("La plataforma es obligatoria");
        }

        if (externalUserId == null || externalUserId.isBlank()) {
            throw new IllegalArgumentException("El ID externo es obligatorio");
        }

        this.user = user;
        this.platform = platform;
        this.externalUserId = externalUserId;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return user.getId();
    }

    public Platform getPlatform() {
        return platform;
    }

    public String getExternalUserId() {
        return externalUserId;
    }
}
