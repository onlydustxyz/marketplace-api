package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.Transaction;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookStorage;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.LedgerProvider;
import onlydust.com.marketplace.accounting.domain.port.out.LedgerStorage;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.time.ZonedDateTime;
import java.util.Collection;

@AllArgsConstructor
public class AccountingService {
    private final AccountBookStorage accountBookStorage;
    private final LedgerProvider<Object> ledgerProvider;
    private final LedgerStorage ledgerStorage;
    private final CurrencyStorage currencyStorage;

    public void fund(SponsorId sponsorId, PositiveAmount amount, Currency.Id currencyId, Network network) {
        fund(sponsorId, amount, currencyId, network, null);
    }

    public void fund(SponsorId sponsorId, PositiveAmount amount, Currency.Id currencyId, Network network, ZonedDateTime lockedUntil) {
        final var currency = getCurrency(currencyId);
        final var ledger = getOrCreateLedger(sponsorId, currency);

        ledger.credit(amount, network, lockedUntil);
        ledgerStorage.save(ledger);
    }

    public <To> void withdraw(To to, PositiveAmount amount, Currency.Id currencyId, Network network) {
        burn(to, amount, currencyId).forEach(transaction -> {
            final var ledger = ledgerStorage.get(transaction.from()).orElseThrow();
            ledger.debit(transaction.amount(), network);
            ledgerStorage.save(ledger);
        });
    }

    public <From> void mint(From from, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = accountBookStorage.get(currency);
        final var ledger = getOrCreateLedger(from, currency);

        accountBook.mint(ledger.id(), amount);
        accountBookStorage.save(accountBook);
    }

    public <To> Collection<Transaction> burn(To to, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = accountBookStorage.get(currency);
        final var ledger = getLedger(to, currency);

        final var transactions = accountBook.burn(ledger.id(), amount);

        accountBookStorage.save(accountBook);
        return transactions;
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

    private <From> Ledger getOrCreateLedger(From from, Currency currency) {
        return ledgerProvider.get(from, currency)
                .orElseGet(() -> createLedger(from, currency));
    }

    private <OwnerId> Ledger createLedger(OwnerId ownerId, Currency currency) {
        final var ledger = new Ledger(ownerId, currency);
        ledgerStorage.save(ledger);
        return ledger;
    }

    private <OwnerId> Ledger getLedger(OwnerId ownerId, Currency currency) {
        return ledgerProvider.get(ownerId, currency)
                .orElseThrow(() -> OnlyDustException.notFound("No ledger found for owner %s in currency %s".formatted(ownerId, currency)));
    }
}
