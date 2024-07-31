package onlydust.com.marketplace.project.domain.model.event;

import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.event.OnPullRequestCreated;

import java.time.ZonedDateTime;
import java.util.UUID;

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
    UUID authorUserId;
    @NonNull
    ZonedDateTime createdAt;

    public static OnPullRequestCreatedTrackingEvent of(OnPullRequestCreated onPullRequestCreated, UUID userId) {
        return OnPullRequestCreatedTrackingEvent.builder()
                .pullRequestId(onPullRequestCreated.id())
                .authorGithubId(onPullRequestCreated.authorId())
                .authorUserId(userId)
                .createdAt(onPullRequestCreated.createdAt())
                .build();
    }
}
