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
public class UserRewardView {
    UUID id;
    UUID projectId;
    Integer numberOfRewardedContributions;
    Date requestedAt;
    Date processedAt;
    String rewardedOnProjectName;
    String rewardedOnProjectLogoUrl;
    UserRewardStatus status;
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

    public enum SortBy {
        requestedAt, status, contribution, amount
    }

    @Data
    @Builder
    public static class Filters {
        List<Currency> currencies;
        List<UUID> projectIds;
        Date from;
        Date to;
    }
}
