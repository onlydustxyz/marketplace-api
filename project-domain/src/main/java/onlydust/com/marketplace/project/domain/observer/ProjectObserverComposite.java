package onlydust.com.marketplace.project.domain.observer;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.port.input.ProjectObserverPort;

import java.util.List;
import java.util.Set;

public class ProjectObserverComposite implements ProjectObserverPort {

    private final List<ProjectObserverPort> observers;

    public ProjectObserverComposite(ProjectObserverPort... observers) {
        this.observers = List.of(observers);
    }

    @Override
    public void onLinkedReposChanged(ProjectId projectId, Set<Long> linkedRepoIds, Set<Long> unlinkedRepoIds) {
        observers.forEach(observer -> observer.onLinkedReposChanged(projectId, linkedRepoIds, unlinkedRepoIds));
    }

    @Override
    public void onRewardSettingsChanged(ProjectId projectId) {
        observers.forEach(observer -> observer.onRewardSettingsChanged(projectId));
    }

    @Override
    public void onProjectCreated(ProjectId projectId, UserId projectLeadId) {
        observers.forEach(observer -> observer.onProjectCreated(projectId, projectLeadId));
    }

    @Override
    public void onProjectCategorySuggested(String categoryName, UserId userId) {
        observers.forEach(observer -> observer.onProjectCategorySuggested(categoryName, userId));
    }

    @Override
    public void onLabelsModified(@NonNull ProjectId projectId, Set<Long> githubUserIds) {
        observers.forEach(observer -> observer.onLabelsModified(projectId, githubUserIds));
    }
}
