package onlydust.com.marketplace.user.domain.port.output;

import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.user.domain.model.rbac.Roles;

import java.util.Optional;
import java.util.Set;

public interface UserRoleStoragePort {
    Set<Roles> findRoles(Optional<UserId> userId, Long contributorId);

    Set<Roles> findRoles(Optional<UserId> userId, ProjectId projectId);

    Set<Roles> findRoles(Optional<UserId> userId, ProgramId programId);
}
