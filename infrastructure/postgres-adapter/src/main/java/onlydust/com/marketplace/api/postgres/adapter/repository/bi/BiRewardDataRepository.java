package onlydust.com.marketplace.api.postgres.adapter.repository.bi;

import jakarta.persistence.EntityManager;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.RewardId;

import java.util.Map;

public class BiRewardDataRepository extends PseudoProjectionRepository {
    public BiRewardDataRepository(final @NonNull EntityManager entityManager) {
        super(entityManager, "bi", "reward_data", "reward_id");
    }

    public int refresh(RewardId id) {
        return refresh(Map.of("reward_id", id.value()));
    }
}
