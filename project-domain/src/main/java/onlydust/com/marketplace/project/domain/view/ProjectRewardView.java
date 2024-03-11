package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.project.domain.model.Currency;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProjectRewardView {
    UUID id;
    Integer numberOfRewardedContributions;
    Date requestedAt;
    Date processedAt;
    String rewardedUserLogin;
    String rewardedUserAvatar;
    RewardStatus status;
    Amount amount;

    public Date getUnlockDate() {
        return amount.currency.unlockDate();
    }

    @Data
    @Builder
    public static class Amount {
        BigDecimal total;
        Currency currency;
        BigDecimal dollarsEquivalent;
    }

    @Builder
    @Data
    public static class Filters {
        @Builder.Default
        List<UUID> currencies = List.of();
        @Builder.Default
        List<Long> contributors = List.of();
        Date from;
        Date to;
    }
}
