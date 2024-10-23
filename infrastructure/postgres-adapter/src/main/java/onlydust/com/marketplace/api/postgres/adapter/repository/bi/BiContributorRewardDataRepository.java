package onlydust.com.marketplace.api.postgres.adapter.repository.bi;

import datadog.trace.api.Trace;
import jakarta.persistence.EntityManager;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.RewardId;

import java.util.Map;

public class BiContributorRewardDataRepository extends PseudoProjectionRepository {
    public BiContributorRewardDataRepository(final @NonNull EntityManager entityManager) {
        super(entityManager, "bi", "contributor_reward_data", "contributor_id");
    }

    @Trace(operationName = "pseudo_projection.global_refresh", resourceName = "global_refresh:bi.contributor_reward_data")
    @Override
    public int refresh() {
        return super.refresh();
    }

    @Trace(operationName = "pseudo_projection.refresh", resourceName = "refresh:bi.contributor_reward_data")
    public int refresh(final Long contributorId) {
        return refresh(Map.of("contributor_id", contributorId));
    }

    @Trace(operationName = "pseudo_projection.refresh", resourceName = "refresh:bi.contributor_reward_data:reward_id")
    public int refreshByReward(final RewardId rewardId) {
        return refreshUnsafe("contributor_id = (select rew.recipient_id from rewards rew where rew.id = '%s')".formatted(rewardId.value()));
    }
}
