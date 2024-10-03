package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.project.ProjectContributorLabelReadEntity;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface ProjectContributorLabelReadRepository extends Repository<ProjectContributorLabelReadEntity, UUID> {
    List<ProjectContributorLabelReadEntity> findAllByProjectId(UUID projectId);

    List<ProjectContributorLabelReadEntity> findAllByProjectSlug(String projectSlug);
}
