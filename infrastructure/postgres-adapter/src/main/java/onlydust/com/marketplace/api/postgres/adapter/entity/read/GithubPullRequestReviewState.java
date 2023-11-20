package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import onlydust.com.marketplace.api.domain.view.PullRequestReviewState;

public enum GithubPullRequestReviewState {
    PENDING_REVIEWER, UNDER_REVIEW, APPROVED, CHANGES_REQUESTED;

    public PullRequestReviewState toView() {
        return switch (this) {
            case UNDER_REVIEW -> PullRequestReviewState.UNDER_REVIEW;
            case APPROVED -> PullRequestReviewState.APPROVED;
            case CHANGES_REQUESTED -> PullRequestReviewState.CHANGES_REQUESTED;
            case PENDING_REVIEWER -> PullRequestReviewState.PENDING_REVIEWER;
        };
    }
}
