package onlydust.com.marketplace.api.postgres.adapter.repository.bi;

import datadog.trace.api.Trace;
import jakarta.persistence.EntityManager;
import lombok.NonNull;

import java.util.Map;

public class BiContributorGlobalDataRepository extends PseudoProjectionRepository {
    public BiContributorGlobalDataRepository(final @NonNull EntityManager entityManager) {
        super(entityManager, "bi", "contributor_global_data", "contributor_id");
    }

    @Trace(operationName = "pseudo_projection.global_refresh", resourceName = "global_refresh:bi.contributor_global_data")
    @Override
    public int refresh() {
        return super.refresh();
    }

    @Trace(operationName = "pseudo_projection.refresh", resourceName = "refresh:bi.contributor_global_data")
    public int refresh(final Long contributorId) {
        return refresh(Map.of("contributor_id", contributorId));
    }
}
