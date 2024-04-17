package onlydust.com.marketplace.project.domain.port.input;

import java.util.Set;
import java.util.UUID;

public interface ProjectObserverPort {
    void onLinkedReposChanged(UUID projectId, Set<Long> linkedRepoIds, Set<Long> unlinkedRepoIds);

    void onRewardSettingsChanged(UUID projectId);

    void onUserApplied(UUID projectId, UUID userId, UUID applicationId);

    void onProjectCreated(UUID projectId, UUID projectLeadId);
}
