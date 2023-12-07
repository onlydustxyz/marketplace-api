package onlydust.com.marketplace.api.domain.model.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProjectLinkedReposChanged extends Event {
    UUID projectId;
    java.util.Set<Long> linkedRepoIds;
    Set<Long> unlinkedRepoIds;
}
