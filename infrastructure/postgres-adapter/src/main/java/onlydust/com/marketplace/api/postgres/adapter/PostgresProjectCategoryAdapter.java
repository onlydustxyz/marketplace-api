package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectCategoryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectCategorySuggestionEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectCategoryRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectCategorySuggestionRepository;
import onlydust.com.marketplace.project.domain.model.ProjectCategory;
import onlydust.com.marketplace.project.domain.model.ProjectCategorySuggestion;
import onlydust.com.marketplace.project.domain.port.output.ProjectCategoryStoragePort;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@AllArgsConstructor
@Transactional
public class PostgresProjectCategoryAdapter implements ProjectCategoryStoragePort {
    private final ProjectCategorySuggestionRepository projectCategorySuggestionRepository;
    private final ProjectCategoryRepository projectCategoryRepository;

    @Override
    public void save(ProjectCategorySuggestion suggestion) {
        projectCategorySuggestionRepository.save(ProjectCategorySuggestionEntity.fromDomain(suggestion));
    }

    @Override
    public void delete(ProjectCategory.Id id) {
        projectCategoryRepository.deleteById(id.value());
    }

    @Override
    public void delete(ProjectCategorySuggestion.Id id) {
        projectCategorySuggestionRepository.deleteById(id.value());
    }

    @Override
    public void save(ProjectCategory projectCategory) {
        projectCategoryRepository.save(ProjectCategoryEntity.fromDomain(projectCategory));
    }

    @Override
    public Optional<ProjectCategory> get(ProjectCategory.Id id) {
        return projectCategoryRepository.findById(id.value())
                .map(ProjectCategoryEntity::toDomain);
    }
}
