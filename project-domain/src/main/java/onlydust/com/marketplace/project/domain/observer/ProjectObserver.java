package onlydust.com.marketplace.project.domain.observer;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;
import onlydust.com.marketplace.project.domain.model.notification.ProjectLinkedReposChanged;
import onlydust.com.marketplace.project.domain.port.input.ProjectObserverPort;

import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
public class ProjectObserver implements ProjectObserverPort {

    private final OutboxPort indexerOutbox;

    @Override
    public void onLinkedReposChanged(UUID projectId, Set<Long> linkedRepoIds, Set<Long> unlinkedRepoIds) {
        indexerOutbox.push(new ProjectLinkedReposChanged(projectId, linkedRepoIds, unlinkedRepoIds));
    }

    @Override
    public void onRewardSettingsChanged(UUID projectId) {
    }

    @Override
    public void onUserApplied(UUID projectId, UUID userId, UUID applicationId) {
    }

    @Override
    public void onProjectCreated(UUID projectId, UUID projectLeadId) {
    }

    @Override
    public void onProjectCategorySuggested(String categoryName, UUID userId) {
    }
}
