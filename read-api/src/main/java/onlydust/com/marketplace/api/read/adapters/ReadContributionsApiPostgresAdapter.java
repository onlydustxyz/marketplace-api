package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadContributionsApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.entities.bi.ContributionReadEntity;
import onlydust.com.marketplace.api.read.properties.Cache;
import onlydust.com.marketplace.api.read.repositories.ContributionReadRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static onlydust.com.marketplace.api.contract.model.ContributionEventEnum.*;
import static onlydust.com.marketplace.api.read.properties.Cache.XS;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.nextPageIndex;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadContributionsApiPostgresAdapter implements ReadContributionsApi {
    private final Cache cache;
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final ContributionReadRepository contributionReadRepository;

    @Override
    public ResponseEntity<ContributionActivityPageItemResponse> getContributionById(UUID contributionUuid) {
        final var authenticatedUser = authenticatedAppUserService.tryGetAuthenticatedUser();
        final var contribution = findContribution(contributionUuid);

        return ok()
                .cacheControl(cache.whenAnonymous(authenticatedUser, XS))
                .body(contribution.toDto(authenticatedUser));
    }

    private ContributionReadEntity findContribution(UUID contributionUuid) {
        final var page = contributionReadRepository.findAll(new ContributionsQueryParams()
                .pageIndex(0)
                .pageSize(1)
                .ids(List.of(contributionUuid)));

        return page.stream().findFirst()
                .orElseThrow(() -> notFound("Contribution %s not found".formatted(contributionUuid)));
    }

    @Override
    public ResponseEntity<ContributionActivityPageResponse> getContributions(ContributionsQueryParams q) {
        final var authenticatedUser = authenticatedAppUserService.tryGetAuthenticatedUser();
        final var page = contributionReadRepository.findAll(q);

        return ok()
                .cacheControl(cache.whenAnonymous(authenticatedUser, XS))
                .body(new ContributionActivityPageResponse()
                        .contributions(page.stream().map(c -> c.toDto(authenticatedUser)).toList())
                        .hasMore(hasMore(q.getPageIndex(), page.getTotalPages()))
                        .nextPageIndex(nextPageIndex(q.getPageIndex(), page.getTotalPages()))
                        .totalItemNumber((int) page.getTotalElements())
                        .totalPageNumber(page.getTotalPages()));
    }

    @Override
    public ResponseEntity<ContributionEventListResponse> getContributionEvents(UUID contributionUuid) {
        final var contribution = findContribution(contributionUuid);
        final List<ContributionEventResponse> events = new ArrayList<>();

        switch (contribution.contributionType()) {
            case ISSUE -> {
                events.add(new ContributionEventResponse()
                        .timestamp(contribution.createdAt())
                        .type(ISSUE_CREATED));
                if (contribution.contributors() != null) {
                    contribution.contributors().forEach(a -> events.add(new ContributionEventResponse()
                            .timestamp(a.getSince())
                            .type(ISSUE_ASSIGNED)
                            .assignee(a)));
                }
                events.add(new ContributionEventResponse()
                        .timestamp(contribution.completedAt())
                        .type(ISSUE_CLOSED));
            }
            case PULL_REQUEST -> {
                events.add(new ContributionEventResponse()
                        .timestamp(contribution.createdAt())
                        .type(PR_CREATED));
                Optional.ofNullable(contribution.mergedBy()).ifPresent(c -> events.add(new ContributionEventResponse()
                        .timestamp(contribution.completedAt())
                        .type(PR_MERGED)
                        .mergedBy(c)));
                if (contribution.linkedIssues() != null) {
                    contribution.linkedIssues().forEach(c -> {
                        final var linkedIssue = findContribution(c.getContributionUuid());
                        events.add(new ContributionEventResponse()
                                .timestamp(linkedIssue.createdAt())
                                .linkedIssueContributionUuid(linkedIssue.contributionUuid())
                                .type(LINKED_ISSUE_CREATED));
                        if (linkedIssue.contributors() != null) {
                            linkedIssue.contributors().forEach(a -> events.add(new ContributionEventResponse()
                                    .timestamp(a.getSince())
                                    .linkedIssueContributionUuid(linkedIssue.contributionUuid())
                                    .type(LINKED_ISSUE_ASSIGNED)
                                    .assignee(a)));
                        }
                        events.add(new ContributionEventResponse()
                                .timestamp(linkedIssue.completedAt())
                                .linkedIssueContributionUuid(linkedIssue.contributionUuid())
                                .type(LINKED_ISSUE_CLOSED));
                    });
                }
            }
            case CODE_REVIEW -> {
                return ok(new ContributionEventListResponse().events(List.of()));
            }
        }

        return ok()
                .cacheControl(cache.forEverybody(XS))
                .body(new ContributionEventListResponse()
                        .events(events.stream()
                                .filter(e -> e.getTimestamp() != null)
                                .sorted(Comparator.comparing(ContributionEventResponse::getTimestamp).reversed())
                                .toList()));
    }
}
