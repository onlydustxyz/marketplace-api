package onlydust.com.marketplace.api.postgres.adapter.repository.bi;

import datadog.trace.api.Trace;
import jakarta.persistence.EntityManager;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.RewardId;

import java.util.Map;

public class BiContributionRewardDataRepository extends PseudoProjectionRepository {
    public BiContributionRewardDataRepository(final @NonNull EntityManager entityManager) {
        super(entityManager, "bi", "contribution_reward_data", "contribution_id");
    }

    @Trace(operationName = "pseudo_projection.global_refresh", resourceName = "global_refresh:bi.contribution_reward_data")
    @Override
    public int refresh() {
        return super.refresh();
    }

    @Trace(operationName = "pseudo_projection.refresh", resourceName = "refresh:bi.contribution_reward_data")
    public int refresh(final String contributionId) {
        return refresh(Map.of("contribution_id", contributionId));
    }

    @Trace(operationName = "pseudo_projection.refresh", resourceName = "refresh:bi.contribution_reward_data")
    public int refresh(final Long repoId) {
        return refresh(Map.of("repo_id", repoId));
    }

    @Trace(operationName = "pseudo_projection.refresh", resourceName = "refresh:bi.contribution_reward_data")
    public int refresh(final ProjectId projectId) {
        return refresh(Map.of("project_id", projectId));
    }

    @Trace(operationName = "pseudo_projection.refresh", resourceName = "refresh:bi.contribution_reward_data")
    public int refresh(final RewardId rewardId) {
        return refreshUnsafe("%s = any(reward_ids)".formatted(rewardId));
    }
}
