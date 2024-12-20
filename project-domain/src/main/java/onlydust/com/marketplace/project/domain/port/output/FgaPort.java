package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;

import java.util.List;

public interface FgaPort {
    interface Project {

        void setMaintainers(ProjectId projectId, List<UserId> userIds);

        void addGrantingProgram(ProjectId projectId, ProgramId programIds);

        boolean canEdit(ProjectId projectId, UserId userId);

        boolean canEditPermissions(ProjectId projectId, UserId userId);

        boolean canReadFinancials(ProjectId projectId, UserId userId);

    }
}
