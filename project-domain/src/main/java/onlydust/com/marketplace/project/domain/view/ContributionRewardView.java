package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Value;
import onlydust.com.marketplace.project.domain.model.Currency;
import onlydust.com.marketplace.project.domain.model.GithubUserIdentity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Value
@Builder
public class ContributionRewardView {
    UUID id;
    Currency currency;
    BigDecimal amount;
    BigDecimal dollarsEquivalent;
    UserRewardView.Status status;
    GithubUserIdentity from;
    GithubUserIdentity to;
    Date createdAt;
    Date processedAt;
}
