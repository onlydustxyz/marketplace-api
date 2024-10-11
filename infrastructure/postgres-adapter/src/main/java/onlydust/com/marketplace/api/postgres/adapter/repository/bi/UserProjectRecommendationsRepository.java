package onlydust.com.marketplace.api.postgres.adapter.repository.bi;

import datadog.trace.api.Trace;
import jakarta.persistence.EntityManager;
import lombok.NonNull;

import java.util.Map;

public class UserProjectRecommendationsRepository extends PseudoProjectionRepository {
    public UserProjectRecommendationsRepository(final @NonNull EntityManager entityManager) {
        super(entityManager, "public", "user_project_recommendations", "pk");
    }

    @Trace(operationName = "pseudo_projection.global_refresh", resourceName = "global_refresh:public.user_project_recommendations")
    @Override
    public int refresh() {
        return super.refresh();
    }

    @Trace(operationName = "pseudo_projection.refresh", resourceName = "refresh:public.user_project_recommendations")
    public int refresh(Long githubUserId) {
        return refresh(Map.of("github_user_id", githubUserId));
    }
}
