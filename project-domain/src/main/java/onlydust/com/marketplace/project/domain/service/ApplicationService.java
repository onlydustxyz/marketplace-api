package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.port.input.ApplicationFacadePort;
import onlydust.com.marketplace.project.domain.port.output.*;
import org.springframework.transaction.annotation.Transactional;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.*;

@AllArgsConstructor
@Slf4j
public class ApplicationService implements ApplicationFacadePort {
    private final UserStoragePort userStoragePort;
    private final ProjectApplicationStoragePort projectApplicationStoragePort;
    private final ProjectStoragePort projectStoragePort;
    private final ApplicationObserverPort applicationObserver;
    private final GithubUserPermissionsService githubUserPermissionsService;
    private final GithubStoragePort githubStoragePort;
    private final GithubApiPort githubApiPort;
    private final GithubCommandService githubCommandService;
    private final GithubAuthenticationPort githubAuthenticationPort;
    private final GithubAppService githubAppService;

    @Override
    @Transactional
    public void deleteApplication(Application.Id id, UserId userId, Long githubUserId, boolean deleteGithubComment) {
        final var application = projectApplicationStoragePort.findApplication(id)
                .orElseThrow(() -> notFound("Application %s not found".formatted(id)));

        final var deleteSelfApplication = application.applicantId().equals(githubUserId);

        if (!deleteSelfApplication) {
            final var isProjectLead = projectStoragePort.getProjectLeadIds(application.projectId()).contains(userId);
            if (!isProjectLead)
                throw forbidden("User is not authorized to delete this application");
        }

        projectApplicationStoragePort.deleteApplications(id);
        applicationObserver.onApplicationDeleted(application);

        if (deleteSelfApplication && deleteGithubComment)
            deleteGithubComment(application);
    }

    private void deleteGithubComment(Application application) {
        final var issue = githubStoragePort.findIssueById(application.issueId())
                .orElseThrow(() -> notFound("Issue %s not found".formatted(application.issueId())));
        final var personalAccessToken = githubAuthenticationPort.getGithubPersonalToken(application.applicantId());
        githubApiPort.deleteComment(personalAccessToken, issue.repoId(), application.commentId());
    }

    @Override
    @Transactional
    public void acceptApplication(Application.Id id, UserId userId) {
        final var application = projectApplicationStoragePort.findApplication(id)
                .orElseThrow(() -> notFound("Application %s not found".formatted(id)));

        if (!projectStoragePort.getProjectLeadIds(application.projectId()).contains(userId))
            throw forbidden("User is not authorized to accept this application");

        final var issue = githubStoragePort.findIssueById(application.issueId())
                .orElseThrow(() -> notFound("Issue %s not found".formatted(application.issueId())));

        final var applicant = userStoragePort.getIndexedUserByGithubId(application.applicantId())
                .orElseThrow(() -> notFound("User %d not found".formatted(application.applicantId())));

        final var githubAppToken = githubAppService.getInstallationTokenFor(issue.repoId())
                .orElseThrow(() -> internalServerError("Could not generate GitHub App token for repository %d".formatted(issue.repoId())));

        githubApiPort.assign(githubAppToken.token(), issue.repoId(), issue.number(), applicant.login());
        applicationObserver.onApplicationAccepted(application, userId);

        projectApplicationStoragePort.findApplicationsOnIssueAndProject(application.issueId(), application.projectId())
                .stream()
                .filter(a -> !a.applicantId().equals(application.applicantId()))
                .forEach(applicationObserver::onApplicationRefused);
    }

    @Override
    @Transactional
    public Application applyOnProject(@NonNull Long githubUserId,
                                      @NonNull ProjectId projectId,
                                      @NonNull GithubIssue.Id issueId,
                                      @NonNull String githubComment) {
        if (!githubUserPermissionsService.isUserAuthorizedToApplyOnProject(githubUserId))
            throw forbidden("User is not authorized to apply on project");

        final var issue = githubStoragePort.findIssueById(issueId)
                .orElseThrow(() -> notFound("Issue %s not found".formatted(issueId)));

        if (issue.isAssigned())
            throw badRequest("Issue %s is already assigned".formatted(issueId));

        if (!projectStoragePort.getProjectRepoIds(projectId).contains(issue.repoId()))
            throw badRequest("Issue %s does not belong to project %s".formatted(issueId, projectId));

        final var application = Application.fromMarketplace(projectId, githubUserId, issueId);
        if (!projectApplicationStoragePort.saveNew(application))
            throw badRequest("User %d already applied to issue %s".formatted(githubUserId, issueId));

        githubCommandService.createComment(application.id(), issue, githubUserId, githubComment);
        return application;
    }

    @Override
    public void updateApplication(@NonNull UserId userId, @NonNull Application.Id applicationId, Boolean isIgnored) {
        final var application = projectApplicationStoragePort.findApplication(applicationId)
                .orElseThrow(() -> notFound("Application %s not found".formatted(applicationId)));

        if (!projectStoragePort.getProjectLeadIds(application.projectId()).contains(userId))
            throw forbidden("User is not authorized to update this application");

        if (TRUE.equals(isIgnored))
            application.ignore();

        if (FALSE.equals(isIgnored))
            application.unIgnore();

        projectApplicationStoragePort.save(application);
    }
}
