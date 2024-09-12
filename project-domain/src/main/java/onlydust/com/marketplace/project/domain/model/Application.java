package onlydust.com.marketplace.project.domain.model;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class Application {
    @NonNull
    private final Id id;
    @NonNull
    private final ProjectId projectId;
    @NonNull
    private final Long applicantId;
    @NonNull
    private final Origin origin;
    @NonNull
    private final ZonedDateTime appliedAt;
    @NonNull
    private final GithubIssue.Id issueId;
    @NonNull
    private final GithubComment.Id commentId;
    @NonNull
    private final String motivations;
    private final String problemSolvingApproach;

    @NoArgsConstructor(staticName = "random")
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    public static class Id extends UuidWrapper {
        public static Id of(@NonNull final UUID uuid) {
            return Id.builder().uuid(uuid).build();
        }

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
                problemSolvingApproach);
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

    public Application update(@NonNull String motivations,
                              String problemSolvingApproach) {
        return new Application(id,
                projectId,
                applicantId,
                Origin.MARKETPLACE,
                appliedAt,
                issueId,
                commentId,
                motivations,
                problemSolvingApproach);
    }

    public enum Origin {GITHUB, MARKETPLACE}
}
