package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.project.domain.port.output.ContributionStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectSponsorStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;

import java.util.UUID;

@AllArgsConstructor
public class PermissionService {
    private final ProjectStoragePort projectStoragePort;
    private final ContributionStoragePort contributionStoragePort;
    private final ProjectSponsorStoragePort sponsorStoragePort;

    public boolean isUserProjectLead(UUID projectId, UUID projectLeadId) {
        return projectStoragePort.getProjectLeadIds(projectId).contains(projectLeadId);
    }

    public boolean isUserContributor(String contributionId, Long githubUserId) {
        return contributionStoragePort.getContributorId(contributionId).equals(githubUserId);
    }

    public boolean isRepoLinkedToProject(UUID projectId, Long githubRepoId) {
        return projectStoragePort.getProjectRepoIds(projectId).contains(githubRepoId);
    }

    public boolean hasUserAccessToProject(UUID projectId, UUID userId) {
        return projectStoragePort.hasUserAccessToProject(projectId, userId);
    }

    public boolean hasUserAccessToProject(String projectSlug, UUID userId) {
        return projectStoragePort.hasUserAccessToProject(projectSlug, userId);
    }

    public boolean isUserSponsorAdmin(UUID userId, UUID sponsorId) {
        return sponsorStoragePort.isUserSponsorAdmin(userId, sponsorId);
    }
}
