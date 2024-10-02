package onlydust.com.marketplace.api.postgres.adapter.repository.bi;

import jakarta.persistence.EntityManager;
import lombok.NonNull;

public class BiProjectGlobalDataRepository extends PseudoProjectionRepository {
    public BiProjectGlobalDataRepository(final @NonNull EntityManager entityManager) {
        super(entityManager, "bi", "project_global_data", "project_id");
    }
}
