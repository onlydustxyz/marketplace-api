package onlydust.com.marketplace.api.postgres.adapter.repository.bi;

import jakarta.persistence.EntityManager;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProjectId;

import java.util.Map;

public class BiProjectGlobalDataRepository extends PseudoProjectionRepository {
    public BiProjectGlobalDataRepository(final @NonNull EntityManager entityManager) {
        super(entityManager, "bi", "project_global_data", "project_id");
    }

    public int refresh(final ProjectId projectId) {
        return refresh(Map.of("project_id", projectId.value()));
    }
}
