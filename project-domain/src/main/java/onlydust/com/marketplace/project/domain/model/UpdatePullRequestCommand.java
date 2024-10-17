package onlydust.com.marketplace.project.domain.model;

import java.util.List;

public record UpdatePullRequestCommand(GithubPullRequest.Id id, Boolean archived, List<GithubIssue.Id> linkedIssueIds) {
}
