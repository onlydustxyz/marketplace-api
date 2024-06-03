package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectCategorySuggestionEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectCategorySuggestionRepository;
import onlydust.com.marketplace.project.domain.model.ProjectCategorySuggestion;
import onlydust.com.marketplace.project.domain.port.output.ProjectCategoryStoragePort;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Transactional
public class PostgresProjectCategoryAdapter implements ProjectCategoryStoragePort {
    private final ProjectCategorySuggestionRepository projectCategorySuggestionRepository;

    @Override
    public void save(ProjectCategorySuggestion suggestion) {
        projectCategorySuggestionRepository.save(ProjectCategorySuggestionEntity.fromDomain(suggestion));
    }

    @Override
    public void delete(ProjectCategorySuggestion.Id id) {
        projectCategorySuggestionRepository.deleteById(id.value());
    }
}
