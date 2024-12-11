package onlydust.com.marketplace.project.domain.job.githubcommands;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.project.domain.model.event.GithubCreateCommentCommand;
import onlydust.com.marketplace.project.domain.port.output.ApplicationObserverPort;
import onlydust.com.marketplace.project.domain.port.output.GithubApiPort;
import onlydust.com.marketplace.project.domain.port.output.GithubAuthenticationPort;
import onlydust.com.marketplace.project.domain.port.output.ProjectApplicationStoragePort;

@Slf4j
@AllArgsConstructor
public class GithubCreateCommentCommandConsumer {
    private final GithubApiPort githubApiPort;
    private final GithubAuthenticationPort githubAuthenticationPort;
    private final ProjectApplicationStoragePort projectApplicationStoragePort;
    private final ApplicationObserverPort applicationObserver;

    @Retryable(maxAttemptsExpression = "${application.retryable.github-command-consumers.max-attempts}", backoff = @Backoff(delay = 500L, multiplier = 2.0))
    @Transactional
    public void process(GithubCreateCommentCommand c) {
        final var application = projectApplicationStoragePort.findApplication(c.getApplicationId())
                .orElseThrow(() -> internalServerError("Application %s not found".formatted(c.getApplicationId())));
        final var personalAccessToken = githubAuthenticationPort.getGithubPersonalToken(c.getGithubUserId());
        final var commentId = githubApiPort.createComment(personalAccessToken,
                c.getRepoId(), c.getIssueNumber(),
                c.getBody());
        application.commentId(commentId);
        application.commentBody(c.getBody());
        projectApplicationStoragePort.save(application);
        applicationObserver.onApplicationCreationCompleted(application);
    }

    @Recover
    @Transactional
    public void onFailure(Exception e, GithubCreateCommentCommand c) {
        LOGGER.error("Failed to process GithubCreateCommentCommand: %s. Deleting application...".formatted(c), e);
        projectApplicationStoragePort.deleteApplications(c.getApplicationId());
    }
}
