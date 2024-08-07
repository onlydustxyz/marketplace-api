package onlydust.com.marketplace.api.infrastructure.accounting;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import onlydust.com.marketplace.project.domain.port.output.AccountingServicePort;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
public class AccountingServiceAdapter implements AccountingServicePort {
    private final AccountingFacadePort accountingFacadePort;

    @Override
    public void createReward(UUID projectId, UUID rewardId, BigDecimal amount, CurrencyView.Id currencyId) {
        accountingFacadePort.createReward(ProjectId.of(projectId), RewardId.of(rewardId), PositiveAmount.of(amount), Currency.Id.of(currencyId.value()));
    }

    @Override
    public void cancelReward(UUID rewardId, CurrencyView.Id currencyId) {
        accountingFacadePort.cancel(RewardId.of(rewardId), Currency.Id.of(currencyId.value()));
    }
}
