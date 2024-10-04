package onlydust.com.marketplace.api.postgres.adapter.repository.bi;

import datadog.trace.api.Trace;
import jakarta.persistence.EntityManager;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.RewardId;

import java.util.Map;

public class BiRewardDataRepository extends PseudoProjectionRepository {
    public BiRewardDataRepository(final @NonNull EntityManager entityManager) {
        super(entityManager, "bi", "reward_data", "reward_id");
    }

    @Trace(operationName = "pseudo_projection.global_refresh", resourceName = "global_refresh:bi.reward_data")
    @Override
    public int refresh() {
        return super.refresh();
    }

    @Trace(operationName = "pseudo_projection.refresh", resourceName = "refresh:bi.reward_data")
    public int refresh(RewardId id) {
        return refresh(Map.of("reward_id", id.value()));
    }
}
