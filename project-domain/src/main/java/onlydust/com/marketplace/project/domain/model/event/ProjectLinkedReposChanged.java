package onlydust.com.marketplace.project.domain.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.EventType;
import onlydust.com.marketplace.kernel.model.ProjectId;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@EventType("ProjectLinkedReposChanged")
public class ProjectLinkedReposChanged extends Event {
    ProjectId projectId;
    java.util.Set<Long> linkedRepoIds;
    Set<Long> unlinkedRepoIds;
}
