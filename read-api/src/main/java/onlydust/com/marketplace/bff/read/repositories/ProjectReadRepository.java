package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.project.ProjectReadEntity;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface ProjectReadRepository extends Repository<ProjectReadEntity, UUID> {
    Optional<ProjectReadEntity> findById(UUID id);

    Optional<ProjectReadEntity> findBySlug(String slug);
}
