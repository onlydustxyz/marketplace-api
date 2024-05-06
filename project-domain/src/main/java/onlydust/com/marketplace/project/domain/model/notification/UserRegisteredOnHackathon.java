package onlydust.com.marketplace.project.domain.model.notification;

import lombok.*;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.EventType;
import onlydust.com.marketplace.project.domain.model.Hackathon;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@EventType("UserRegisteredOnHackathon")
public class UserRegisteredOnHackathon extends Event {
    @NonNull
    UUID userId;
    @NonNull
    Hackathon.Id hackathonId;
}
