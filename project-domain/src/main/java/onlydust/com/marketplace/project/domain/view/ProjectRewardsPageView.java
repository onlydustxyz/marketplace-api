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
public class ProjectRewardsPageView {
    Page<ProjectRewardView> rewards;
    List<BudgetStats> budgetStatsPerCurrency;
    Integer sentRewardsCount;
    Integer rewardedContributionsCount;
    Integer rewardedContributorsCount;

    public record BudgetStats(@NonNull Money remainingBudget, @NonNull Money spentAmount) {
    }

    public BigDecimal totalRemainingUsdEquivalent() {
        return prettyUsd(budgetStatsPerCurrency.stream()
                .map(BudgetStats::remainingBudget)
                .map(money -> money.dollarsEquivalent().orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public BigDecimal totalSpentUsdEquivalent() {
        return prettyUsd(budgetStatsPerCurrency.stream()
                .map(BudgetStats::spentAmount)
                .map(money -> money.dollarsEquivalent().orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
