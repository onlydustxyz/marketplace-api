package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.ProjectCategory;
import onlydust.com.marketplace.project.domain.model.ProjectCategorySuggestion;

public interface ProjectCategoryFacadePort {
    void deleteCategorySuggestion(final @NonNull ProjectCategorySuggestion.Id id);

    ProjectCategory createCategory(final @NonNull String name, final @NonNull String description, final @NonNull String iconSlug,
                                   final ProjectCategorySuggestion.Id suggestionId);

    ProjectCategory updateCategory(final @NonNull ProjectCategory.Id id, final String name, final String description, final String iconSlug,
                                   final ProjectCategorySuggestion.Id suggestionId);

    void deleteCategory(ProjectCategory.Id id);
}
