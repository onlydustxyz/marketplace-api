package onlydust.com.marketplace.project.domain.observer;

import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.port.input.ProjectObserverPort;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ProjectObserverComposite implements ProjectObserverPort {

    private final List<ProjectObserverPort> observers;

    public ProjectObserverComposite(ProjectObserverPort... observers) {
        this.observers = List.of(observers);
    }

    @Override
    public void onLinkedReposChanged(UUID projectId, Set<Long> linkedRepoIds, Set<Long> unlinkedRepoIds) {
        observers.forEach(observer -> observer.onLinkedReposChanged(projectId, linkedRepoIds, unlinkedRepoIds));
    }

    @Override
    public void onRewardSettingsChanged(UUID projectId) {
        observers.forEach(observer -> observer.onRewardSettingsChanged(projectId));
    }

    @Override
    public void onUserApplied(UUID projectId, UUID userId, Application.Id applicationId) {
        observers.forEach(observer -> observer.onUserApplied(projectId, userId, applicationId));
    }

    @Override
    public void onProjectCreated(UUID projectId, UUID projectLeadId) {
        observers.forEach(observer -> observer.onProjectCreated(projectId, projectLeadId));
    }

    @Override
    public void onProjectCategorySuggested(String categoryName, UUID userId) {
        observers.forEach(observer -> observer.onProjectCategorySuggested(categoryName, userId));
    }
}
