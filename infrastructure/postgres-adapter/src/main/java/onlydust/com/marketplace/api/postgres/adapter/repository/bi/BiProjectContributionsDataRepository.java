package onlydust.com.marketplace.api.postgres.adapter.repository.bi;

import datadog.trace.api.Trace;
import jakarta.persistence.EntityManager;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProjectId;

import java.util.Map;

public class BiProjectContributionsDataRepository extends PseudoProjectionRepository {
    public BiProjectContributionsDataRepository(final @NonNull EntityManager entityManager) {
        super(entityManager, "bi", "project_contributions_data", "project_id");
    }

    @Trace(operationName = "pseudo_projection.global_refresh", resourceName = "global_refresh:bi.project_contributions_data")
    @Override
    public int refresh() {
        return super.refresh();
    }

    @Trace(operationName = "pseudo_projection.refresh", resourceName = "refresh:bi.project_contributions_data:project_id")
    public int refreshByProject(final ProjectId projectId) {
        return refresh(Map.of("project_id", projectId.value()));
    }

    @Trace(operationName = "pseudo_projection.refresh", resourceName = "refresh:bi.project_contributions_data:repo_id")
    public int refreshByRepo(final Long repoId) {
        return refreshUnsafe("project_id = (select pgr.project_id from project_github_repos pgr where pgr.github_repo_id = %d)".formatted(repoId));
    }
}
