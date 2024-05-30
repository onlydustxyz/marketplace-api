package onlydust.com.marketplace.bff.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadEcosystemsApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.bff.read.entities.ecosystem.EcosystemReadEntity;
import onlydust.com.marketplace.bff.read.entities.project.ProjectEcosystemCardReadEntity;
import onlydust.com.marketplace.bff.read.repositories.EcosystemContributorPageItemEntityRepository;
import onlydust.com.marketplace.bff.read.repositories.EcosystemReadRepository;
import onlydust.com.marketplace.bff.read.repositories.ProjectEcosystemCardReadEntityRepository;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static onlydust.com.marketplace.api.postgres.adapter.mapper.PaginationMapper.getPostgresOffsetFromPagination;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.*;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.nextPageIndex;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
public class ReadEcosystemsApiPostgresAdapter implements ReadEcosystemsApi {
    public static final String SORT_BY_TOTAL_EARNED = "TOTAL_EARNED";
    final EcosystemContributorPageItemEntityRepository ecosystemContributorPageItemEntityRepository;
    private final ProjectEcosystemCardReadEntityRepository projectEcosystemCardReadEntityRepository;

    private final EcosystemReadRepository ecosystemReadRepository;

    @Override
    public ResponseEntity<EcosystemProjectPageResponse> getEcosystemProjects(String ecosystemSlug, Integer pageIndex, Integer pageSize,
                                                                             Boolean hasGoodFirstIssues) {
        final int sanitizePageIndex = sanitizePageIndex(pageIndex);
        final int sanitizePageSize = sanitizePageSize(pageSize);
        final List<ProjectEcosystemCardReadEntity> projects = projectEcosystemCardReadEntityRepository.findAllBy(ecosystemSlug,
                hasGoodFirstIssues, getPostgresOffsetFromPagination(sanitizePageSize, sanitizePageIndex), sanitizePageSize
        );

        final int projectsCount = projectEcosystemCardReadEntityRepository.countAllBy(ecosystemSlug, hasGoodFirstIssues);
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
    public ResponseEntity<EcosystemDetailsResponse> getEcosystemBySlug(String slug) {
        final var ecosystem = ecosystemReadRepository.findBySlug(slug)
                .orElseThrow(() -> notFound("Ecosystem %s not found".formatted(slug)));

        return ok(ecosystem.toDetailsResponse());
    }

    @Override
    public ResponseEntity<EcosystemContributorsPage> getEcosystemContributors(String ecosystemSlug, Integer pageIndex, Integer pageSize, String sort) {
        final var contributors = SORT_BY_TOTAL_EARNED.equals(sort) ?
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
                        .dynamicRank(SORT_BY_TOTAL_EARNED.equals(sort) ? c.totalEarnedUsdRank() : c.contributionCountRank())
                        .globalRank(c.rank())
                        .globalRankCategory(c.rankCategory())
                        .contributionCount(c.contributionCount())
                        .totalEarnedUsd(c.totalEarnedUsd())
                        .rewardCount(c.rewardCount())
                ).toList()));
    }

    @Override
    public ResponseEntity<EcosystemPageV2> getEcosystemsPage(Boolean featured, Integer pageIndex, Integer pageSize) {
        final var page = featured ?
                ecosystemReadRepository.findAllFeatured(PageRequest.of(pageIndex, pageSize, Sort.by("featured"))) :
                ecosystemReadRepository.findAll(PageRequest.of(pageIndex, pageSize, Sort.by("slug")));

        final var response = new EcosystemPageV2()
                .ecosystems(page.getContent().stream().map(EcosystemReadEntity::toPageItemResponse).toList())
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .hasMore(hasMore(pageIndex, page.getTotalPages()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPages()));
        return response.getHasMore() ? status(HttpStatus.PARTIAL_CONTENT).body(response) : ok(response);
    }
}
