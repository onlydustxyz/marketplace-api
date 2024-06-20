package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.GlobalConfig;
import onlydust.com.marketplace.project.domain.port.input.ApplicationFacadePort;
import onlydust.com.marketplace.project.domain.port.output.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.*;

@AllArgsConstructor
@Slf4j
public class ApplicationService implements ApplicationFacadePort {
    private final UserStoragePort userStoragePort;
    private final ProjectStoragePort projectStoragePort;
    private final ApplicationObserverPort applicationObserver;
    private final GithubUserPermissionsService githubUserPermissionsService;
    private final GithubStoragePort githubStoragePort;
    private final GithubApiPort githubApiPort;
    private final GithubAuthenticationPort githubAuthenticationPort;
    private final GithubAppService githubAppService;
    private final GlobalConfig globalConfig;

    @Override
    public void deleteApplication(Application.Id id, UUID userId, Long githubUserId) {
        final var application = userStoragePort.findApplication(id)
                .orElseThrow(() -> notFound("Application %s not found".formatted(id)));

        if (!application.applicantId().equals(githubUserId)) {
            final var isProjectLead = projectStoragePort.getProjectLeadIds(application.projectId()).contains(userId);
            if (!isProjectLead)
                throw forbidden("User is not authorized to delete this application");
        }

        userStoragePort.deleteApplications(id);
    }

    @Override
    public void acceptApplication(Application.Id id, UUID userId) {
        final var application = userStoragePort.findApplication(id)
                .orElseThrow(() -> notFound("Application %s not found".formatted(id)));

        if (!projectStoragePort.getProjectLeadIds(application.projectId()).contains(userId))
            throw forbidden("User is not authorized to accept this application");

        final var issue = githubStoragePort.findIssueById(application.issueId())
                .orElseThrow(() -> notFound("Issue %s not found".formatted(application.issueId())));

        final var applicant = userStoragePort.getUserByGithubId(application.applicantId())
                .orElseThrow(() -> notFound("User %d not found".formatted(application.applicantId())));

        final var githubAppToken = githubAppService.getInstallationTokenFor(issue.repoId())
                .orElseThrow(() -> internalServerError("Could not generate GitHub App token for repository %d".formatted(issue.repoId())));

        githubApiPort.assign(githubAppToken.token(), issue.repoId(), issue.number(), applicant.getGithubLogin());
    }

    @Override
    @Transactional
    public Application applyOnProject(@NonNull Long githubUserId,
                                      @NonNull UUID projectId,
                                      @NonNull GithubIssue.Id issueId,
                                      @NonNull String motivation,
                                      String problemSolvingApproach) {
        if (!githubUserPermissionsService.isUserAuthorizedToApplyOnProject(githubUserId))
            throw forbidden("User is not authorized to apply on project");

        final var issue = githubStoragePort.findIssueById(issueId)
                .orElseThrow(() -> notFound("Issue %s not found".formatted(issueId)));

        if (issue.isAssigned())
            throw forbidden("Issue %s is already assigned".formatted(issueId));

        if (userStoragePort.findApplication(githubUserId, projectId, issueId).isPresent())
            throw badRequest("User already applied to this issue");

        if (!projectStoragePort.getProjectRepoIds(projectId).contains(issue.repoId()))
            throw badRequest("Issue %s does not belong to project %s".formatted(issueId, projectId));

        final var personalAccessToken = githubAuthenticationPort.getGithubPersonalToken(githubUserId);

        final var commentId = githubApiPort.createComment(personalAccessToken, issue, """
                I am applying to this issue via [OnlyDust platform](%s).
                """.formatted(globalConfig.getAppBaseUrl()));

        final var application = Application.fromMarketplace(projectId, githubUserId, issueId, commentId, motivation, problemSolvingApproach);

        userStoragePort.save(application);
        applicationObserver.onApplicationCreated(application);

        return application;
    }

    @Override
    public Application updateApplication(@NonNull Application.Id applicationId, @NonNull Long githubUserId, @NonNull String motivation,
                                         String problemSolvingApproach) {
        final var application = userStoragePort.findApplication(applicationId)
                .orElseThrow(() -> notFound("Application %s not found".formatted(applicationId)));

        if (!application.applicantId().equals(githubUserId))
            throw forbidden("User is not authorized to update this application");

        final var updated = application.update(motivation, problemSolvingApproach);
        userStoragePort.save(updated);

        return updated;
    }
}
