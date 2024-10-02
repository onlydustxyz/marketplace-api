package onlydust.com.marketplace.api.postgres.adapter.repository.bi;

import jakarta.persistence.EntityManager;
import lombok.NonNull;

import java.util.Map;

public class BiContributorGlobalDataRepository extends PseudoProjectionRepository {
    public BiContributorGlobalDataRepository(final @NonNull EntityManager entityManager) {
        super(entityManager, "bi", "contributor_global_data", "contributor_id");
    }

    public int refresh(final Long contributorId) {
        return refresh(Map.of("contributor_id", contributorId));
    }
}
