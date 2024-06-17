package onlydust.com.marketplace.project.domain.model;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.event.OnGithubCommentCreated;
import onlydust.com.marketplace.kernel.model.event.OnGithubCommentEdited;

import java.time.ZonedDateTime;

public record GithubComment(@NonNull Id id,
                            @NonNull GithubIssue.Id issueId,
                            @NonNull Long repoId,
                            @NonNull Long authorId,
                            @NonNull ZonedDateTime updatedAt,
                            @NonNull String body) {

    public record Id(Long value) {
        public static Id of(Long value) {
            return new Id(value);
        }

        public static Id random() {
            return new Id((long) (Math.random() * 1_000_000));
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }

    public static GithubComment of(OnGithubCommentCreated event) {
        return new GithubComment(
                Id.of(event.id()),
                GithubIssue.Id.of(event.issueId()),
                event.repoId(),
                event.authorId(),
                event.createdAt(),
                event.body()
        );
    }

    public static GithubComment of(OnGithubCommentEdited event) {
        return new GithubComment(
                Id.of(event.id()),
                GithubIssue.Id.of(event.issueId()),
                event.repoId(),
                event.authorId(),
                event.updatedAt(),
                event.body()
        );
    }
}
