package onlydust.com.marketplace.api.postgres.adapter.repository.bi;

import datadog.trace.api.Trace;
import jakarta.persistence.EntityManager;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ContributionUUID;
import onlydust.com.marketplace.project.domain.model.Application;

import java.util.Map;

public class BiApplicationDataRepository extends PseudoProjectionRepository {
    public BiApplicationDataRepository(final @NonNull EntityManager entityManager) {
        super(entityManager, "bi", "application_data", "application_id");
    }

    @Trace(operationName = "pseudo_projection.global_refresh", resourceName = "global_refresh:bi.application_data")
    @Override
    public int refresh() {
        return super.refresh();
    }

    @Trace(operationName = "pseudo_projection.refresh", resourceName = "refresh:bi.application_data:application_id")
    public int refresh(Application.Id id) {
        return refresh(Map.of("application_id", id.value()));
    }

    @Trace(operationName = "pseudo_projection.refresh", resourceName = "refresh:bi.application_data:contribution_uuid")
    public int refreshByContributionUUID(ContributionUUID contributionUUID) {
        return refresh(Map.of("contribution_uuid", contributionUUID.value()));
    }

    @Trace(operationName = "pseudo_projection.refresh", resourceName = "refresh:bi.application_data:repo_id")
    public int refreshByRepo(final Long repoId) {
        return refresh(Map.of("repo_id", repoId));
    }
}
