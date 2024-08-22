package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.project.ProjectLinkReadEntity;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface ProjectLinkReadRepository extends Repository<ProjectLinkReadEntity, UUID> {

    Optional<ProjectLinkReadEntity> findById(UUID id);
}
