package onlydust.com.marketplace.project.domain.observer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.port.output.ApplicationObserverPort;
import onlydust.com.marketplace.project.domain.port.output.GithubApiPort;
import onlydust.com.marketplace.project.domain.port.output.GithubAuthenticationInfoPort;
import onlydust.com.marketplace.project.domain.port.output.GithubStoragePort;
import onlydust.com.marketplace.project.domain.service.GithubAppService;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

@AllArgsConstructor
@Slf4j
public class GithubIssueCommenter implements ApplicationObserverPort {
    private final GithubStoragePort githubStoragePort;
    private final GithubAppService githubAppService;
    private final GithubAuthenticationInfoPort githubAuthenticationInfoPort;
    private final GithubApiPort githubApiPort;

    @Override
    public void onApplicationCreated(Application application) {
        if (application.origin() == Application.Origin.MARKETPLACE) return;

        final var issue = githubStoragePort.findIssueById(application.issueId())
                .orElseThrow(() -> internalServerError("Issue %s not found".formatted(application.issueId())));

        githubAppService.getInstallationTokenFor(issue.repoId())
                .filter(token -> githubAuthenticationInfoPort.getAuthorizedScopes(token).contains("issues"))
                .ifPresentOrElse(
                        token -> githubApiPort.createComment(token, issue, """
                                Hey @%d!
                                Thanks for showing interest.
                                We've created an application for you to contribute to this project.
                                Go check it out on OnlyDust!
                                """.formatted(application.applicantId())),
                        () -> LOGGER.info("Could not get an authorized GitHub token to comment on issue {}", issue.repoId())
                );
    }
}
