package onlydust.com.marketplace.api.postgres.adapter.repository.bi;

import datadog.trace.api.Trace;
import jakarta.persistence.EntityManager;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.kernel.model.ProjectId;

import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.joining;

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

    @Trace(operationName = "pseudo_projection.refresh", resourceName = "refresh:bi.contribution_data")
    public int refresh(final ProjectId projectId, final Set<GithubUserId> githubUserIds) {
        final var userIdArray = githubUserIds.stream().map(GithubUserId::value).map(Object::toString).collect(joining(","));
        return refreshUnsafe("'%s' = project_id and array[%s]::bigint[] && contributor_ids".formatted(projectId.value(), userIdArray));
    }
}
