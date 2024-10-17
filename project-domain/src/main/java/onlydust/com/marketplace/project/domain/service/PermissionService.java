package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.model.*;
import onlydust.com.marketplace.kernel.port.output.PermissionPort;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.GithubPullRequest;
import onlydust.com.marketplace.project.domain.port.output.*;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class PermissionService implements PermissionPort {
    private final ProjectStoragePort projectStoragePort;
    private final ContributionStoragePort contributionStoragePort;
    private final SponsorStoragePort sponsorStoragePort;
    private final ProgramStoragePort programStoragePort;
    private final EcosystemStoragePort ecosystemStoragePort;

    @Override
    public boolean isUserProjectLead(OrSlug<ProjectId> projectIdOrSlug, UserId projectLeadId) {
        return projectStoragePort.getProjectLeadIds(projectIdOrSlug).contains(projectLeadId);
    }

    @Override
    public boolean isUserProjectLead(ProjectId projectId, UserId projectLeadId) {
        return isUserProjectLead(OrSlug.of(projectId), projectLeadId);
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

    @Override
    public List<ProgramId> getLedProgramIds(UserId userId) {
        return programStoragePort.getProgramLedIdsForUser(userId);
    }

    @Override
    public List<EcosystemId> getLedEcosystemIds(UserId userId) {
        return ecosystemStoragePort.getEcosystemLedIdsForUser(userId);
    }

    @Override
    public List<SponsorId> getLedSponsorIds(UserId userId) {
        return sponsorStoragePort.getSponsorLedIdsForUser(userId);
    }

    @Override
    public List<ProjectId> getLedProjectIds(UserId userId) {
        return projectStoragePort.getProjectLedIdsForUser(userId);
    }

    @Override
    public boolean canUserUpdateIssue(UserId userId, Long githubIssueId) {
        for (ProjectId projectId : projectStoragePort.getProjectIdsLinkedToIssueId(GithubIssue.Id.of(githubIssueId))) {
            if (isUserProjectLead(projectId, userId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canUserUpdatePullRequest(UserId userId, Long githubPullRequestId) {
        for (ProjectId projectId : projectStoragePort.getProjectIdsLinkedToPullRequestId(GithubPullRequest.Id.of(githubPullRequestId))) {
            if (isUserProjectLead(projectId, userId)) {
                return true;
            }
        }
        return false;
    }
}
