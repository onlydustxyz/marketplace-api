package onlydust.com.marketplace.bff.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadProjectCategoriesApi;
import onlydust.com.marketplace.api.contract.model.ProjectCategoriesResponse;
import onlydust.com.marketplace.bff.read.entities.project.ProjectCategoryReadEntity;
import onlydust.com.marketplace.bff.read.repositories.ProjectCategoryReadRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
public class ReadProjectCategoriesApiPostgresAdapter implements ReadProjectCategoriesApi {
    ProjectCategoryReadRepository projectCategoryReadRepository;

    @Override
    public ResponseEntity<ProjectCategoriesResponse> getAllProjectCategories() {
        final var categories = projectCategoryReadRepository.findAll();
        return ResponseEntity.ok(new ProjectCategoriesResponse().categories(categories.stream().map(ProjectCategoryReadEntity::toDto).toList()));
    }
}
