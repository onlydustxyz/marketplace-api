package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.project.domain.model.Currency;
import onlydust.com.marketplace.project.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.project.domain.model.Project;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Data
@Builder
public class RewardDetailsView {
    UUID id;
    Currency currency;
    BigDecimal amount;
    BigDecimal dollarsEquivalent;
    UserRewardView.Status statusForUser;
    ProjectRewardView.Status statusForProjectLead;
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
