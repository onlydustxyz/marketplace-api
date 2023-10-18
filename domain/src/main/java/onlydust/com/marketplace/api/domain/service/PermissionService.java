package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.port.output.ProjectStoragePort;

import java.util.UUID;

@AllArgsConstructor
public class PermissionService {
    private final ProjectStoragePort projectStoragePort;

    public boolean isUserProjectLead(UUID projectId, UUID projectLeadId) {
        return projectStoragePort.getProjectLeadIds(projectId).contains(projectLeadId);
    }
}
