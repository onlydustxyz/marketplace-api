package onlydust.com.marketplace.user.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.user.domain.model.rbac.Permission;
import onlydust.com.marketplace.user.domain.model.rbac.Resource;
import onlydust.com.marketplace.user.domain.port.output.UserRoleStoragePort;


@AllArgsConstructor
public class RBACService {
    private final UserRoleStoragePort userRoleStoragePort;

    public boolean canRead(UserId userId, Resource resource) {
        final var roles = userRoleStoragePort.findRoles(userId, contributorId);
        return roles.stream().anyMatch(r -> r.role().hasPermission(Resource.CONTRIBUTOR, permission));
    }

    public boolean hasPermission(UserId userId, Long contributorId, Permission permission) {
        final var roles = userRoleStoragePort.findRoles(userId, contributorId);
        return roles.stream().anyMatch(r -> r.role().hasPermission(Resource.CONTRIBUTOR, permission));
    }

    public boolean hasPermission(UserId userId, ProjectId projectId, Permission permission) {
        final var roles = userRoleStoragePort.findRoles(userId, projectId);
        return roles.stream().anyMatch(r -> r.role().hasPermission(Resource.PROJECT, permission));
    }

    public boolean hasPermission(UserId userId, ProgramId programId, Permission permission) {
        final var roles = userRoleStoragePort.findRoles(userId, programId);
        return roles.stream().anyMatch(r -> r.role().hasPermission(Resource.PROGRAM, permission));
    }
}
