package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.project.domain.model.Application;

import java.util.Set;
import java.util.UUID;

public interface ProjectObserverPort {
    void onLinkedReposChanged(UUID projectId, Set<Long> linkedRepoIds, Set<Long> unlinkedRepoIds);

    void onRewardSettingsChanged(UUID projectId);

    void onUserApplied(UUID projectId, Long githubUserId, Application.Id applicationId);

    void onProjectCreated(UUID projectId, UUID projectLeadId);

    void onProjectCategorySuggested(String categoryName, UUID userId);
}
