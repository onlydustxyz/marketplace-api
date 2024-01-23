package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Account;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookStorage;
import onlydust.com.marketplace.accounting.domain.port.out.AccountProvider;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

@AllArgsConstructor
public class AccountingService {
    private final AccountBookStorage accountBookStorage;
    private final AccountProvider<Object> accountProvider;
    private final CurrencyStorage currencyStorage;

    public void receiveFrom(SponsorId sponsorId, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = accountBookStorage.get(currency);
        final var account = getOrCreateAccount(sponsorId, currency);

        accountBook.mint(account, amount);
        accountBookStorage.save(accountBook);
    }

    public void refundTo(SponsorId sponsorId, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = accountBookStorage.get(currency);
        final var account = getAccount(sponsorId, currency);

        accountBook.burn(account, amount);
        accountBookStorage.save(accountBook);
    }

    public <From, To> void transfer(From from, To to, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = accountBookStorage.get(currency);
        final var fromAccount = getAccount(from, currency);
        final var toAccount = getOrCreateAccount(to, currency);

        accountBook.transfer(fromAccount, toAccount, amount);
        accountBookStorage.save(accountBook);
    }

    public <From, To> void refund(From from, To to, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = accountBookStorage.get(currency);
        final var fromAccount = getAccount(from, currency);
        final var toAccount = getAccount(to, currency);

        accountBook.refund(fromAccount, toAccount, amount);
        accountBookStorage.save(accountBook);
    }

    private Currency getCurrency(Currency.Id id) {
        return currencyStorage.get(id)
                .orElseThrow(() -> OnlyDustException.notFound("Currency %s not found".formatted(id)));
    }

    private <OwnerId> Account.Id getOrCreateAccount(OwnerId ownerId, Currency currency) {
        return accountProvider.get(ownerId, currency)
                .orElseGet(() -> accountProvider.create(ownerId, currency));
    }

    private <OwnerId> Account.Id getAccount(OwnerId ownerId, Currency currency) {
        return accountProvider.get(ownerId, currency)
                .orElseThrow(() -> OnlyDustException.notFound("No account found for owner %s in currency %s".formatted(ownerId, currency)));
    }
}
