package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadContributionsApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.entities.bi.ContributionReadEntity;
import onlydust.com.marketplace.api.read.repositories.ContributionReadRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.nextPageIndex;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadContributionsApiPostgresAdapter implements ReadContributionsApi {
    private final ContributionReadRepository contributionReadRepository;

    @Override
    public ResponseEntity<ContributionActivityPageItemResponse> getContributionById(UUID contributionUuid) {
        final var page = contributionReadRepository.findAll(new ContributionsQueryParams()
                .pageIndex(0)
                .pageSize(1)
                .ids(List.of(contributionUuid)));

        final var contribution = page.stream().findFirst()
                .orElseThrow(() -> notFound("Contribution %s not found".formatted(contributionUuid)));

        return ok(contribution.toDto());
    }

    @Override
    public ResponseEntity<ContributionEventListResponse> getContributionEvents(String contributionId) {
        return ok(new ContributionEventListResponse()
                .events(List.of(
                                new ContributionEventResponse()
                                        .timestamp(ZonedDateTime.now().minusDays(2).minusHours(1).minusMinutes(5))
                                        .assigneeAdded(new ContributionEventResponseAssigneeAdded()
                                                .assignee(new GithubUserResponse()
                                                        .avatarUrl("https://avatars.githubusercontent.com/u/43467246?v=4")
                                                        .githubUserId(1L)
                                                        .login("Antho")
                                                )),
                                new ContributionEventResponse()
                                        .timestamp(ZonedDateTime.now().minusDays(2).minusHours(1).minusMinutes(5))
                                        .opened(new ContributionEventResponseOpened()
                                                .by(new GithubUserResponse()
                                                        .avatarUrl("https://avatars.githubusercontent.com/u/43467246?v=4")
                                                        .githubUserId(1L)
                                                        .login("Antho")
                                                ))
                        )
                ));
    }

    @Override
    public ResponseEntity<ContributionActivityPageResponse> getContributions(ContributionsQueryParams q) {
        final var page = contributionReadRepository.findAll(q);

        return ok(new ContributionActivityPageResponse()
                .contributions(page.stream().map(ContributionReadEntity::toDto).toList())
                .hasMore(hasMore(q.getPageIndex(), page.getTotalPages()))
                .nextPageIndex(nextPageIndex(q.getPageIndex(), page.getTotalPages()))
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages()));
    }
}
