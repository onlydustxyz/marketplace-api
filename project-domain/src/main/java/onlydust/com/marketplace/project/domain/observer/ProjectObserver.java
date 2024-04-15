package onlydust.com.marketplace.project.domain.observer;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;
import onlydust.com.marketplace.project.domain.model.notification.ProjectCreated;
import onlydust.com.marketplace.project.domain.model.notification.ProjectLinkedReposChanged;
import onlydust.com.marketplace.project.domain.model.notification.UserAppliedOnProject;
import onlydust.com.marketplace.project.domain.port.input.ProjectObserverPort;
import onlydust.com.marketplace.project.domain.port.output.ContributionStoragePort;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
public class ProjectObserver implements ProjectObserverPort {

    private final OutboxPort notificationOutbox;
    private final ContributionStoragePort contributionStoragePort;
    private final OutboxPort indexerOutbox;

    @Override
    public void onProjectCreated(UUID projectId) {
        notificationOutbox.push(new ProjectCreated(projectId, new Date()));
    }

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
        notificationOutbox.push(new UserAppliedOnProject(applicationId, projectId, userId, new Date()));
    }
}
