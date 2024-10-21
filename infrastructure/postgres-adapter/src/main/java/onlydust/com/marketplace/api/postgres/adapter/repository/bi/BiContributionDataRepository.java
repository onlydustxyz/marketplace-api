package onlydust.com.marketplace.api.postgres.adapter.repository.bi;

import datadog.trace.api.Trace;
import jakarta.persistence.EntityManager;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ContributionUUID;

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

    @Trace(operationName = "pseudo_projection.refresh", resourceName = "refresh:bi.contribution_data:contribution_uuid")
    public int refreshByUUID(final ContributionUUID contributionUUID) {
        return refresh(Map.of("contribution_uuid", contributionUUID.value()));
    }

    @Trace(operationName = "pseudo_projection.refresh", resourceName = "refresh:bi.contribution_data:repo_id")
    public int refreshByRepo(final Long repoId) {
        return refresh(Map.of("repo_id", repoId));
    }
}
