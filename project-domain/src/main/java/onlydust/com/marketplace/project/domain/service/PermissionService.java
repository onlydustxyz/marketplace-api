package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.project.domain.port.output.ContributionStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProgramStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.port.output.SponsorStoragePort;

import java.util.UUID;

@AllArgsConstructor
public class PermissionService {
    private final ProjectStoragePort projectStoragePort;
    private final ContributionStoragePort contributionStoragePort;
    private final SponsorStoragePort sponsorStoragePort;
    private final ProgramStoragePort programStoragePort;

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

    public boolean isUserSponsorLead(UUID userId, UUID sponsorId) {
        return sponsorStoragePort.isAdmin(userId, SponsorId.of(sponsorId));
    }

    public boolean isUserProgramLead(UUID userId, ProgramId programId) {
        return programStoragePort.isAdmin(userId, programId);
    }
}
