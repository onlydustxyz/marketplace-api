package onlydust.com.marketplace.project.domain.model.event;

import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.model.event.OnGithubIssueAssigned;

import java.time.ZonedDateTime;
import java.util.Optional;

@Value
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@Builder(access = AccessLevel.PRIVATE)
public class OnGithubIssueAssignedTrackingEvent extends Event {
    @NonNull
    Long issueId;
    @NonNull
    Long assigneeGithubId;
    UserId assigneeUserId;
    @NonNull
    ZonedDateTime createdAt;
    @NonNull
    ZonedDateTime assignedAt;
    boolean isGoodFirstIssue;
    ProjectId projectId;

    public static OnGithubIssueAssignedTrackingEvent of(@NonNull OnGithubIssueAssigned onGithubIssueAssigned,
                                                        @NonNull Optional<UserId> userId, ProjectId projectId) {
        return OnGithubIssueAssignedTrackingEvent.builder()
                .issueId(onGithubIssueAssigned.id())
                .assigneeGithubId(onGithubIssueAssigned.assigneeId())
                .assigneeUserId(userId.orElse(null))
                .createdAt(onGithubIssueAssigned.createdAt())
                .assignedAt(onGithubIssueAssigned.assignedAt())
                .projectId(projectId)
                .isGoodFirstIssue(onGithubIssueAssigned.labels().stream().anyMatch(OnGithubIssueAssignedTrackingEvent::isGoodFirstIssue))
                .build();
    }

    private static boolean isGoodFirstIssue(String label) {
        return label.matches("(?i)(.*)good(.*)first(.*)issue(.*)");
    }
}
