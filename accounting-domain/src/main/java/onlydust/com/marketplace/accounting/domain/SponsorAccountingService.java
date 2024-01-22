package onlydust.com.marketplace.accounting.domain;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.PositiveMoney;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;

@AllArgsConstructor
public class SponsorAccountingService {

    private final SponsorAccountProvider sponsorAccountProvider;
    private final CurrencyStorage currencyStorage;

    public void receiveFrom(SponsorId sponsorId, PositiveAmount amount, Currency.Id id) {
        final var currency = currencyStorage.get(id).orElseThrow();
        final var account = sponsorAccountProvider.get(sponsorId, currency)
                .orElseGet(() -> sponsorAccountProvider.create(sponsorId, currency));
        account.mint(PositiveMoney.of(PositiveMoney.of(amount, currency)));
    }
}
