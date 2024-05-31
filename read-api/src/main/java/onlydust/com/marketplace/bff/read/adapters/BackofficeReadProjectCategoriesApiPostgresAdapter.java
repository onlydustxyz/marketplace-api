package onlydust.com.marketplace.bff.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeReadProjectCategoriesApi;
import onlydust.com.backoffice.api.contract.model.ProjectCategorySuggestionsResponse;
import onlydust.com.marketplace.bff.read.entities.project.ProjectCategorySuggestionReadEntity;
import onlydust.com.marketplace.bff.read.repositories.ProjectCategorySuggestionReadRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
public class BackofficeReadProjectCategoriesApiPostgresAdapter implements BackofficeReadProjectCategoriesApi {
    ProjectCategorySuggestionReadRepository projectCategorySuggestionReadRepository;

    @Override
    public ResponseEntity<ProjectCategorySuggestionsResponse> getProjectCategorySuggestions() {
        final var page = projectCategorySuggestionReadRepository.findAll(PageRequest.of(0, 10));
        return ResponseEntity.ok(new ProjectCategorySuggestionsResponse()
                .categories(page.getContent().stream().map(ProjectCategorySuggestionReadEntity::toDto).toList())
        );
    }
}
