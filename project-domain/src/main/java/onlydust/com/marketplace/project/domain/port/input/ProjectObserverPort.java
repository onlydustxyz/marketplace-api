package onlydust.com.marketplace.project.domain.port.input;

import java.util.Set;
import java.util.UUID;

public interface ProjectObserverPort {
    void onLinkedReposChanged(UUID projectId, Set<Long> linkedRepoIds, Set<Long> unlinkedRepoIds);

    void onRewardSettingsChanged(UUID projectId);

    void onProjectCreated(UUID projectId, UUID projectLeadId);

    void onProjectCategorySuggested(String categoryName, UUID userId);
}
