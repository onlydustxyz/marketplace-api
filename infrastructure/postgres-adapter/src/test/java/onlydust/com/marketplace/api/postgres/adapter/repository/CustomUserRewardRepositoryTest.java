package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.project.domain.view.UserRewardView;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import org.junit.jupiter.api.Test;

import static onlydust.com.marketplace.api.postgres.adapter.repository.CustomUserRewardRepository.FIND_USER_REWARDS_BY_ID;
import static onlydust.com.marketplace.api.postgres.adapter.repository.CustomUserRewardRepository.buildQuery;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomUserRewardRepositoryTest {

    @Test
    void should_build_query_with_default_sort_and_pagination() {
        // Then
        assertEquals(FIND_USER_REWARDS_BY_ID.replace("%order_by%", "requested_at asc"), buildQuery(null,
                SortDirection.asc));
        assertEquals(FIND_USER_REWARDS_BY_ID.replace("%order_by%", "requested_at desc"), buildQuery(null,
                SortDirection.desc));
    }

    @Test
    void should_build_query_with_sort() {
        // Then
        assertEquals(FIND_USER_REWARDS_BY_ID.replace("%order_by%", "requested_at desc"),
                buildQuery(UserRewardView.SortBy.requestedAt,
                        SortDirection.desc));
        assertEquals(FIND_USER_REWARDS_BY_ID.replace("%order_by%", "contribution_count asc, requested_at desc"),
                buildQuery(UserRewardView.SortBy.contribution,
                        SortDirection.asc));
        assertEquals(FIND_USER_REWARDS_BY_ID.replace("%order_by%", "dollars_equivalent asc nulls last, requested_at desc"),
                buildQuery(UserRewardView.SortBy.amount,
                        SortDirection.asc));
        assertEquals(FIND_USER_REWARDS_BY_ID.replace("%order_by%", "status asc, requested_at desc"),
                buildQuery(UserRewardView.SortBy.status,
                        SortDirection.asc));
    }
}
