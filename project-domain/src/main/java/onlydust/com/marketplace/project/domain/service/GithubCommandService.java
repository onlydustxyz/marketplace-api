package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.event.GithubCreateCommentCommand;

@AllArgsConstructor
public class GithubCommandService {
    private final OutboxPort githubCommandOutbox;

    public void createComment(@NonNull Application.Id applicationId, @NonNull GithubIssue issue, @NonNull Long githubUserId, @NonNull String body) {
        githubCommandOutbox.push(new GithubCreateCommentCommand(applicationId, githubUserId, issue.id().value(), issue.repoId(), issue.number(), body));
    }
}
