package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Ledger;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookStorage;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.LedgerProvider;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

@AllArgsConstructor
public class AccountingService {
    private final AccountBookStorage accountBookStorage;
    private final LedgerProvider<Object> ledgerProvider;
    private final CurrencyStorage currencyStorage;

    public <From> void receiveFrom(From from, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = accountBookStorage.get(currency);
        final var ledger = getOrCreateLedger(from, currency);

        accountBook.mint(ledger.id(), amount);
        accountBookStorage.save(accountBook);
    }

    public <To> void sendTo(To to, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = accountBookStorage.get(currency);
        final var ledger = getLedger(to, currency);

        final var funderLedgerId = accountBook.funders(ledger.id()).stream().findFirst().orElseThrow();
        final var funderLedger = ledgerProvider.get(funderLedgerId, currency).orElseThrow();
        if (funderLedger.balance().isStrictlyLowerThan(amount)) {
            throw OnlyDustException.badRequest("Not enough funds");
        }

        accountBook.burn(ledger.id(), amount);
        accountBookStorage.save(accountBook);
    }

    public <From, To> void transfer(From from, To to, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = accountBookStorage.get(currency);
        final var fromLedger = getLedger(from, currency);
        final var toLedger = getOrCreateLedger(to, currency);

        accountBook.transfer(fromLedger.id(), toLedger.id(), amount);
        accountBookStorage.save(accountBook);
    }

    public <From, To> void refund(From from, To to, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = accountBookStorage.get(currency);
        final var fromLedger = getLedger(from, currency);
        final var toLedger = getLedger(to, currency);

        accountBook.refund(fromLedger.id(), toLedger.id(), amount);
        accountBookStorage.save(accountBook);
    }

    private Currency getCurrency(Currency.Id id) {
        return currencyStorage.get(id)
                .orElseThrow(() -> OnlyDustException.notFound("Currency %s not found".formatted(id)));
    }

    private <OwnerId> Ledger getOrCreateLedger(OwnerId ownerId, Currency currency) {
        return ledgerProvider.get(ownerId, currency)
                .orElseGet(() -> ledgerProvider.create(ownerId, currency));
    }

    private <OwnerId> Ledger getLedger(OwnerId ownerId, Currency currency) {
        return ledgerProvider.get(ownerId, currency)
                .orElseThrow(() -> OnlyDustException.notFound("No ledger found for owner %s in currency %s".formatted(ownerId, currency)));
    }
}
