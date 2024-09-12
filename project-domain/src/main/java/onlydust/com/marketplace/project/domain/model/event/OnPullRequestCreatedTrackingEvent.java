package onlydust.com.marketplace.project.domain.model.event;

import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.model.event.OnPullRequestCreated;

import java.time.ZonedDateTime;

@Value
@Builder(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class OnPullRequestCreatedTrackingEvent extends Event {
    @NonNull
    Long pullRequestId;
    @NonNull
    Long authorGithubId;
    @NonNull
    UserId authorUserId;
    @NonNull
    ZonedDateTime createdAt;
    ProjectId projectId;

    public static OnPullRequestCreatedTrackingEvent of(OnPullRequestCreated onPullRequestCreated, UserId userId, ProjectId projectId) {
        return OnPullRequestCreatedTrackingEvent.builder()
                .pullRequestId(onPullRequestCreated.id())
                .authorGithubId(onPullRequestCreated.authorId())
                .authorUserId(userId)
                .createdAt(onPullRequestCreated.createdAt())
                .projectId(projectId)
                .build();
    }
}
