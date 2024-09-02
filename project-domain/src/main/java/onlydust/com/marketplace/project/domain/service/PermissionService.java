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
import java.util.UUID;

@AllArgsConstructor
public class PermissionService implements PermissionPort {
    private final ProjectStoragePort projectStoragePort;
    private final ContributionStoragePort contributionStoragePort;
    private final SponsorStoragePort sponsorStoragePort;
    private final ProgramStoragePort programStoragePort;


    @Override
    public boolean isUserProjectLead(ProjectId projectId, UserId projectLeadId) {
        return projectStoragePort.getProjectLeadIds(projectId.value()).contains(projectLeadId.value());
    }

    @Deprecated
    public boolean isUserProjectLead(UUID projectId, UUID projectLeadId) {
        return isUserProjectLead(ProjectId.of(projectId), UserId.of(projectLeadId));
    }


    @Override
    public boolean isUserContributor(String contributionId, Long githubUserId) {
        return contributionStoragePort.getContributorId(contributionId).equals(githubUserId);
    }


    @Override
    public boolean isRepoLinkedToProject(ProjectId projectId, Long githubRepoId) {
        return projectStoragePort.getProjectRepoIds(projectId.value()).contains(githubRepoId);
    }

    @Deprecated
    public boolean isRepoLinkedToProject(UUID projectId, Long githubRepoId) {
        return isRepoLinkedToProject(ProjectId.of(projectId), githubRepoId);
    }


    @Override
    public boolean hasUserAccessToProject(ProjectId projectId, Optional<UserId> userId) {
        return projectStoragePort.hasUserAccessToProject(projectId.value(), userId.map(UserId::value).orElse(null))
               || (userId.isPresent() && programStoragePort.isAdmin(userId.get().value(), projectId));
    }

    @Deprecated
    public boolean hasUserAccessToProject(UUID projectId, UUID userId) {
        return hasUserAccessToProject(ProjectId.of(projectId), Optional.ofNullable(userId).map(UserId::of));
    }


    @Override
    public boolean hasUserAccessToProject(String projectSlug, Optional<UserId> userId) {
        return projectStoragePort.hasUserAccessToProject(projectSlug, userId.map(UserId::value).orElse(null))
               || (userId.isPresent() && programStoragePort.isAdmin(userId.get().value(), projectSlug));
    }

    @Deprecated
    public boolean hasUserAccessToProject(String projectSlug, UUID userId) {
        return hasUserAccessToProject(projectSlug, Optional.ofNullable(userId).map(UserId::of));
    }


    @Override
    public boolean isUserSponsorLead(UserId userId, SponsorId sponsorId) {
        return sponsorStoragePort.isAdmin(userId.value(), sponsorId);
    }

    @Deprecated
    public boolean isUserSponsorLead(UUID userId, SponsorId sponsorId) {
        return isUserSponsorLead(UserId.of(userId), sponsorId);
    }


    @Override
    public boolean isUserSponsorLeadOfProgram(UserId userId, ProgramId programId) {
        return sponsorStoragePort.isAdminOfProgramSponsor(userId.value(), programId);
    }

    @Deprecated
    public boolean isUserSponsorLeadOfProgram(UUID userId, ProgramId programId) {
        return isUserSponsorLeadOfProgram(UserId.of(userId), programId);
    }


    @Override
    public boolean isUserProgramLead(UserId userId, ProgramId programId) {
        return programStoragePort.isAdmin(userId.value(), programId);
    }

    @Deprecated
    public boolean isUserProgramLead(UUID userId, ProgramId programId) {
        return isUserProgramLead(UserId.of(userId), programId);
    }
}
