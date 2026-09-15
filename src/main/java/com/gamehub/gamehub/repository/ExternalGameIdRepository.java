package com.gamehub.gamehub.repository;

import com.gamehub.gamehub.model.ExternalGameId;
import com.gamehub.gamehub.model.Platform;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExternalGameIdRepository extends JpaRepository<ExternalGameId, Long> {

    Optional<ExternalGameId> findByPlatformAndExternalId(
            Platform platform,
            String externalId
    );
}
