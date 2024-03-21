package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.project.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.project.domain.model.Project;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@Data
@Builder
public class RewardDetailsView {
    UUID id;
    CurrencyView currency;
    BigDecimal amount;
    BigDecimal dollarsEquivalent;
    RewardStatus status;
    Date unlockDate;
    GithubUserIdentity from;
    GithubUserIdentity to;
    Date createdAt;
    Date processedAt;
    ReceiptView receipt;
    Project project;
    UUID invoiceId;
    UUID billingProfileId;
}
