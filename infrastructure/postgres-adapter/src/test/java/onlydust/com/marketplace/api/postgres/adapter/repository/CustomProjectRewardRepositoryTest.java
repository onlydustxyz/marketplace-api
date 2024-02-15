package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.domain.view.ProjectRewardView;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import org.junit.jupiter.api.Test;

import static onlydust.com.marketplace.api.postgres.adapter.repository.CustomProjectRewardRepository.FIND_PROJECT_REWARDS;
import static onlydust.com.marketplace.api.postgres.adapter.repository.CustomProjectRewardRepository.buildQuery;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomProjectRewardRepositoryTest {

    @Test
    void should_build_query_with_default_sort_and_pagination() {
        // Then
        assertEquals(FIND_PROJECT_REWARDS.replace("%order_by%", "requested_at asc"), buildQuery(null,
                SortDirection.asc));
        assertEquals(FIND_PROJECT_REWARDS.replace("%order_by%", "requested_at desc"), buildQuery(null,
                SortDirection.desc));
    }

    @Test
    void should_build_query_with_sort() {
        // Then
        assertEquals(FIND_PROJECT_REWARDS.replace("%order_by%", "requested_at desc"),
                buildQuery(ProjectRewardView.SortBy.requestedAt,
                        SortDirection.desc));
        assertEquals(FIND_PROJECT_REWARDS.replace("%order_by%", "contribution_count asc, requested_at desc"),
                buildQuery(ProjectRewardView.SortBy.contribution,
                        SortDirection.asc));
        assertEquals(FIND_PROJECT_REWARDS.replace("%order_by%", "dollars_equivalent asc, requested_at desc"),
                buildQuery(ProjectRewardView.SortBy.amount,
                        SortDirection.asc));
        assertEquals(FIND_PROJECT_REWARDS.replace("%order_by%", "status asc, requested_at desc"),
                buildQuery(ProjectRewardView.SortBy.status,
                        SortDirection.asc));
    }
}
