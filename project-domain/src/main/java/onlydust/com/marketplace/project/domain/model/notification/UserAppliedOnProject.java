package onlydust.com.marketplace.project.domain.model.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.EventType;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@EventType("UserAppliedOnProject")
public class UserAppliedOnProject extends Event {
    UUID applicationId;
    UUID projectId;
    UUID userId;
    Date appliedAt;
}
