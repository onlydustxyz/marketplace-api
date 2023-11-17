package onlydust.com.marketplace.api.postgres.adapter.entity.read;

public enum CodeReviewState {
    APPROVED, CHANGES_REQUESTED, COMMENTED, PENDING, DISMISSED;

    public onlydust.com.marketplace.api.domain.view.CodeReviewState toDomain() {
        return switch (this) {
            case APPROVED -> onlydust.com.marketplace.api.domain.view.CodeReviewState.APPROVED;
            case CHANGES_REQUESTED -> onlydust.com.marketplace.api.domain.view.CodeReviewState.CHANGES_REQUESTED;
            case COMMENTED -> onlydust.com.marketplace.api.domain.view.CodeReviewState.COMMENTED;
            case PENDING -> onlydust.com.marketplace.api.domain.view.CodeReviewState.PENDING;
            case DISMISSED -> onlydust.com.marketplace.api.domain.view.CodeReviewState.DISMISSED;
        };
    }

}
