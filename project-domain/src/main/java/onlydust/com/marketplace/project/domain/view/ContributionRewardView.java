package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Value;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.project.domain.model.GithubUserIdentity;

import java.util.Date;
import java.util.UUID;

@Value
@Builder
public class ContributionRewardView {
    UUID id;
    Money amount;
    RewardStatus status;
    GithubUserIdentity from;
    GithubUserIdentity to;
    Date createdAt;
    Date processedAt;
    UUID billingProfileId;
}
