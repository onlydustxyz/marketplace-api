package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.api.domain.model.Project;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Data
@Builder
public class RewardView {
    UUID id;
    Currency currency;
    BigDecimal amount;
    BigDecimal dollarsEquivalent;
    UserRewardStatus status;
    GithubUserIdentity from;
    GithubUserIdentity to;
    Date createdAt;
    Date processedAt;
    ReceiptView receipt;
    Project project;

    public Date getUnlockDate() {
        return currency.unlockDate();
    }
}
