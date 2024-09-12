package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.port.output.PermissionPort;
import onlydust.com.marketplace.project.domain.port.output.ContributionStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProgramStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.port.output.SponsorStoragePort;

import java.util.Optional;

@AllArgsConstructor
public class PermissionService implements PermissionPort {
    private final ProjectStoragePort projectStoragePort;
    private final ContributionStoragePort contributionStoragePort;
    private final SponsorStoragePort sponsorStoragePort;
    private final ProgramStoragePort programStoragePort;

    @Override
    public boolean isUserProjectLead(ProjectId projectId, UserId projectLeadId) {
        return projectStoragePort.getProjectLeadIds(projectId).contains(projectLeadId);
    }

    @Override
    public boolean isUserContributor(String contributionId, Long githubUserId) {
        return contributionStoragePort.getContributorId(contributionId).equals(githubUserId);
    }

    @Override
    public boolean isRepoLinkedToProject(ProjectId projectId, Long githubRepoId) {
        return projectStoragePort.getProjectRepoIds(projectId).contains(githubRepoId);
    }

    @Override
    public boolean hasUserAccessToProject(ProjectId projectId, Optional<UserId> userId) {
        return projectStoragePort.hasUserAccessToProject(projectId, userId.orElse(null))
               || (userId.isPresent() && programStoragePort.isAdmin(userId.get(), projectId));
    }

    @Override
    public boolean hasUserAccessToProject(String projectSlug, Optional<UserId> userId) {
        return projectStoragePort.hasUserAccessToProject(projectSlug, userId.orElse(null))
               || (userId.isPresent() && programStoragePort.isAdmin(userId.get(), projectSlug));
    }

    @Override
    public boolean isUserSponsorLead(UserId userId, SponsorId sponsorId) {
        return sponsorStoragePort.isAdmin(userId, sponsorId);
    }

    @Override
    public boolean isUserSponsorLeadOfProgram(UserId userId, ProgramId programId) {
        return sponsorStoragePort.isAdminOfProgramSponsor(userId, programId);
    }

    @Override
    public boolean isUserProgramLead(UserId userId, ProgramId programId) {
        return programStoragePort.isAdmin(userId, programId);
    }
}
