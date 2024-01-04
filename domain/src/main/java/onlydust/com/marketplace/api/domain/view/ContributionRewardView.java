package onlydust.com.marketplace.api.domain.view;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.model.GithubUserIdentity;

@Value
@Builder
public class ContributionRewardView {

  UUID id;
  Currency currency;
  BigDecimal amount;
  BigDecimal dollarsEquivalent;
  UserRewardView.RewardStatusView status;
  GithubUserIdentity from;
  GithubUserIdentity to;
  Date createdAt;
  Date processedAt;
}
