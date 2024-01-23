package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.port.out.AccountProvider;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

@AllArgsConstructor
public class AccountingService {
    private final AccountProvider<Object> accountProvider;
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

    public <From, To> void send(From from, To to, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var fromAccount = getAccount(from, currency);
        final var toAccount = getOrCreateAccount(to, currency);

        fromAccount.send(toAccount, PositiveMoney.of(amount, currency));
    }

    public <From, To> void refund(From from, To to, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var fromAccount = getAccount(from, currency);
        final var toAccount = getAccount(to, currency);

        toAccount.refund(fromAccount, PositiveMoney.of(amount, currency));
    }

    private Currency getCurrency(Currency.Id id) {
        return currencyStorage.get(id)
                .orElseThrow(() -> OnlyDustException.notFound("Currency %s not found".formatted(id)));
    }

    private <OwnerId> Account getOrCreateAccount(OwnerId ownerId, Currency currency) {
        return accountProvider.get(ownerId, currency)
                .orElseGet(() -> accountProvider.create(ownerId, currency));
    }

    private <OwnerId> Account getAccount(OwnerId ownerId, Currency currency) {
        return accountProvider.get(ownerId, currency)
                .orElseThrow(() -> OnlyDustException.notFound("No account found for owner %s in currency %s".formatted(ownerId, currency)));
    }
}
