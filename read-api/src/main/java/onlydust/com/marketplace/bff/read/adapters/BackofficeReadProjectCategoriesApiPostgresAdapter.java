package onlydust.com.marketplace.bff.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeReadProjectCategoriesApi;
import onlydust.com.backoffice.api.contract.model.ProjectCategoryPageResponse;
import onlydust.com.marketplace.bff.read.entities.project.ProjectCategoryPageItemReadEntity;
import onlydust.com.marketplace.bff.read.repositories.ProjectCategoryPageItemReadRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.nextPageIndex;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
public class BackofficeReadProjectCategoriesApiPostgresAdapter implements BackofficeReadProjectCategoriesApi {
    ProjectCategoryPageItemReadRepository projectCategoryPageItemReadRepository;

    @Override
    public ResponseEntity<ProjectCategoryPageResponse> getProjectCategories(Integer pageIndex, Integer pageSize) {
        final var page = projectCategoryPageItemReadRepository.findAll(PageRequest.of(pageIndex, pageSize, Sort.by("name")));
        return ResponseEntity.ok(new ProjectCategoryPageResponse()
                .categories(page.getContent().stream().map(ProjectCategoryPageItemReadEntity::toDto).toList())
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .hasMore(hasMore(pageIndex, page.getTotalPages()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPages())));
    }
}