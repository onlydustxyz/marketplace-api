package onlydust.com.marketplace.api.postgres.adapter.repository;


import onlydust.com.marketplace.api.postgres.adapter.entity.read.GithubAccountEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface GithubAccountRepository extends JpaRepository<GithubAccountEntity, UUID>, JpaSpecificationExecutor<ProjectEntity> {
    Optional<GithubAccountEntity> findByInstallationId(Long installationId);
}
