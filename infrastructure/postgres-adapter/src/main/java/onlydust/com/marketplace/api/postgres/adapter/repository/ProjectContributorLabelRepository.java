package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectContributorLabelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;
import java.util.UUID;

public interface ProjectContributorLabelRepository extends JpaRepository<ProjectContributorLabelEntity, UUID> {
    Set<ProjectContributorLabelEntity> findAllByProjectId(UUID projectId);
}
