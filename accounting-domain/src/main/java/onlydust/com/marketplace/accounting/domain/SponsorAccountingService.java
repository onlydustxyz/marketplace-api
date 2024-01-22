package onlydust.com.marketplace.accounting.domain;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

@AllArgsConstructor
public class SponsorAccountingService {

    private final SponsorAccountProvider sponsorAccountProvider;
    private final CurrencyStorage currencyStorage;

    public void receiveFrom(SponsorId sponsorId, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var account = getOrCreateAccount(sponsorId, currency);

        account.mint(PositiveMoney.of(PositiveMoney.of(amount, currency)));
    }

    public void refundTo(SponsorId sponsorId, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var account = getAccount(sponsorId, currency);

        account.burn(PositiveMoney.of(PositiveMoney.of(amount, currency)));
    }

    private Account getOrCreateAccount(SponsorId sponsorId, Currency currency) {
        return sponsorAccountProvider.get(sponsorId, currency)
                .orElseGet(() -> sponsorAccountProvider.create(sponsorId, currency));
    }

    private Account getAccount(SponsorId sponsorId, Currency currency) {
        return sponsorAccountProvider.get(sponsorId, currency)
                .orElseThrow(() -> OnlyDustException.notFound("No account found for sponsor %s in currency %s".formatted(sponsorId, currency)));
    }

    private Currency getCurrency(Currency.Id id) {
        return currencyStorage.get(id)
                .orElseThrow(() -> OnlyDustException.notFound("Currency %s not found".formatted(id)));
    }
}
