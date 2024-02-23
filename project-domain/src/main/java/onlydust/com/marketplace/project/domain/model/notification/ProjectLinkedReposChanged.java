package onlydust.com.marketplace.project.domain.model.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.EventType;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@EventType("ProjectLinkedReposChanged")
public class ProjectLinkedReposChanged extends Event {
    UUID projectId;
    java.util.Set<Long> linkedRepoIds;
    Set<Long> unlinkedRepoIds;
}
