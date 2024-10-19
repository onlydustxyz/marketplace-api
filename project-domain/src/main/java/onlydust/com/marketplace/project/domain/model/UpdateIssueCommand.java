package onlydust.com.marketplace.project.domain.model;

public record UpdateIssueCommand(GithubIssue.Id id, Boolean archived, Boolean closed) {
}
