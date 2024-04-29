package onlydust.com.marketplace.project.domain.port.input;

import java.util.UUID;

public interface BoostNodeGuardiansRewardsPort {

    void boostProject(UUID projectId, UUID projectLeadId, Long githubRepoId, UUID ecosystemId);
}
