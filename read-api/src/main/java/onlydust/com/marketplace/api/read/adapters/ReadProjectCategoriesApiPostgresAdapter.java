package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadProjectCategoriesApi;
import onlydust.com.marketplace.api.contract.model.ProjectCategoriesResponse;
import onlydust.com.marketplace.api.read.entities.project.ProjectCategoryReadEntity;
import onlydust.com.marketplace.api.read.repositories.ProjectCategoryReadRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadProjectCategoriesApiPostgresAdapter implements ReadProjectCategoriesApi {
    ProjectCategoryReadRepository projectCategoryReadRepository;

    @Override
    public ResponseEntity<ProjectCategoriesResponse> getAllProjectCategories(String search) {
        final var categories = projectCategoryReadRepository.findAllByNameContainingIgnoreCase(search == null ? "" : search, Sort.by("name"));
        return ResponseEntity.ok(new ProjectCategoriesResponse().categories(categories.stream().map(ProjectCategoryReadEntity::toDto).toList()));
    }
}
