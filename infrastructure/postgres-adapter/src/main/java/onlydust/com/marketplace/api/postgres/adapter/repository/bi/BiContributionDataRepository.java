package onlydust.com.marketplace.api.postgres.adapter.repository.bi;

import jakarta.persistence.EntityManager;
import lombok.NonNull;

public class BiContributionDataRepository extends PseudoProjectionRepository {
    public BiContributionDataRepository(final @NonNull EntityManager entityManager) {
        super(entityManager, "bi", "contribution_data", "contribution_id");
    }
}
