package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface UserViewRepository extends JpaRepository<UserViewEntity, UUID>, JpaSpecificationExecutor<UserViewEntity> {
    Optional<UserViewEntity> findByGithubUserId(Long githubId);
}
