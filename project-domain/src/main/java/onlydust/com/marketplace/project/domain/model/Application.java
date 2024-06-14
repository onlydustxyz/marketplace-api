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
                          @NonNull UUID applicantId,
                          @NonNull ZonedDateTime appliedAt,
                          @NonNull GithubIssue.Id issueId,
                          @NonNull GithubComment.Id commentId,
                          @NonNull String motivations,
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
}
