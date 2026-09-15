package com.gamehub.gamehub.repository;

import com.gamehub.gamehub.model.Platform;
import com.gamehub.gamehub.model.UserExternalAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserExternalAccountRepository
        extends JpaRepository<UserExternalAccount, Long> {

    boolean existsByUser_IdAndPlatform(Long userId, Platform platform);

    boolean existsByPlatformAndExternalUserId(
            Platform platform,
            String externalUserId
    );

    Optional<UserExternalAccount> findByUser_IdAndPlatform(
            Long userId,
            Platform platform
    );
}
