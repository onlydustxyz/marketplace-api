package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import onlydust.com.marketplace.kernel.model.ContributionUUID;

@Builder
public record UpdatePullRequestCommand(ContributionUUID id, Boolean archived) {
}
