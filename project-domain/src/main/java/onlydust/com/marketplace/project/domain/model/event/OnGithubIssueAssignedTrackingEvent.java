package onlydust.com.marketplace.project.domain.model.event;

import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.event.OnGithubIssueAssigned;
import onlydust.com.marketplace.project.domain.model.User;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Value
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@Builder(access = AccessLevel.PRIVATE)
public class OnGithubIssueAssignedTrackingEvent extends Event {
    @NonNull
    Long issueId;
    @NonNull
    Long assigneeGithubId;
    UUID assigneeUserId;
    @NonNull
    ZonedDateTime createdAt;
    @NonNull
    ZonedDateTime assignedAt;
    boolean isGoodFirstIssue;

    public static OnGithubIssueAssignedTrackingEvent of(@NonNull OnGithubIssueAssigned onGithubIssueAssigned,
                                                        @NonNull Optional<User> user) {
        return OnGithubIssueAssignedTrackingEvent.builder()
                .issueId(onGithubIssueAssigned.id())
                .assigneeGithubId(onGithubIssueAssigned.assigneeId())
                .assigneeUserId(user.map(User::getId).orElse(null))
                .createdAt(onGithubIssueAssigned.createdAt())
                .assignedAt(onGithubIssueAssigned.assignedAt())
                .isGoodFirstIssue(onGithubIssueAssigned.labels().stream().anyMatch(OnGithubIssueAssignedTrackingEvent::isGoodFirstIssue))
                .build();
    }

    private static boolean isGoodFirstIssue(String label) {
        return label.matches("(?i)(.*)good(.*)first(.*)issue(.*)");
    }
}
