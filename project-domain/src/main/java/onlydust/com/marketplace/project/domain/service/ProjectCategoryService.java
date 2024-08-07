package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.ProjectCategory;
import onlydust.com.marketplace.project.domain.model.ProjectCategorySuggestion;
import onlydust.com.marketplace.project.domain.port.input.ProjectCategoryFacadePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectCategoryStoragePort;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class ProjectCategoryService implements ProjectCategoryFacadePort {
    private final ProjectCategoryStoragePort projectCategoryStoragePort;

    @Override
    public void deleteCategorySuggestion(@NonNull ProjectCategorySuggestion.Id id) {
        projectCategoryStoragePort.delete(id);
    }

    @Override
    public ProjectCategory createCategory(@NonNull String name, @NonNull String description, @NonNull String iconSlug,
                                          final ProjectCategorySuggestion.Id suggestionId) {
        final var projectCategory = ProjectCategory.of(name, description, iconSlug);

        if (suggestionId != null)
            link(suggestionId, projectCategory);

        projectCategoryStoragePort.save(projectCategory);
        return projectCategory;
    }

    @Override
    public ProjectCategory updateCategory(@NonNull ProjectCategory.Id id, final String name, final String description, final String iconSlug,
                                          final ProjectCategorySuggestion.Id suggestionId) {
        final var projectCategory = projectCategoryStoragePort.get(id)
                .orElseThrow(() -> notFound("Project category %s not found".formatted(id)));

        if (name != null)
            projectCategory.name(name);

        if (description != null)
            projectCategory.description(description);

        if (iconSlug != null)
            projectCategory.iconSlug(iconSlug);

        if (suggestionId != null)
            link(suggestionId, projectCategory);

        projectCategoryStoragePort.save(projectCategory);
        
        return projectCategory;
    }

    @Override
    public void deleteCategory(ProjectCategory.Id id) {
        projectCategoryStoragePort.delete(id);
    }

    private void link(ProjectCategorySuggestion.Id suggestionId, ProjectCategory projectCategory) {
        final var suggestion = projectCategoryStoragePort.get(suggestionId)
                .orElseThrow(() -> notFound("Project category suggestion %s not found".formatted(suggestionId)));
        projectCategory.projects().add(suggestion.projectId());
        projectCategoryStoragePort.delete(suggestionId);
    }
}
