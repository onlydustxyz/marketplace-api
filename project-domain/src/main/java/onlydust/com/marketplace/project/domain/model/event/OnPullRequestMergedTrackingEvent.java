package onlydust.com.marketplace.project.domain.model.event;

import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.model.event.OnPullRequestMerged;

import java.time.ZonedDateTime;

@Value
@Builder(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class OnPullRequestMergedTrackingEvent extends Event {
    @NonNull
    Long pullRequestId;
    @NonNull
    Long authorGithubId;
    @NonNull
    UserId authorUserId;
    @NonNull
    ZonedDateTime createdAt;
    @NonNull
    ZonedDateTime mergedAt;
    ProjectId projectId;

    public static OnPullRequestMergedTrackingEvent of(OnPullRequestMerged onPullRequestMerged, UserId userId, ProjectId projectId) {
        return OnPullRequestMergedTrackingEvent.builder()
                .pullRequestId(onPullRequestMerged.id())
                .authorGithubId(onPullRequestMerged.authorId())
                .authorUserId(userId)
                .createdAt(onPullRequestMerged.createdAt())
                .mergedAt(onPullRequestMerged.mergedAt())
                .projectId(projectId)
                .build();
    }
}
