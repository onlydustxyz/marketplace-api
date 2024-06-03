package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.project.domain.model.ProjectCategorySuggestion;

import java.util.UUID;

public interface ProjectCategoryFacadePort {
    void suggest(String categoryName, UUID userId);

    void deleteCategorySuggestion(ProjectCategorySuggestion.Id id);
}
