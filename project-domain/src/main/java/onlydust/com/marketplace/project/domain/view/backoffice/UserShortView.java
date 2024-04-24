package onlydust.com.marketplace.project.domain.view.backoffice;

import lombok.*;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Value
@Builder
@EqualsAndHashCode
@Accessors(fluent = true)
public class UserShortView {
    UUID id;
    Long githubUserId;
    String login;
    String avatarUrl;
    String email;
    ZonedDateTime lastSeenAt;
    ZonedDateTime signedUpAt;

    @Value
    @Builder
    @EqualsAndHashCode
    @Getter(AccessLevel.NONE)
    public static class Filters {
        String loginLike;

        public Optional<String> loginLike() {
            return Optional.ofNullable(loginLike);
        }
    }
}
