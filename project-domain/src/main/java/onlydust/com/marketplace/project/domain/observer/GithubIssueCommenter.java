package onlydust.com.marketplace.project.domain.observer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.port.output.*;
import onlydust.com.marketplace.project.domain.service.GithubAppService;

import java.util.UUID;

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

        final var applicant = userStoragePort.getIndexedUserByGithubId(application.applicantId())
                .orElseThrow(() -> internalServerError("User %s not found".formatted(application.applicantId())));

        githubAppService.getInstallationTokenFor(issue.repoId())
                .filter(GithubAppAccessToken::canWriteIssues)
                .ifPresentOrElse(
                        token -> githubApiPort.createComment(token.token(), issue, """
                                Hey @%s!
                                Thanks for showing interest.
                                We've created an application for you to contribute to %s.
                                Go check it out on [OnlyDust](%s/p/%s)!
                                """.formatted(applicant.login(), project.getName(), globalConfig.getAppBaseUrl(), project.getSlug())),
                        () -> LOGGER.info("Could not get an authorized GitHub token to comment on issue {}", issue.repoId())
                );
    }

    @Override
    public void onApplicationAccepted(Application application, UUID projectLeadId) {
        if (application.origin() != Application.Origin.MARKETPLACE) return;

        final var issue = githubStoragePort.findIssueById(application.issueId())
                .orElseThrow(() -> internalServerError("Issue %s not found".formatted(application.issueId())));

        final var project = projectStoragePort.getById(application.projectId())
                .orElseThrow(() -> internalServerError("Project %s not found".formatted(application.projectId())));

        final var applicant = userStoragePort.getIndexedUserByGithubId(application.applicantId())
                .orElseThrow(() -> internalServerError("User %s not found".formatted(application.applicantId())));

        final var projectLead = userStoragePort.getRegisteredUserById(projectLeadId)
                .orElseThrow(() -> internalServerError("User %s not found".formatted(application.applicantId())));

        githubAppService.getInstallationTokenFor(issue.repoId())
                .filter(GithubAppAccessToken::canWriteIssues)
                .ifPresentOrElse(
                        token -> githubApiPort.createComment(token.token(), issue, """
                                The maintainer %s has assigned %s to this issue via [OnlyDust](%s/p/%s) Platform.
                                Good luck!
                                """.formatted(projectLead.getGithubLogin(), applicant.getGithubLogin(), globalConfig.getAppBaseUrl(), project.getSlug())),
                        () -> LOGGER.info("Could not get an authorized GitHub token to comment on issue {}", issue.repoId())
                );
    }

    @Override
    public void onHackathonExternalApplicationDetected(GithubIssue issue, Long applicantId, Hackathon hackathon) {
        final var applicant = userStoragePort.getIndexedUserByGithubId(applicantId)
                .orElseThrow(() -> internalServerError("User %s not found".formatted(applicantId)));

        githubAppService.getInstallationTokenFor(issue.repoId())
                .filter(GithubAppAccessToken::canWriteIssues)
                .ifPresentOrElse(
                        token -> githubApiPort.createComment(token.token(), issue, """
                                Hi @%s!
                                Maintainers during the %s will be tracking applications via [OnlyDust](%s/hackathons/%s).
                                Therefore, in order for you to have a chance at being assigned to this issue, please [apply directly here](%s/hackathons/%s), or else your application may not be considered.
                                """.formatted(applicant.login(), hackathon.title(),
                                globalConfig.getAppBaseUrl(), hackathon.slug(),
                                globalConfig.getAppBaseUrl(), hackathon.slug())),
                        () -> LOGGER.info("Could not get an authorized GitHub token to comment on issue {}", issue.repoId())
                );
    }
}
