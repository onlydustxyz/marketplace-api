package onlydust.com.marketplace.api.rest.api.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeProjectCategoriesApi;
import onlydust.com.backoffice.api.contract.model.ProjectCategoryCreateRequest;
import onlydust.com.backoffice.api.contract.model.ProjectCategoryResponse;
import onlydust.com.backoffice.api.contract.model.ProjectCategoryUpdateRequest;
import onlydust.com.marketplace.project.domain.model.ProjectCategory;
import onlydust.com.marketplace.project.domain.model.ProjectCategorySuggestion;
import onlydust.com.marketplace.project.domain.port.input.ProjectCategoryFacadePort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.map;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
public class BackofficeProjectCategoriesRestApi implements BackofficeProjectCategoriesApi {
    private final ProjectCategoryFacadePort projectCategoryFacadePort;

    @Override
    public ResponseEntity<ProjectCategoryResponse> createProjectCategory(ProjectCategoryCreateRequest request) {
        final var projectCategory = projectCategoryFacadePort.createCategory(
                request.getName(),
                request.getIconSlug(),
                request.getSuggestionId() == null ? null : ProjectCategorySuggestion.Id.of(request.getSuggestionId()));

        return ok(map(projectCategory));
    }

    @Override
    public ResponseEntity<Void> deleteProjectCategory(UUID id) {
        projectCategoryFacadePort.deleteCategory(ProjectCategory.Id.of(id));
        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> deleteProjectCategorySuggestion(UUID id) {
        projectCategoryFacadePort.deleteCategorySuggestion(ProjectCategorySuggestion.Id.of(id));
        return noContent().build();
    }

    @Override
    public ResponseEntity<ProjectCategoryResponse> updateProjectCategory(UUID id, ProjectCategoryUpdateRequest request) {
        final var projectCategory = projectCategoryFacadePort.updateCategory(
                ProjectCategory.Id.of(id),
                request.getName(),
                request.getIconSlug());

        return ok(map(projectCategory));
    }
}
