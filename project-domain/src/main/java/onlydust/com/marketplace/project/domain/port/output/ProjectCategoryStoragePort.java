package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.project.domain.model.ProjectCategory;
import onlydust.com.marketplace.project.domain.model.ProjectCategorySuggestion;

import java.util.Optional;

public interface ProjectCategoryStoragePort {
    void save(ProjectCategorySuggestion suggestion);

    void delete(ProjectCategorySuggestion.Id id);

    void save(ProjectCategory projectCategory);

    Optional<ProjectCategory> get(ProjectCategory.Id id);

    void delete(ProjectCategory.Id id);
}
