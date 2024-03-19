package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Value;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.project.domain.model.GithubUserIdentity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Value
@Builder
public class ContributionRewardView {
    UUID id;
    CurrencyView currency;
    BigDecimal amount;
    BigDecimal dollarsEquivalent;
    RewardStatus status;
    GithubUserIdentity from;
    GithubUserIdentity to;
    Date createdAt;
    Date processedAt;
}
