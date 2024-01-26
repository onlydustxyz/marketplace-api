package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Ledger;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookStorage;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.LedgerProvider;
import onlydust.com.marketplace.accounting.domain.port.out.LedgerStorage;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

@AllArgsConstructor
public class AccountingService {
    private final AccountBookStorage accountBookStorage;
    private final LedgerProvider<Object> ledgerProvider;
    private final LedgerStorage ledgerStorage;
    private final CurrencyStorage currencyStorage;

    public void fund(PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var ledger = getOrCreateLedger(currency);

        ledger.credit(amount);
        ledgerStorage.save(currency, ledger);
    }

    public <To> void withdraw(To to, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        burn(to, amount, currencyId);
        final var ledger = ledgerStorage.get(currency).orElseThrow();
        ledger.debit(amount);
        ledgerStorage.save(currency, ledger);
    }

    public <From> void mint(From from, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = accountBookStorage.get(currency);
        final var ledger = getOrCreateLedger(from, currency);

        accountBook.mint(ledger.id(), amount);
        accountBookStorage.save(accountBook);
    }

    public <To> void burn(To to, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = accountBookStorage.get(currency);
        final var ledger = getLedger(to, currency);

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

    private Ledger getOrCreateLedger(Currency currency) {
        return ledgerStorage.get(currency)
                .orElseGet(() -> createLedger(currency));
    }

    private <From> Ledger getOrCreateLedger(From from, Currency currency) {
        return ledgerProvider.get(from, currency)
                .orElseGet(() -> createLedger(from, currency));
    }

    private Ledger createLedger(Currency currency) {
        final var ledger = new Ledger();
        ledgerStorage.save(currency, ledger);
        return ledger;
    }

    private <OwnerId> Ledger createLedger(OwnerId ownerId, Currency currency) {
        final var ledger = new Ledger();
        ledgerProvider.save(ownerId, currency, ledger);
        return ledger;
    }

    private <OwnerId> Ledger getLedger(OwnerId ownerId, Currency currency) {
        return ledgerProvider.get(ownerId, currency)
                .orElseThrow(() -> OnlyDustException.notFound("No ledger found for owner %s in currency %s".formatted(ownerId, currency)));
    }
}
