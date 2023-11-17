package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import onlydust.com.marketplace.api.domain.model.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@EqualsAndHashCode
public abstract class ContributionBaseView {
    public abstract ContributionType getType();
    public abstract List<CodeReviewState> getCodeReviewStates();

    public Optional<PullRequestReviewState> getGithubPullRequestReviewState() {
        if (getType().equals(ContributionType.PULL_REQUEST)) {
            final var codeReviewStates = Optional.ofNullable(getCodeReviewStates()).orElse(List.of());
            if (codeReviewStates.contains(CodeReviewState.CHANGES_REQUESTED)) {
                return Optional.of(PullRequestReviewState.CHANGES_REQUESTED);
            }

            if (codeReviewStates.contains(CodeReviewState.APPROVED)) {
                return Optional.of(PullRequestReviewState.APPROVED);
            }

            return Optional.of(codeReviewStates.isEmpty() ? PullRequestReviewState.PENDING_REVIEWER : PullRequestReviewState.UNDER_REVIEW);
        }

        return Optional.empty();
    }
}
