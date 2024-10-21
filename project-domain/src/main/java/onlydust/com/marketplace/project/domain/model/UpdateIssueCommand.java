package onlydust.com.marketplace.project.domain.model;

import onlydust.com.marketplace.kernel.model.ContributionUUID;

public record UpdateIssueCommand(ContributionUUID id, Boolean archived) {
}
