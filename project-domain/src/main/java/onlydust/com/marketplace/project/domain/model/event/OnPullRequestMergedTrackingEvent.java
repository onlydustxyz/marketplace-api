package onlydust.com.marketplace.project.domain.model.event;

import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.event.OnPullRequestMerged;
import onlydust.com.marketplace.project.domain.model.User;

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

    public static OnPullRequestMergedTrackingEvent of(OnPullRequestMerged onPullRequestMerged, User user) {
        return OnPullRequestMergedTrackingEvent.builder()
                .pullRequestId(onPullRequestMerged.id())
                .authorGithubId(onPullRequestMerged.authorId())
                .authorUserId(user.getId())
                .createdAt(onPullRequestMerged.createdAt())
                .mergedAt(onPullRequestMerged.mergedAt())
                .build();
    }
}
