package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadEcosystemsApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.entities.LanguageReadEntity;
import onlydust.com.marketplace.api.read.entities.ecosystem.EcosystemReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectCategoryReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectEcosystemCardReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectPageItemQueryEntity;
import onlydust.com.marketplace.api.read.repositories.*;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static onlydust.com.marketplace.api.postgres.adapter.mapper.PaginationMapper.getPostgresOffsetFromPagination;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.*;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.PARTIAL_CONTENT;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadEcosystemsApiPostgresAdapter implements ReadEcosystemsApi {
    public static final String SORT_BY_TOTAL_EARNED = "TOTAL_EARNED";
    final EcosystemContributorPageItemEntityRepository ecosystemContributorPageItemEntityRepository;
    private final ProjectEcosystemCardReadEntityRepository projectEcosystemCardReadEntityRepository;

    private final EcosystemReadRepository ecosystemReadRepository;
    private final LanguageReadRepository languageReadRepository;
    private final ProjectCategoryReadRepository projectCategoryReadRepository;

    @Override
    public ResponseEntity<EcosystemProjectPageResponse> getEcosystemProjects(String ecosystemSlug, Boolean hasGoodFirstIssues, Boolean featuredOnly,
                                                                             Integer pageIndex, Integer pageSize, EcosystemProjectsSortBy sortBy,
                                                                             ProjectTag tag) {
        final int sanitizePageIndex = sanitizePageIndex(pageIndex);
        final int sanitizePageSize = sanitizePageSize(pageSize);
        final String tagJsonPath = Optional.ofNullable(tag).map(Enum::name).map(List::of).map(ProjectPageItemQueryEntity::getTagsJsonPath).orElse(null);
        final List<ProjectEcosystemCardReadEntity> projects = projectEcosystemCardReadEntityRepository.findAllBy(ecosystemSlug,
                hasGoodFirstIssues, featuredOnly, getPostgresOffsetFromPagination(sanitizePageSize, sanitizePageIndex), sanitizePageSize,
                Optional.ofNullable(sortBy).map(Enum::name).orElse(null), tagJsonPath
        );

        final int projectsCount = projectEcosystemCardReadEntityRepository.countAllBy(ecosystemSlug, hasGoodFirstIssues, tagJsonPath);
        final int totalNumberOfPage = calculateTotalNumberOfPage(sanitizePageSize, projectsCount);

        final EcosystemProjectPageResponse response = new EcosystemProjectPageResponse()
                .projects(projects.stream().map(ProjectEcosystemCardReadEntity::toContract).toList())
                .hasMore(PaginationHelper.hasMore(sanitizePageIndex, totalNumberOfPage))
                .nextPageIndex(PaginationHelper.nextPageIndex(sanitizePageIndex, totalNumberOfPage))
                .totalItemNumber(projectsCount)
                .totalPageNumber(totalNumberOfPage);

        return response.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response) :
                ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<EcosystemPage> getAllEcosystems(Integer pageIndex, Integer pageSize) {
        final var index = sanitizePageIndex(pageIndex);
        final var size = sanitizePageSize(pageSize);

        final var page = ecosystemReadRepository.findAll(null, PageRequest.of(index, size, Sort.by("name")));

        final var response = new EcosystemPage()
                .ecosystems(page.getContent().stream().map(EcosystemReadEntity::toLinkResponse).toList())
                .hasMore(hasMore(index, page.getTotalPages()))
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .nextPageIndex(nextPageIndex(index, page.getTotalPages()));

        return status(response.getHasMore() ? PARTIAL_CONTENT : OK).body(response);
    }

    @Override
    public ResponseEntity<EcosystemDetailsResponse> getEcosystemBySlug(String slug) {
        final var ecosystem = ecosystemReadRepository.findBySlug(slug)
                .orElseThrow(() -> notFound("Ecosystem %s not found".formatted(slug)));

        return ok(ecosystem.toDetailsResponse());
    }

    @Override
    public ResponseEntity<EcosystemContributorsPage> getEcosystemContributors(String ecosystemSlug, EcosystemContributorsFilter sort, Integer pageIndex,
                                                                              Integer pageSize) {
        final var contributors = SORT_BY_TOTAL_EARNED.equals(sort.name()) ?
                ecosystemContributorPageItemEntityRepository.findByEcosystemSlugOrderByTotalEarnedUsdDesc(ecosystemSlug, PageRequest.of(pageIndex, pageSize)) :
                ecosystemContributorPageItemEntityRepository.findByEcosystemSlugOrderByContributionCountDesc(ecosystemSlug, PageRequest.of(pageIndex,
                        pageSize));

        return ResponseEntity.ok(new EcosystemContributorsPage()
                .hasMore(contributors.hasNext())
                .nextPageIndex(contributors.hasNext() ? pageIndex + 1 : null)
                .totalItemNumber((int) contributors.getTotalElements())
                .totalPageNumber(contributors.getTotalPages())
                .contributors(contributors.stream().map(c -> new EcosystemContributorsPageItemResponse()
                        .githubUserId(c.contributorId())
                        .avatarUrl(c.avatarUrl())
                        .login(c.login())
                        .dynamicRank(SORT_BY_TOTAL_EARNED.equals(sort.name()) ? c.totalEarnedUsdRank() : c.contributionCountRank())
                        .globalRank(c.rank())
                        .globalRankCategory(c.rankCategory())
                        .contributionCount(c.contributionCount())
                        .totalEarnedUsd(c.totalEarnedUsd())
                        .rewardCount(c.rewardCount())
                ).toList()));
    }

    @Override
    public ResponseEntity<EcosystemLanguagesPageResponse> getEcosystemLanguages(String slug, Integer pageIndex, Integer pageSize) {
        final var page = languageReadRepository.findAllByEcosystemSlug(slug, PageRequest.of(pageIndex, pageSize));
        return ResponseEntity.ok(new EcosystemLanguagesPageResponse()
                .hasMore(hasMore(pageIndex, page.getTotalPages()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPages()))
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .languages(page.stream().map(LanguageReadEntity::toDto).toList()
                        .stream().sorted(Comparator.comparing(LanguageResponse::getName)).toList()));
    }

    @Override
    public ResponseEntity<EcosystemProjectCategoriesPageResponse> getEcosystemCategories(String slug, Integer pageIndex, Integer pageSize) {
        final var page = projectCategoryReadRepository.findAllByEcosystemSlug(slug, PageRequest.of(pageIndex, pageSize));
        return ResponseEntity.ok(new EcosystemProjectCategoriesPageResponse()
                .hasMore(hasMore(pageIndex, page.getTotalPages()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPages()))
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .projectCategories(page.stream().map(ProjectCategoryReadEntity::toDto).toList()));
    }

    @Override
    public ResponseEntity<EcosystemPageV2> getEcosystemsPage(Boolean featured, Boolean hidden, Integer pageIndex, Integer pageSize) {
        final var page = featured ?
                ecosystemReadRepository.findAllFeatured(hidden, PageRequest.of(pageIndex, pageSize, Sort.by("featuredRank"))) :
                ecosystemReadRepository.findAll(hidden, PageRequest.of(pageIndex, pageSize, Sort.by("slug")));

        final var response = new EcosystemPageV2()
                .ecosystems(page.getContent().stream().map(EcosystemReadEntity::toPageItemResponse).toList()
                        .stream()
                        .sorted(Comparator.nullsLast(Comparator.comparing(EcosystemPageItemResponse::getProjectCount).reversed()))
                        .toList())
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .hasMore(hasMore(pageIndex, page.getTotalPages()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPages()));
        return response.getHasMore() ? status(HttpStatus.PARTIAL_CONTENT).body(response) : ok(response);
    }
}
