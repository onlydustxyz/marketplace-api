package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import onlydust.com.marketplace.kernel.model.RewardStatus;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Data
@Builder
public class BillingProfileRewardView {
    UUID id;
    UUID projectId;
    Integer numberOfRewardedContributions;
    Date requestedAt;
    Date processedAt;
    String rewardedOnProjectName;
    String rewardedOnProjectLogoUrl;
    RewardStatus status;
    Date unlockDate;
    Amount amount;
    Long recipientId;
    String recipientLogin;
    String recipientAvatarUrl;
    UUID billingProfileId;

    @Data
    @Builder
    public static class Amount {
        BigDecimal total;
        CurrencyView currency;
        BigDecimal dollarsEquivalent;
    }
}
