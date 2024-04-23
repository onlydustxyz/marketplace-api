package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.math.BigDecimal;
import java.util.List;

import static onlydust.com.marketplace.kernel.mapper.AmountMapper.prettyUsd;

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
        return prettyUsd(rewardAmountsPerCurrency.stream()
                .map(RewardAmounts::rewardedAmount)
                .map(money -> money.dollarsEquivalent().orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public BigDecimal totalPendingAmountUsdEquivalent() {
        return prettyUsd(rewardAmountsPerCurrency.stream()
                .map(RewardAmounts::pendingAmount)
                .map(money -> money.dollarsEquivalent().orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
