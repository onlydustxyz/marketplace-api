package onlydust.com.marketplace.project.domain.job;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.event.OnGithubCommentCreated;
import onlydust.com.marketplace.kernel.model.event.OnGithubCommentEdited;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubComment;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.Project;
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

    private void process(OnGithubCommentCreated onGithubCommentCreated) {
        final var issueId = GithubIssue.Id.of(onGithubCommentCreated.issueId());

        if (!userStoragePort.findApplications(onGithubCommentCreated.authorId(), issueId).isEmpty()) {
            LOGGER.debug("Skipping comment {} as user already applied to this issue", onGithubCommentCreated.id());
            return;
        }

        final var applications = projectStoragePort.findProjectsByRepoId(onGithubCommentCreated.repoId()).stream()
                .map(project -> newApplication(onGithubCommentCreated, project))
                .toArray(Application[]::new);

        if (applications.length > 0 && llmPort.isCommentShowingInterestToContribute(onGithubCommentCreated.body()))
            userStoragePort.save(applications);
    }

    private void process(OnGithubCommentEdited onGithubCommentEdited) {
        final var commentId = GithubComment.Id.of(onGithubCommentEdited.id());
    }

    private static @NonNull Application newApplication(OnGithubCommentCreated onGithubCommentCreated, Project project) {
        return Application.fromGithub(project.getId(),
                onGithubCommentCreated.authorId(),
                onGithubCommentCreated.createdAt(),
                GithubIssue.Id.of(onGithubCommentCreated.issueId()),
                GithubComment.Id.of(onGithubCommentCreated.id()));
    }
}
