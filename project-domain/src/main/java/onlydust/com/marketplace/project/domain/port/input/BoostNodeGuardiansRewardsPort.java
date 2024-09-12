package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;

import java.util.UUID;

public interface BoostNodeGuardiansRewardsPort {

    void boostProject(ProjectId projectId, UserId projectLeadId, Long githubRepoId, UUID ecosystemId);
}
