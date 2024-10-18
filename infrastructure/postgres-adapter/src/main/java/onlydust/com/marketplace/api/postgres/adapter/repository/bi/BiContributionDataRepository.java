package onlydust.com.marketplace.api.postgres.adapter.repository.bi;

import datadog.trace.api.Trace;
import jakarta.persistence.EntityManager;
import lombok.NonNull;

import java.util.Map;

public class BiContributionDataRepository extends PseudoProjectionRepository {
    public BiContributionDataRepository(final @NonNull EntityManager entityManager) {
        super(entityManager, "bi", "contribution_data", "contribution_uuid");
    }

    @Trace(operationName = "pseudo_projection.global_refresh", resourceName = "global_refresh:bi.contribution_data")
    @Override
    public int refresh() {
        return super.refresh();
    }

    @Trace(operationName = "pseudo_projection.refresh", resourceName = "refresh:bi.contribution_data")
    public int refreshByRepo(final Long repoId) {
        return refresh(Map.of("repo_id", repoId));
    }
}
