package onlydust.com.marketplace.project.domain.model.event;

import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.event.OnPullRequestMerged;

import java.time.ZonedDateTime;
import java.util.UUID;

@Value
@Builder(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class OnPullRequestMergedTrackingEvent extends Event {
    @NonNull
    Long pullRequestId;
    @NonNull
    Long authorGithubId;
    @NonNull
    UUID authorUserId;
    @NonNull
    ZonedDateTime createdAt;
    @NonNull
    ZonedDateTime mergedAt;
    UUID projectId;

    public static OnPullRequestMergedTrackingEvent of(OnPullRequestMerged onPullRequestMerged, UUID userId, UUID projectId) {
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
