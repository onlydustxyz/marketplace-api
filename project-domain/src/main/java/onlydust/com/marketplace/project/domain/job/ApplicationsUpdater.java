package onlydust.com.marketplace.project.domain.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.event.OnGithubCommentCreated;
import onlydust.com.marketplace.kernel.model.event.OnGithubCommentEdited;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubComment;
import onlydust.com.marketplace.project.domain.port.output.LLMPort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.port.output.UserStoragePort;

@Slf4j
@AllArgsConstructor
public class ApplicationsUpdater implements OutboxConsumer {
    private final ProjectStoragePort projectStoragePort;
    private final UserStoragePort userStoragePort;
    private final LLMPort llmPort;

    @Override
    public void process(Event event) {
        if (event instanceof OnGithubCommentCreated onGithubCommentCreated)
            process(onGithubCommentCreated);
        else if (event instanceof OnGithubCommentEdited onGithubCommentEdited)
            process(onGithubCommentEdited);
        else
            LOGGER.debug("Event type {} not handled", event.getClass().getSimpleName());
    }

    private void process(OnGithubCommentCreated event) {
        createMissingApplications(GithubComment.of(event));
    }

    private void process(OnGithubCommentEdited event) {
        final var comment = GithubComment.of(event);
        createMissingApplications(comment);
        deleteObsoleteApplications(comment);
    }

    private void createMissingApplications(GithubComment comment) {
        if (!userStoragePort.findApplications(comment.authorId(), comment.issueId()).isEmpty()) {
            LOGGER.debug("Skipping comment {} as user already applied to this issue", comment.id());
            return;
        }

        final var applications = projectStoragePort.findProjectsByRepoId(comment.repoId()).stream()
                .map(project -> Application.fromGithubComment(comment, project.getId()))
                .toArray(Application[]::new);

        if (applications.length > 0 && llmPort.isCommentShowingInterestToContribute(comment.body()))
            userStoragePort.save(applications);
    }

    private void deleteObsoleteApplications(GithubComment comment) {
        final var githubApplicationIds = userStoragePort.findApplications(comment.authorId(), comment.issueId()).stream()
                .filter(a -> a.origin() == Application.Origin.GITHUB)
                .map(Application::id)
                .toArray(Application.Id[]::new);

        if (githubApplicationIds.length > 0 && !llmPort.isCommentShowingInterestToContribute(comment.body()))
            userStoragePort.deleteApplications(githubApplicationIds);
    }
}
