package onlydust.com.marketplace.project.domain.view;

import lombok.NonNull;

import java.time.ZonedDateTime;
import java.util.List;

public record GithubIssueView(
        @NonNull Long id,
        @NonNull Long number,
        @NonNull String title,
        @NonNull Status status,
        @NonNull ZonedDateTime createdAt,
        ZonedDateTime closedAt,
        @NonNull String htmlUrl,
        String body,
        @NonNull ContributorLinkView author,
        @NonNull ShortRepoView repository,
        @NonNull Integer commentsCount,
        @NonNull List<String> labels
) {
    public enum Status {
        OPEN, COMPLETED, CANCELLED
    }
}
