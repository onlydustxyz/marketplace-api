package onlydust.com.marketplace.bff.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeReadProjectCategoriesApi;
import onlydust.com.backoffice.api.contract.model.ProjectCategoryPageResponse;
import onlydust.com.backoffice.api.contract.model.ProjectCategoryResponse;
import onlydust.com.marketplace.bff.read.entities.project.ProjectCategoryPageItemReadEntity;
import onlydust.com.marketplace.bff.read.repositories.ProjectCategoryPageItemReadRepository;
import onlydust.com.marketplace.bff.read.repositories.ProjectCategoryReadRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.nextPageIndex;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
public class BackofficeReadProjectCategoriesApiPostgresAdapter implements BackofficeReadProjectCategoriesApi {
    ProjectCategoryPageItemReadRepository projectCategoryPageItemReadRepository;
    ProjectCategoryReadRepository projectCategoryReadRepository;

    @Override
    public ResponseEntity<ProjectCategoryResponse> getProjectCategory(UUID id) {
        final var projectCategory = projectCategoryReadRepository.findById(id)
                .orElseThrow(() -> notFound("Project category %s not found".formatted(id)));
        return ok(projectCategory.toDto());
    }

    @Override
    public ResponseEntity<ProjectCategoryPageResponse> getProjectCategories(Integer pageIndex, Integer pageSize) {
        final var page = projectCategoryPageItemReadRepository.findAll(PageRequest.of(pageIndex, pageSize, Sort.by("name")));
        final var body = new ProjectCategoryPageResponse()
                .categories(page.getContent().stream().map(ProjectCategoryPageItemReadEntity::toDto).toList())
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .hasMore(hasMore(pageIndex, page.getTotalPages()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPages()));

        return body.getHasMore() ? status(HttpStatus.PARTIAL_CONTENT).body(body) : ok(body);
    }
}