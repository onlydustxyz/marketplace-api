package onlydust.com.marketplace.api.infrastructure.accounting;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.CurrencyFacadePort;
import onlydust.com.marketplace.project.domain.port.output.AccountingServicePort;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
public class AccountingServiceAdapter implements AccountingServicePort {
    private final AccountingFacadePort accountingFacadePort;
    private final CurrencyFacadePort currencyFacadePort;

    @Override
    public void createReward(UUID projectId, UUID rewardId, BigDecimal amount, String currencyCode) {
        final var currency = getCurrency(currencyCode);
        accountingFacadePort.createReward(ProjectId.of(projectId), RewardId.of(rewardId), PositiveAmount.of(amount), currency.id());
    }

    @Override
    public void cancelReward(UUID rewardId, String currencyCode) {
        final var currency = getCurrency(currencyCode);
        accountingFacadePort.cancel(RewardId.of(rewardId), currency.id());
    }

    private Currency getCurrency(String code) {
        return currencyFacadePort.listCurrencies().stream()
                .filter(c -> c.code().toString().equals(code))
                .findFirst()
                .orElseThrow(() -> OnlyDustException.badRequest("Unsupported currency %s".formatted(code)));
    }
}
