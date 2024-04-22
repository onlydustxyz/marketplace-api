package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.math.BigDecimal;
import java.util.List;

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
        return budgetStatsPerCurrency.stream()
                .map(BudgetStats::remainingBudget)
                .map(Money::getUsdEquivalent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalSpentUsdEquivalent() {
        return budgetStatsPerCurrency.stream()
                .map(BudgetStats::spentAmount)
                .map(Money::getUsdEquivalent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
