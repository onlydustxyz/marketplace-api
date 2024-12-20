package onlydust.com.marketplace.project.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UuidWrapper;
import onlydust.com.marketplace.project.domain.gateway.CurrentDateProvider;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class Application {
    @NonNull
    final Id id;
    @NonNull
    final ProjectId projectId;
    @NonNull
    final Long applicantId;
    @NonNull
    Origin origin;
    @NonNull
    final ZonedDateTime appliedAt;
    @NonNull
    final GithubIssue.Id issueId;
    @Setter
    GithubComment.Id commentId;
    @Setter
    String commentBody;
    ZonedDateTime ignoredAt;

    public Application(final @NonNull Id id,
                       final @NonNull ProjectId projectId,
                       final @NonNull Long applicantId,
                       final @NonNull Origin origin,
                       final @NonNull ZonedDateTime appliedAt,
                       final @NonNull GithubIssue.Id issueId,
                       final GithubComment.Id commentId,
                       final String commentBody) {
        this(id, projectId, applicantId, origin, appliedAt, issueId, commentId, commentBody, null);
    }

    @NoArgsConstructor(staticName = "random")
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    public static class Id extends UuidWrapper {
        public static Id of(@NonNull final UUID uuid) {
            return Id.builder().uuid(uuid).build();
        }

        @JsonCreator
        public static Id of(@NonNull final String uuid) {
            return Id.of(UUID.fromString(uuid));
        }
    }

    public static Application fromMarketplace(@NonNull ProjectId projectId,
                                              @NonNull Long applicantId,
                                              @NonNull GithubIssue.Id issueId) {
        return new Application(Id.random(),
                projectId,
                applicantId,
                Origin.MARKETPLACE,
                CurrentDateProvider.now().toInstant().atZone(ZoneOffset.UTC),
                issueId,
                null,
                null,
                null);
    }

    public static Application fromGithubComment(@NonNull GithubComment comment, @NonNull ProjectId projectId) {
        return new Application(Id.random(),
                projectId,
                comment.authorId(),
                Origin.GITHUB,
                comment.updatedAt(),
                comment.issueId(),
                comment.id(),
                comment.body(),
                null);
    }

    public void ignore() {
        this.ignoredAt = CurrentDateProvider.now().toInstant().atZone(ZoneOffset.UTC);
    }

    public void unIgnore() {
        this.ignoredAt = null;
    }

    public enum Origin {GITHUB, MARKETPLACE}
}
