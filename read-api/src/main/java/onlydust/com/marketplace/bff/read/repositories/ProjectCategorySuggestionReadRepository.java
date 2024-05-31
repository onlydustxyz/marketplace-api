package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.project.ProjectCategorySuggestionReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface ProjectCategorySuggestionReadRepository extends Repository<ProjectCategorySuggestionReadEntity, UUID> {
    Page<ProjectCategorySuggestionReadEntity> findAll(Pageable pageable);
}
