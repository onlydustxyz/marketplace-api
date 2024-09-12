package onlydust.com.marketplace.api.infrastructure.accounting;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.project.domain.port.output.AccountingServicePort;

import java.math.BigDecimal;

@AllArgsConstructor
public class AccountingServiceAdapter implements AccountingServicePort {
    private final AccountingFacadePort accountingFacadePort;

    @Override
    public void createReward(ProjectId projectId, RewardId rewardId, BigDecimal amount, CurrencyView.Id currencyId) {
        accountingFacadePort.createReward(projectId, rewardId, PositiveAmount.of(amount), Currency.Id.of(currencyId.value()));
    }

    @Override
    public void cancelReward(RewardId rewardId, CurrencyView.Id currencyId) {
        accountingFacadePort.cancel(rewardId, Currency.Id.of(currencyId.value()));
    }
}
