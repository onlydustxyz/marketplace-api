package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class UserRewardsPageView {
    Page<UserRewardView> rewards;
    List<RewardAmounts> rewardAmountsPerCurrency;
    Integer receivedRewardsCount;
    Integer rewardedContributionsCount;
    Integer rewardingProjectsCount;
    Integer pendingRequestCount;

    public record RewardAmounts(@NonNull Money rewardedAmount, @NonNull Money pendingAmount) {
    }

    public BigDecimal totalRewardedAmountUsdEquivalent() {
        return rewardAmountsPerCurrency.stream()
                .map(RewardAmounts::rewardedAmount)
                .map(Money::getUsdEquivalent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalPendingAmountUsdEquivalent() {
        return rewardAmountsPerCurrency.stream()
                .map(RewardAmounts::pendingAmount)
                .map(Money::getUsdEquivalent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
