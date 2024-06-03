package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.project.domain.model.ProjectCategorySuggestion;

public interface ProjectCategoryStoragePort {
    void save(ProjectCategorySuggestion suggestion);

    void delete(ProjectCategorySuggestion.Id id);
}
