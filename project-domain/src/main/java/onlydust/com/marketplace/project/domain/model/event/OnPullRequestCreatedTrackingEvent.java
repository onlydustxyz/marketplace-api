package onlydust.com.marketplace.project.domain.model.event;

import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.event.OnPullRequestCreated;
import onlydust.com.marketplace.project.domain.model.User;

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

    public static OnPullRequestCreatedTrackingEvent of(OnPullRequestCreated onPullRequestCreated, User user) {
        return OnPullRequestCreatedTrackingEvent.builder()
                .pullRequestId(onPullRequestCreated.id())
                .authorGithubId(onPullRequestCreated.authorId())
                .authorUserId(user.getId())
                .createdAt(onPullRequestCreated.createdAt())
                .build();
    }
}
