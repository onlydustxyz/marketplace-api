package onlydust.com.marketplace.api.postgres.adapter.repository.old;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.AuthUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

@Deprecated
public interface AuthUserRepository extends JpaRepository<AuthUserEntity, UUID>,
        JpaSpecificationExecutor<AuthUserEntity> {
    Optional<AuthUserEntity> findByGithubUserId(Long githubUserId);
}
