package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.event.OnApplicationCreated;
import onlydust.com.marketplace.project.domain.model.event.ProjectLinkedReposChanged;
import onlydust.com.marketplace.project.domain.port.input.ProjectObserverPort;
import onlydust.com.marketplace.project.domain.port.output.ApplicationObserverPort;

import java.util.Set;

@AllArgsConstructor
public class OutboxProjectService implements ProjectObserverPort, ApplicationObserverPort {
    private final OutboxPort indexerOutbox;
    private final OutboxPort trackingOutbox;

    @Override
    public void onLinkedReposChanged(ProjectId projectId, Set<Long> linkedRepoIds, Set<Long> unlinkedRepoIds) {
        indexerOutbox.push(new ProjectLinkedReposChanged(projectId, linkedRepoIds, unlinkedRepoIds));
    }

    @Override
    public void onRewardSettingsChanged(ProjectId projectId) {
    }

    @Override
    public void onProjectCreated(ProjectId projectId, UserId projectLeadId) {
    }

    @Override
    public void onProjectCategorySuggested(String categoryName, UserId userId) {
    }

    @Override
    public void onLabelsModified(@NonNull ProjectId projectId, Set<Long> githubUserIds) {
    }

    @Override
    public void onApplicationCreated(Application application) {
        trackingOutbox.push(OnApplicationCreated.of(application));
    }

    @Override
    public void onApplicationAccepted(Application application, UserId projectLeadId) {
    }

    @Override
    public void onApplicationRefused(Application application) {
    }
}
