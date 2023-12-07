package onlydust.com.marketplace.api.domain.port.input;

import java.util.Set;
import java.util.UUID;

public interface ProjectObserverPort {
    void onProjectCreated(UUID projectId);

    void onProjectDetailsUpdated(UUID projectId);

    void onLeaderAssigned(UUID projectId, UUID leaderId);

    void onLeaderUnassigned(UUID projectId, UUID leaderId);

    void onLeaderInvited(UUID projectId, Long githubUserId);

    void onLeaderInvitationCancelled(UUID projectId, Long githubUserId);

    void onContributionsChanged(UUID projectId);

    void onLinkedReposChanged(UUID projectId, Set<Long> linkedRepoIds, Set<Long> unlinkedRepoIds);

    void onRewardSettingsChanged(UUID projectId);
}
