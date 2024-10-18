package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadRewardsApi;
import onlydust.com.marketplace.api.contract.model.PageableRewardsQueryParams;
import onlydust.com.marketplace.api.contract.model.RewardPageResponse;
import onlydust.com.marketplace.api.read.repositories.RewardReadV2Repository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.nextPageIndex;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadRewardsPostgresAdapter implements ReadRewardsApi {
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final RewardReadV2Repository rewardReadV2Repository;

    @Override
    public ResponseEntity<RewardPageResponse> getRewards(PageableRewardsQueryParams q) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        final var page = rewardReadV2Repository.findAll(
                authenticatedUser.projectsLed() == null ? null : authenticatedUser.projectsLed().toArray(UUID[]::new),
                authenticatedUser.githubUserId(),
                q.getProjectIds() == null ? null : q.getProjectIds().toArray(UUID[]::new),
                q.getContributionUUIDs() == null ? null : q.getContributionUUIDs().toArray(UUID[]::new),
                q.getRecipientIds() == null ? null : q.getRecipientIds().toArray(Long[]::new),
                PageRequest.of(q.getPageIndex(), q.getPageSize(), Sort.by(Sort.Order.desc("requested_at")))
        );

        return ok(new RewardPageResponse()
                .rewards(page.stream().map(r -> r.toDto(authenticatedUser)).toList())
                .hasMore(hasMore(q.getPageIndex(), page.getTotalPages()))
                .nextPageIndex(nextPageIndex(q.getPageIndex(), page.getTotalPages()))
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages()));
    }
}
