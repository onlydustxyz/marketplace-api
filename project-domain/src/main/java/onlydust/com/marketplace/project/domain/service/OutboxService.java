package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;
import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.model.notification.ProjectLinkedReposChanged;
import onlydust.com.marketplace.project.domain.model.notification.UserSignedUp;
import onlydust.com.marketplace.project.domain.port.input.ProjectObserverPort;
import onlydust.com.marketplace.project.domain.port.input.UserObserverPort;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
public class OutboxService implements ProjectObserverPort, UserObserverPort {
    private final OutboxPort indexerOutbox;
    private final OutboxPort trackingOutbox;

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

    @Override
    public void onUserSignedUp(User user) {
        final var event = new UserSignedUp(user.getId(), user.getGithubUserId(), user.getGithubLogin(), new Date());
        indexerOutbox.push(event);
        trackingOutbox.push(event);
    }
}
