package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.domain.view.ProjectContributorsLinkView;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;
import org.junit.jupiter.api.Test;

import static onlydust.com.marketplace.api.postgres.adapter.repository.CustomContributorRepository.GET_CONTRIBUTORS_FOR_PROJECT;
import static onlydust.com.marketplace.api.postgres.adapter.repository.CustomContributorRepository.buildQuery;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomContributorRepositoryTest {

    private static final String GET_CONTRIBUTORS_FOR_PROJECT_WITH_DEFAULT_SORT =
            GET_CONTRIBUTORS_FOR_PROJECT.replace("%order_by%", "login asc");

    @Test
    void should_build_query_given_a_pagination() {
        // When
        final String query = buildQuery(null, null);

        // Then
        assertEquals(GET_CONTRIBUTORS_FOR_PROJECT_WITH_DEFAULT_SORT, query);
    }

    @Test
    void should_build_query_given_a_sort_by_contribution_count() {
        // Given
        final ProjectContributorsLinkView.SortBy sortBy = ProjectContributorsLinkView.SortBy.contributionCount;

        // When
        final String query = buildQuery(sortBy, SortDirection.desc);

        // Then
        assertEquals(GET_CONTRIBUTORS_FOR_PROJECT.replace("%order_by%", "contribution_count desc, login asc")
                , query);
    }

    @Test
    void should_build_query_given_a_sort_by_earned() {
        // Given
        final ProjectContributorsLinkView.SortBy sortBy = ProjectContributorsLinkView.SortBy.earned;

        // When
        final String query = buildQuery(sortBy, SortDirection.asc);

        // Then
        assertEquals(GET_CONTRIBUTORS_FOR_PROJECT
                        .replace("%order_by%", "earned asc, login asc")
                , query);
    }

    @Test
    void should_build_query_given_a_sort_by_to_reward_count() {
        // Given
        final ProjectContributorsLinkView.SortBy sortBy = ProjectContributorsLinkView.SortBy.toRewardCount;

        // When
        final String query = buildQuery(sortBy, SortDirection.desc);

        // Then
        assertEquals(GET_CONTRIBUTORS_FOR_PROJECT
                        .replace("%order_by%", "to_reward_count desc, login asc")
                , query);
    }

    @Test
    void should_build_query_given_a_sort_by_reward_count() {
        // Given
        final ProjectContributorsLinkView.SortBy sortBy = ProjectContributorsLinkView.SortBy.rewardCount;

        // When
        final String query = buildQuery(sortBy, SortDirection.asc);

        // Then
        assertEquals(GET_CONTRIBUTORS_FOR_PROJECT
                        .replace("%order_by%", "reward_count asc, login asc")
                , query);
    }

    @Test
    void should_build_query_given_a_sort_by_login() {
        // Given
        final ProjectContributorsLinkView.SortBy sortBy = ProjectContributorsLinkView.SortBy.login;

        // When
        final String query = buildQuery(sortBy, SortDirection.desc);

        // Then
        assertEquals(GET_CONTRIBUTORS_FOR_PROJECT
                        .replace("%order_by%", "login desc")
                , query);
    }


}
