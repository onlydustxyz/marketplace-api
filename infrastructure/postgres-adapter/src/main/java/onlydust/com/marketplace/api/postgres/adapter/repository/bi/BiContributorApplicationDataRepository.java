package onlydust.com.marketplace.api.postgres.adapter.repository.bi;

import datadog.trace.api.Trace;
import jakarta.persistence.EntityManager;
import lombok.NonNull;

import java.util.Map;

public class BiContributorApplicationDataRepository extends PseudoProjectionRepository {
    public BiContributorApplicationDataRepository(final @NonNull EntityManager entityManager) {
        super(entityManager, "bi", "contributor_application_data", "contributor_id");
    }

    @Trace(operationName = "pseudo_projection.global_refresh", resourceName = "global_refresh:bi.contributor_application_data")
    @Override
    public int refresh() {
        return super.refresh();
    }

    @Trace(operationName = "pseudo_projection.refresh", resourceName = "refresh:bi.contributor_application_data")
    public int refresh(final Long contributorId) {
        return refresh(Map.of("contributor_id", contributorId));
    }
}
