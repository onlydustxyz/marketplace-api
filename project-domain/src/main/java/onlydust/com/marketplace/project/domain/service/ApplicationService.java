package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.GlobalConfig;
import onlydust.com.marketplace.project.domain.model.Project;
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
    private final GithubAuthenticationPort githubAuthenticationPort;
    private final GithubAppService githubAppService;
    private final GlobalConfig globalConfig;

    @Override
    @Transactional
    public void deleteApplication(Application.Id id, UserId userId, Long githubUserId) {
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

        if (deleteSelfApplication && application.origin() == Application.Origin.MARKETPLACE)
            tryDeleteGithubComment(application);
    }

    private void tryDeleteGithubComment(Application application) {
        try {
            final var issue = githubStoragePort.findIssueById(application.issueId())
                    .orElseThrow(() -> notFound("Issue %s not found".formatted(application.issueId())));
            final var personalAccessToken = githubAuthenticationPort.getGithubPersonalToken(application.applicantId());
            githubApiPort.deleteComment(personalAccessToken, issue.repoId(), application.commentId());
        } catch (Exception e) {
            LOGGER.info("Could not delete GitHub comment for application %s".formatted(application.id()), e);
        }
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
                                      @NonNull String motivation,
                                      String problemSolvingApproach) {
        if (!githubUserPermissionsService.isUserAuthorizedToApplyOnProject(githubUserId))
            throw forbidden("User is not authorized to apply on project");

        final var issue = githubStoragePort.findIssueById(issueId)
                .orElseThrow(() -> notFound("Issue %s not found".formatted(issueId)));

        if (issue.isAssigned())
            throw badRequest("Issue %s is already assigned".formatted(issueId));

        if (projectApplicationStoragePort.findApplication(githubUserId, projectId, issueId).isPresent())
            throw badRequest("User already applied to this issue");

        final var project = projectStoragePort.getById(projectId)
                .orElseThrow(() -> notFound("Project %s not found".formatted(projectId)));

        if (!projectStoragePort.getProjectRepoIds(projectId).contains(issue.repoId()))
            throw badRequest("Issue %s does not belong to project %s".formatted(issueId, projectId));

        final var personalAccessToken = githubAuthenticationPort.getGithubPersonalToken(githubUserId);

        final var commentId = githubApiPort.createComment(personalAccessToken, issue, formatComment(project, motivation, problemSolvingApproach));

        final var application = Application.fromMarketplace(projectId, githubUserId, issueId, commentId, motivation, problemSolvingApproach);

        projectApplicationStoragePort.save(application);
        applicationObserver.onApplicationCreated(application);

        return application;
    }

    private @NonNull String formatComment(final @NonNull Project project,
                                          final @NonNull String motivations,
                                          final String problemSolvingApproach) {
        final var header = """
                I am applying to this issue via [OnlyDust platform](%s/p/%s).
                """.formatted(globalConfig.getAppBaseUrl(), project.getSlug());

        final var motivationsSection = """
                
                ### My background and how it can be leveraged
                %s
                """.formatted(motivations);

        final var problemSolvingApproachSection = (problemSolvingApproach == null || problemSolvingApproach.isBlank()) ? "" : """
                
                ### How I plan on tackling this issue
                %s
                """.formatted(problemSolvingApproach);

        return header + motivationsSection + problemSolvingApproachSection;
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
