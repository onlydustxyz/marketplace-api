package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.ProjectCategory;
import onlydust.com.marketplace.project.domain.model.ProjectCategorySuggestion;

import java.util.UUID;

public interface ProjectCategoryFacadePort {
    void suggest(final @NonNull String categoryName, final @NonNull UUID userId);

    void deleteCategorySuggestion(final @NonNull ProjectCategorySuggestion.Id id);

    ProjectCategory createCategory(final @NonNull String categoryName, final @NonNull String iconSlug);

    ProjectCategory updateCategory(final @NonNull ProjectCategory.Id id, final @NonNull String name, final @NonNull String iconSlug);

    void deleteCategory(ProjectCategory.Id id);
}
