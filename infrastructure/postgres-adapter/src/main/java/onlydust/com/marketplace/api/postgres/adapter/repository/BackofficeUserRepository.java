package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.backoffice.BackofficeUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BackofficeUserRepository extends JpaRepository<BackofficeUserEntity, UUID> {

    Optional<BackofficeUserEntity> findByEmail(String email);
}
