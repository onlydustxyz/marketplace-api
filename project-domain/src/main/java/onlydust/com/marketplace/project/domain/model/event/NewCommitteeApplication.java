package onlydust.com.marketplace.project.domain.model.event;

import lombok.*;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.EventType;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@EventType("NewCommitteeApplication")
@Builder
public class NewCommitteeApplication extends Event {
    @NonNull
    String projectName;
    @NonNull
    UUID projectId;
    @NonNull
    String email;
    @NonNull
    String githubLogin;
    @NonNull
    UUID userId;
    @NonNull
    String committeeName;
    @NonNull
    UUID committeeId;
    @NonNull
    ZonedDateTime applicationEndDate;
}
