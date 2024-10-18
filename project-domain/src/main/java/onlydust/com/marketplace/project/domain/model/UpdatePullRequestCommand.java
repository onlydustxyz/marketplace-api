package onlydust.com.marketplace.project.domain.model;

import onlydust.com.marketplace.kernel.model.ContributionUUID;

import java.util.List;

public record UpdatePullRequestCommand(ContributionUUID id, Boolean archived, List<GithubIssue.Id> linkedIssueIds) {
}
