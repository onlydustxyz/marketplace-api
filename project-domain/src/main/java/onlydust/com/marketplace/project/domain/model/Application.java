package onlydust.com.marketplace.project.domain.model;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.time.ZonedDateTime;
import java.util.UUID;

public record Application(@NonNull Id id,
                          @NonNull UUID projectId,
                          @NonNull Long applicantId,
                          @NonNull Origin origin,
                          @NonNull ZonedDateTime appliedAt,
                          @NonNull GithubIssue.Id issueId,
                          @NonNull GithubComment.Id commentId,
                          String motivations,
                          String problemSolvingApproach) {

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

    public static Application fromMarketplace(@NonNull UUID projectId,
                                              @NonNull Long applicantId,
                                              @NonNull GithubIssue.Id issueId,
                                              @NonNull GithubComment.Id commentId,
                                              @NonNull String motivations,
                                              String problemSolvingApproach) {
        return new Application(Id.random(), projectId, applicantId, Origin.MARKETPLACE, ZonedDateTime.now(), issueId, commentId, motivations,
                problemSolvingApproach);
    }


    public static Application fromGithubComment(@NonNull GithubComment comment, @NonNull UUID projectId) {
        return new Application(Id.random(), projectId, comment.authorId(), Origin.GITHUB, comment.updatedAt(), comment.issueId(), comment.id(), null, null);
    }

    public Application update(@NonNull String motivations,
                              String problemSolvingApproach) {
        return new Application(id, projectId, applicantId, Origin.MARKETPLACE, appliedAt, issueId, commentId, motivations, problemSolvingApproach);
    }

    public enum Origin {GITHUB, MARKETPLACE}
}
