package onlydust.com.marketplace.api.postgres.adapter.repository.bi;

import datadog.trace.api.Trace;
import jakarta.persistence.EntityManager;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;

import java.util.Map;

public class BiProjectGrantsDataRepository extends PseudoProjectionRepository {
    public BiProjectGrantsDataRepository(final @NonNull EntityManager entityManager) {
        super(entityManager, "bi", "project_grants_data", "transaction_id");
    }

    @Trace(operationName = "pseudo_projection.global_refresh", resourceName = "global_refresh:bi.project_grants_data")
    @Override
    public int refresh() {
        return super.refresh();
    }

    @Trace(operationName = "pseudo_projection.refresh", resourceName = "refresh:bi.project_grants_data")
    public int refresh(final ProgramId programId, final ProjectId projectId) {
        return refresh(Map.of(
                "project_id", projectId.value(),
                "program_id", programId.value())
        );
    }
}
