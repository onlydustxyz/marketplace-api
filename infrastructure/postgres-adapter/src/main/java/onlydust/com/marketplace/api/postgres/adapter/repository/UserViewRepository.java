package onlydust.com.marketplace.api.postgres.adapter.repository;

import java.util.Optional;
import java.util.UUID;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserViewRepository extends JpaRepository<UserViewEntity, UUID>, JpaSpecificationExecutor<UserViewEntity> {

  Optional<UserViewEntity> findByGithubUserId(Long githubId);
}
