package com.gamehub.gamehub.repository;

import com.gamehub.gamehub.mode1.ExternalGameId;
import com.gamehub.gamehub.mode1.Platform;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExternalGameIdRepository extends JpaRepository<ExternalGameId, Long> {

    Optional<ExternalGameId> findByPlatformAndExternalId(
            Platform platform,
            String externalId
    );
}
