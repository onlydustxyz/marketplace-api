package onlydust.com.marketplace.api.postgres.adapter.repository.old;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.old.RegisteredUserViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RegisteredUserRepository extends JpaRepository<RegisteredUserViewEntity, UUID> {
    Optional<RegisteredUserViewEntity> findByGithubId(Long githubId);
}
