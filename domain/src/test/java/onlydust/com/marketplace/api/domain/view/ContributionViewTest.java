package onlydust.com.marketplace.api.domain.view;

import onlydust.com.marketplace.api.domain.model.ContributionType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ContributionViewTest {
    @Test
    void should_not_compute_pull_request_review_state_if_not_a_pull_request() {
        assertThat(ContributionView.builder().type(ContributionType.ISSUE).build().getGithubPullRequestReviewState()).isEmpty();
        assertThat(ContributionView.builder().type(ContributionType.CODE_REVIEW).build().getGithubPullRequestReviewState()).isEmpty();
    }

    @Test
    void should_not_compute_pull_request_review_state_for_pull_requests() {
        assertThat(ContributionView.builder()
                .type(ContributionType.PULL_REQUEST)
                .codeReviewStates(List.of(CodeReviewState.CHANGES_REQUESTED, CodeReviewState.COMMENTED, CodeReviewState.APPROVED, CodeReviewState.DISMISSED, CodeReviewState.PENDING))
                .build().getGithubPullRequestReviewState())
                .contains(PullRequestReviewState.CHANGES_REQUESTED);

        assertThat(ContributionView.builder()
                .type(ContributionType.PULL_REQUEST)
                .codeReviewStates(List.of(CodeReviewState.COMMENTED, CodeReviewState.APPROVED, CodeReviewState.DISMISSED, CodeReviewState.PENDING))
                .build().getGithubPullRequestReviewState())
                .contains(PullRequestReviewState.APPROVED);

        assertThat(ContributionView.builder()
                .type(ContributionType.PULL_REQUEST)
                .codeReviewStates(List.of(CodeReviewState.COMMENTED, CodeReviewState.DISMISSED, CodeReviewState.PENDING))
                .build().getGithubPullRequestReviewState())
                .contains(PullRequestReviewState.UNDER_REVIEW);

        assertThat(ContributionView.builder()
                .type(ContributionType.PULL_REQUEST)
                .codeReviewStates(List.of())
                .build().getGithubPullRequestReviewState())
                .contains(PullRequestReviewState.PENDING_REVIEWER);
    }
}