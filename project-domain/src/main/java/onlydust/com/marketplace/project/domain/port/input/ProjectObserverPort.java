package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;

import java.util.Set;

public interface ProjectObserverPort {
    void onLinkedReposChanged(ProjectId projectId, Set<Long> linkedRepoIds, Set<Long> unlinkedRepoIds);

    void onRewardSettingsChanged(ProjectId projectId);

    void onProjectCreated(ProjectId projectId, UserId projectLeadId);

    void onProjectCategorySuggested(String categoryName, UserId userId);

    void onLabelsModified(@NonNull ProjectId projectId, Set<Long> githubUserIds);
}
