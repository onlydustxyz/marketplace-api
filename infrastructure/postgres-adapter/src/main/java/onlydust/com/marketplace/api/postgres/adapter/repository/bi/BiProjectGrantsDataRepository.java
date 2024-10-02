package onlydust.com.marketplace.api.postgres.adapter.repository.bi;

import jakarta.persistence.EntityManager;
import lombok.NonNull;

public class BiProjectGrantsDataRepository extends PseudoProjectionRepository {
    public BiProjectGrantsDataRepository(final @NonNull EntityManager entityManager) {
        super(entityManager, "bi", "project_grants_data", "transaction_id");
    }
}
