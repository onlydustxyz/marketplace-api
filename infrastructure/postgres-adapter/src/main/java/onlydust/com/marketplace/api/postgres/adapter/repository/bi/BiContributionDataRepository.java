package onlydust.com.marketplace.api.postgres.adapter.repository.bi;

import jakarta.persistence.EntityManager;
import lombok.NonNull;

import java.util.Map;

public class BiContributionDataRepository extends PseudoProjectionRepository {
    public BiContributionDataRepository(final @NonNull EntityManager entityManager) {
        super(entityManager, "bi", "contribution_data", "contribution_id");
    }

    public int refreshByRepo(final Long repoId) {
        return refresh(Map.of("repo_id", repoId));
    }
}
