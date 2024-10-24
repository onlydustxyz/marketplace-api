package onlydust.com.marketplace.project.domain.job;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.event.*;
import onlydust.com.marketplace.kernel.port.output.IndexerPort;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubComment;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.port.output.ApplicationObserverPort;
import onlydust.com.marketplace.project.domain.port.output.LLMPort;
import onlydust.com.marketplace.project.domain.port.output.ProjectApplicationStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.joining;

@Slf4j
@AllArgsConstructor
@Transactional
public class ApplicationsUpdater implements OutboxConsumer {
    private final ProjectStoragePort projectStoragePort;
    private final ProjectApplicationStoragePort projectApplicationStoragePort;
    private final LLMPort llmPort;
    private final IndexerPort indexerPort;
    private final ApplicationObserverPort applicationObserverPort;

    @Override
    public void process(Event event) {
        if (event instanceof OnGithubCommentCreated onGithubCommentCreated)
            process(onGithubCommentCreated);
        else if (event instanceof OnGithubCommentEdited onGithubCommentEdited)
            process(onGithubCommentEdited);
        else if (event instanceof OnGithubCommentDeleted onGithubCommentDeleted)
            process(onGithubCommentDeleted);
        else if (event instanceof OnGithubIssueDeleted onGithubIssueDeleted)
            process(onGithubIssueDeleted);
        else if (event instanceof OnGithubIssueTransferred onGithubIssueTransferred)
            process(onGithubIssueTransferred);
        else
            LOGGER.debug("Event type {} not handled", event.getClass().getSimpleName());
    }

    private void process(OnGithubCommentCreated event) {
        createMissingApplications(GithubComment.of(event));
    }

    private void process(OnGithubCommentEdited event) {
        final var comment = GithubComment.of(event);
        deleteObsoleteGithubApplications(comment.id(), Optional.of(comment.body()));
        updateExistingApplications(comment);
        createMissingApplications(comment);
    }

    private void process(OnGithubCommentDeleted event) {
        final var commentId = GithubComment.Id.of(event.id());
        deleteObsoleteGithubApplications(commentId, Optional.empty());
    }

    private void process(OnGithubIssueDeleted event) {
        final var issueId = GithubIssue.Id.of(event.id());
        projectApplicationStoragePort.deleteApplicationsByIssueId(issueId);
    }

    private void process(OnGithubIssueTransferred event) {
        final var issueId = GithubIssue.Id.of(event.id());
        projectApplicationStoragePort.deleteApplicationsByIssueId(issueId);
    }

    private void updateExistingApplications(GithubComment comment) {
        final var applications = projectApplicationStoragePort.findApplications(comment.id());
        if (!applications.isEmpty()) {
            applications.forEach(a -> a.commentBody(comment.body()));
            projectApplicationStoragePort.save(applications.toArray(Application[]::new));
        }
    }

    private void createMissingApplications(GithubComment comment) {
        final var existingApplicationsForUser = projectApplicationStoragePort.findApplications(comment.authorId(), comment.issueId());
        if (!existingApplicationsForUser.isEmpty()) {
            LOGGER.debug("Skipping comment {} as user already applied to this issue", comment.id());
            return;
        }

        final var projectIds = projectStoragePort.findProjectIdsByRepoId(comment.repoId());
        if (projectIds.isEmpty()) {
            LOGGER.debug("Skipping comment {} as not related to a project", comment.id());
            return;
        }

        final var pattern = Pattern.compile("\\R");
        final var cleanComment = pattern.splitAsStream(comment.body())
                .filter(s -> !s.startsWith(">"))
                .filter(s -> !s.isEmpty())
                .collect(joining("\n"));

        if (llmPort.isCommentShowingInterestToContribute(cleanComment)) {
            indexerPort.indexUser(comment.authorId());
            saveGithubApplications(comment, projectIds);
        }
    }

    private void saveGithubApplications(GithubComment comment, List<ProjectId> projectIds) {
        final var applications = projectIds.stream()
                .map(projectId -> Application.fromGithubComment(comment, projectId))
                .toArray(Application[]::new);

        projectApplicationStoragePort.save(applications);
        Arrays.stream(applications).forEach(applicationObserverPort::onApplicationCreated);
    }

    private void deleteObsoleteGithubApplications(@NonNull GithubComment.Id commentId, @NonNull Optional<String> commentBody) {
        final var githubApplicationIds = projectApplicationStoragePort.findApplications(commentId).stream()
                .filter(a -> a.origin() == Application.Origin.GITHUB)
                .map(Application::id)
                .toArray(Application.Id[]::new);

        if (githubApplicationIds.length > 0 && commentBody.map(body -> !llmPort.isCommentShowingInterestToContribute(body)).orElse(true))
            projectApplicationStoragePort.deleteApplications(githubApplicationIds);
    }
}
