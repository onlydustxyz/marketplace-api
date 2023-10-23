package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.api.domain.model.Currency;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Data
@Builder
public class UserRewardView {
    UUID id;
    Integer numberOfRewardedContributions;
    Date requestedAt;
    String rewardedOnProjectName;
    String rewardedOnProjectLogoUrl;
    RewardStatusView status;
    RewardAmountView amount;


    @Data
    @Builder
    public static class RewardAmountView {
        BigDecimal total;
        Currency currency;
        BigDecimal dollarsEquivalent;
    }

    public enum RewardStatusView {
        pendingInvoice, processing, complete
    }

    public enum SortBy {
        requestedAt, status, contribution, amount
    }
}
