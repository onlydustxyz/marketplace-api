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
@EventType("ProjectLeaderUnassigned")
public class ProjectLeaderUnassigned extends Event {
    UUID projectId;
    UUID leaderId;
    Date unassignedAt;
}
