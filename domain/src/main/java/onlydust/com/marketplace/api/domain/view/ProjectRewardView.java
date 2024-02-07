package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.api.domain.model.Currency;

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
    RewardStatusView status;
    RewardAmountView amount;

    public Date getUnlockDate() {
        return amount.currency.unlockDate();
    }

    @Data
    @Builder
    public static class RewardAmountView {
        BigDecimal total;
        Currency currency;
        BigDecimal dollarsEquivalent;
    }

    public enum RewardStatusView {
        pendingSignup, processing, complete, locked, pendingContributor
    }

    public enum SortBy {
        requestedAt, status, contribution, amount
    }

    @Builder
    @Data
    public static class Filters {
        @Builder.Default
        List<Currency> currencies = List.of();
        @Builder.Default
        List<Long> contributors = List.of();
        Date from;
        Date to;
    }
}
