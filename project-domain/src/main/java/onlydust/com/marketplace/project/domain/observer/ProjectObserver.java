package onlydust.com.marketplace.project.domain.observer;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;
import onlydust.com.marketplace.project.domain.model.notification.ProjectCreated;
import onlydust.com.marketplace.project.domain.model.notification.ProjectLinkedReposChanged;
import onlydust.com.marketplace.project.domain.model.notification.UserAppliedOnProject;
import onlydust.com.marketplace.project.domain.port.input.ProjectObserverPort;
import onlydust.com.marketplace.project.domain.port.output.ContributionStoragePort;

import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
public class ProjectObserver implements ProjectObserverPort {

    private final ContributionStoragePort contributionStoragePort;
    private final OutboxPort indexerOutbox;
    private final NotificationPort notificationPort;

    @Override
    public void onLinkedReposChanged(UUID projectId, Set<Long> linkedRepoIds, Set<Long> unlinkedRepoIds) {
        contributionStoragePort.refreshIgnoredContributions(projectId);
        indexerOutbox.push(new ProjectLinkedReposChanged(projectId, linkedRepoIds, unlinkedRepoIds));
    }

    @Override
    public void onRewardSettingsChanged(UUID projectId) {
        contributionStoragePort.refreshIgnoredContributions(projectId);
    }

    @Override
    public void onUserApplied(UUID projectId, UUID userId, UUID applicationId) {
        notificationPort.notify(new UserAppliedOnProject(applicationId, projectId, userId));
    }

    @Override
    public void onProjectCreated(UUID projectId, UUID projectLeadId) {
        notificationPort.notify(new ProjectCreated(projectId, projectLeadId));
    }
}
