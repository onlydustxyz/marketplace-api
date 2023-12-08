package onlydust.com.marketplace.api.domain.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.view.pagination.Page;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Data
@Builder
public class ProjectRewardsPageView {
    Page<ProjectRewardView> rewards;
    Money remainingBudget;
    Money spentAmount;
    Integer sentRewardsCount;
    Integer rewardedContributionsCount;
    Integer rewardedContributorsCount;

    @AllArgsConstructor
    @Value
    public static class Money {
        BigDecimal amount;
        Currency currency;
        BigDecimal usdEquivalent;
    }
}
