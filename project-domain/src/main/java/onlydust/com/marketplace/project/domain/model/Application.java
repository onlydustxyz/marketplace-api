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
    @NonNull
    final GithubComment.Id commentId;
    @NonNull
    String motivations;
    String problemSolvingApproach;
    ZonedDateTime ignoredAt;

    public Application(final @NonNull Id id,
                       final @NonNull ProjectId projectId,
                       final @NonNull Long applicantId,
                       final @NonNull Origin origin,
                       final @NonNull ZonedDateTime appliedAt,
                       final @NonNull GithubIssue.Id issueId,
                       final @NonNull GithubComment.Id commentId,
                       final @NonNull String motivation,
                       String problemSolvingApproach) {
        this(id, projectId, applicantId, origin, appliedAt, issueId, commentId, motivation, problemSolvingApproach, null);
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
                                              @NonNull GithubIssue.Id issueId,
                                              @NonNull GithubComment.Id commentId,
                                              @NonNull String motivations,
                                              String problemSolvingApproach) {
        return new Application(Id.random(),
                projectId,
                applicantId,
                Origin.MARKETPLACE,
                ZonedDateTime.now(),
                issueId,
                commentId,
                motivations,
                problemSolvingApproach,
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
                null,
                null);
    }

    public void updateMotivations(@NonNull String motivations,
                                  String problemSolvingApproach) {
        this.origin = Origin.MARKETPLACE;
        this.motivations = motivations;
        this.problemSolvingApproach = problemSolvingApproach;
    }

    public void ignore() {
        this.ignoredAt = CurrentDateProvider.now().toInstant().atZone(ZoneOffset.UTC);
    }

    public void unIgnore() {
        this.ignoredAt = null;
    }

    public enum Origin {GITHUB, MARKETPLACE}
}
