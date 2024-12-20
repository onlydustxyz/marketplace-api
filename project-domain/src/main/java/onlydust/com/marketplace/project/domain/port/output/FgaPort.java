package onlydust.com.marketplace.project.domain.port.output;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;

import java.util.List;

public interface FgaPort {
    interface Project {

        void setMaintainers(@NonNull ProjectId projectId, @NonNull List<UserId> userIds);

        void addGrantingProgram(@NonNull ProjectId projectId, @NonNull ProgramId programId);

        boolean canEdit(@NonNull ProjectId projectId, @NonNull UserId userId);

        boolean canEditPermissions(@NonNull ProjectId projectId, @NonNull UserId userId);

        boolean canReadFinancial(@NonNull ProjectId projectId, @NonNull UserId userId);

    }
}
