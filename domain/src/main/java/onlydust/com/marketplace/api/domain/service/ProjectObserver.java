package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.notification.*;
import onlydust.com.marketplace.api.domain.port.input.ProjectObserverPort;
import onlydust.com.marketplace.api.domain.port.output.ContributionStoragePort;
import onlydust.com.marketplace.api.domain.port.output.OutboxPort;

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
    public void onProjectDetailsUpdated(UUID projectId) {
        notificationOutbox.push(new ProjectUpdated(projectId, new Date()));
    }

    @Override
    public void onLeaderAssigned(UUID projectId, UUID leaderId) {
        notificationOutbox.push(new ProjectLeaderAssigned(projectId, leaderId, new Date()));
    }

    @Override
    public void onLeaderUnassigned(UUID projectId, UUID leaderId) {
        notificationOutbox.push(new ProjectLeaderUnassigned(projectId, leaderId, new Date()));
    }

    @Override
    public void onLeaderInvited(UUID projectId, Long githubUserId) {
        notificationOutbox.push(new ProjectLeaderInvited(projectId, githubUserId, new Date()));
    }

    @Override
    public void onLeaderInvitationCancelled(UUID projectId, Long githubUserId) {
        notificationOutbox.push(new ProjectLeaderInvitationCancelled(projectId, githubUserId, new Date()));
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
