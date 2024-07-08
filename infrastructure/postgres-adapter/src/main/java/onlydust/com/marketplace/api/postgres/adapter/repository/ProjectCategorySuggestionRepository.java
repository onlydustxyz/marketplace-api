package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectCategorySuggestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectCategorySuggestionRepository extends JpaRepository<ProjectCategorySuggestionEntity, UUID> {
    List<ProjectCategorySuggestionEntity> findAllByProjectId(UUID projectId);
}
