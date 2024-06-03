package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.project.domain.model.ProjectCategorySuggestion;
import onlydust.com.marketplace.project.domain.port.input.ProjectCategoryFacadePort;
import onlydust.com.marketplace.project.domain.port.input.ProjectObserverPort;
import onlydust.com.marketplace.project.domain.port.output.ProjectCategoryStoragePort;

import java.util.UUID;

@AllArgsConstructor
public class ProjectCategoryService implements ProjectCategoryFacadePort {
    private final ProjectObserverPort projectObserverPort;
    private final ProjectCategoryStoragePort projectCategoryStoragePort;

    @Override
    public void suggest(String categoryName, UUID userId) {
        projectCategoryStoragePort.save(ProjectCategorySuggestion.of(categoryName));
        projectObserverPort.onProjectCategorySuggested(categoryName, userId);
    }

    @Override
    public void deleteCategorySuggestion(ProjectCategorySuggestion.Id id) {
        projectCategoryStoragePort.delete(id);
    }
}
