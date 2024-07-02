package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.ProjectCategory;
import onlydust.com.marketplace.project.domain.model.ProjectCategorySuggestion;
import onlydust.com.marketplace.project.domain.port.input.ProjectCategoryFacadePort;
import onlydust.com.marketplace.project.domain.port.input.ProjectObserverPort;
import onlydust.com.marketplace.project.domain.port.output.ProjectCategoryStoragePort;

import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.forbidden;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class ProjectCategoryService implements ProjectCategoryFacadePort {
    private final ProjectObserverPort projectObserverPort;
    private final ProjectCategoryStoragePort projectCategoryStoragePort;
    private final PermissionService permissionService;

    @Override
    public void suggest(final @NonNull String categoryName, final @NonNull UUID userId, final @NonNull UUID projectId) {
        if (!permissionService.isUserProjectLead(projectId, userId))
            throw forbidden("Only project leads can suggest project categories");

        projectCategoryStoragePort.save(ProjectCategorySuggestion.of(categoryName, projectId));
        projectObserverPort.onProjectCategorySuggested(categoryName, userId);
    }

    @Override
    public void deleteCategorySuggestion(ProjectCategorySuggestion.Id id) {
        projectCategoryStoragePort.delete(id);
    }

    @Override
    public ProjectCategory createCategory(@NonNull String name, @NonNull String description, @NonNull String iconSlug, final ProjectCategorySuggestion.Id suggestionId) {
        final var projectCategory = ProjectCategory.of(name, description, iconSlug);

        if (suggestionId != null)
            link(suggestionId, projectCategory);

        projectCategoryStoragePort.save(projectCategory);
        return projectCategory;
    }

    @Override
    public ProjectCategory updateCategory(ProjectCategory.@NonNull Id id, @NonNull String name, @NonNull String description, @NonNull String iconSlug,
                                          final ProjectCategorySuggestion.Id suggestionId) {
        final var projectCategory = projectCategoryStoragePort.get(id)
                .orElseThrow(() -> notFound("Project category %s not found".formatted(id)));

        if (suggestionId != null)
            link(suggestionId, projectCategory);

        projectCategoryStoragePort.save(projectCategory
                .name(name)
                .description(description)
                .iconSlug(iconSlug));
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
