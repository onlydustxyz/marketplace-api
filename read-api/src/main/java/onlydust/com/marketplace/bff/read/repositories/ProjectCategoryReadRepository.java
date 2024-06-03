package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.project.ProjectCategoryReadEntity;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface ProjectCategoryReadRepository extends Repository<ProjectCategoryReadEntity, UUID> {
    Optional<ProjectCategoryReadEntity> findById(UUID Id);
}
