package onlydust.com.marketplace.project.domain.observer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubAppAccessToken;
import onlydust.com.marketplace.project.domain.model.GlobalConfig;
import onlydust.com.marketplace.project.domain.port.output.*;
import onlydust.com.marketplace.project.domain.service.GithubAppService;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

@AllArgsConstructor
@Slf4j
public class GithubIssueCommenter implements ApplicationObserverPort {
    private final UserStoragePort userStoragePort;
    private final ProjectStoragePort projectStoragePort;
    private final GithubStoragePort githubStoragePort;
    private final GithubAppService githubAppService;
    private final GithubApiPort githubApiPort;
    private final GlobalConfig globalConfig;

    @Override
    public void onApplicationCreated(Application application) {
        if (application.origin() == Application.Origin.MARKETPLACE) return;

        final var issue = githubStoragePort.findIssueById(application.issueId())
                .orElseThrow(() -> internalServerError("Issue %s not found".formatted(application.issueId())));

        final var project = projectStoragePort.getById(application.projectId())
                .orElseThrow(() -> internalServerError("Project %s not found".formatted(application.projectId())));

        final var applicant = userStoragePort.getUserByGithubId(application.applicantId())
                .orElseThrow(() -> internalServerError("User %s not found".formatted(application.applicantId())));

        githubAppService.getInstallationTokenFor(issue.repoId())
                .filter(GithubAppAccessToken::canWriteIssues)
                .ifPresentOrElse(
                        token -> githubApiPort.createComment(token.token(), issue, """
                                Hey @%s!
                                Thanks for showing interest.
                                We've created an application for you to contribute to %s.
                                Go check it out on [OnlyDust](%s/p/%s)!
                                """.formatted(applicant.getGithubLogin(), project.getName(), globalConfig.getAppBaseUrl(), project.getSlug())),
                        () -> LOGGER.info("Could not get an authorized GitHub token to comment on issue {}", issue.repoId())
                );
    }
}
