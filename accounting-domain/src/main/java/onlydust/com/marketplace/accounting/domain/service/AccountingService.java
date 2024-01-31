package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.model.accountbook.Transaction;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookEventStorage;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.LedgerProvider;
import onlydust.com.marketplace.accounting.domain.port.out.LedgerStorage;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.time.ZonedDateTime;
import java.util.Collection;

import static onlydust.com.marketplace.accounting.domain.model.PositiveAmount.min;

@AllArgsConstructor
public class AccountingService implements AccountingFacadePort {
    private final AccountBookEventStorage accountBookEventStorage;
    private final LedgerProvider<Object> ledgerProvider;
    private final LedgerStorage ledgerStorage;
    private final CurrencyStorage currencyStorage;

    public void fund(SponsorId sponsorId, PositiveAmount amount, Currency.Id currencyId, Network network) {
        fund(sponsorId, amount, currencyId, network, null);
    }

    @Override
    public Ledger.Transaction.Id fund(SponsorId sponsorId, PositiveAmount amount, Currency.Id currencyId, Network network, ZonedDateTime lockedUntil) {
        final var currency = getCurrency(currencyId);
        final var ledger = getOrCreateLedger(sponsorId, currency);

        final var transaction = ledger.credit(amount, network, lockedUntil);
        ledgerStorage.save(ledger);
        return transaction.id();
    }

    @Override
    public void pay(RewardId rewardId, Currency.Id currencyId, Network network) {
        final var currency = getCurrency(currencyId);
        final var rewardLedger = getLedger(rewardId, currency);
        final var accountBook = getAccountBook(currency);
        final var rewardAmount = accountBook.state().balanceOf(rewardLedger.id());

        burn(rewardId, rewardAmount, currencyId).forEach(transaction -> {
            final var ledger = ledgerStorage.get(transaction.from()).orElseThrow();
            withdraw(ledger, min(transaction.amount(), ledger.unlockedBalance(network)), network);
        });
    }

    @Override
    public boolean isPayable(RewardId rewardId, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var rewardLedger = getLedger(rewardId, currency);
        final var accountBook = getAccountBook(currency);

        return accountBook.state().transferredAmountPerOrigin(rewardLedger.id())
                .entrySet()
                .stream()
                .noneMatch(entry -> {
                    final var sponsorLedger = ledgerStorage.get(entry.getKey()).orElseThrow();
                    return sponsorLedger.unlockedBalance().isStrictlyLowerThan(entry.getValue());
                });
    }

    @Override
    public Ledger.Transaction.Id withdraw(SponsorId sponsorId, PositiveAmount amount, Currency.Id currencyId, Network network) {
        final var currency = getCurrency(currencyId);
        final var ledger = getLedger(sponsorId, currency);

        return withdraw(ledger, amount, network);
    }

    private Ledger.Transaction.Id withdraw(Ledger ledger, PositiveAmount amount, Network network) {
        final var transaction = ledger.debit(amount, network);
        ledgerStorage.save(ledger);
        return transaction.id();
    }

    @Override
    public <To> void mint(To to, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = getAccountBook(currency);
        final var ledger = getOrCreateLedger(to, currency);

        accountBook.mint(ledger.id(), amount);
        accountBookEventStorage.save(currency, accountBook.pendingEvents());
    }

    private AccountBookAggregate getAccountBook(Currency currency) {
        return AccountBookAggregate.fromEvents(accountBookEventStorage.get(currency));
    }

    @Override
    public <To> Collection<Transaction> burn(To to, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = getAccountBook(currency);
        final var ledger = getLedger(to, currency);

        final var transactions = accountBook.burn(ledger.id(), amount);

        accountBookEventStorage.save(currency, accountBook.pendingEvents());
        return transactions;
    }

    @Override
    public <From, To> void transfer(From from, To to, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = getAccountBook(currency);
        final var fromLedger = getLedger(from, currency);
        final var toLedger = getOrCreateLedger(to, currency);

        accountBook.transfer(fromLedger.id(), toLedger.id(), amount);
        accountBookEventStorage.save(currency, accountBook.pendingEvents());
    }

    @Override
    public <From, To> void refund(From from, To to, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = getAccountBook(currency);
        final var fromLedger = getLedger(from, currency);
        final var toLedger = getLedger(to, currency);

        accountBook.refund(fromLedger.id(), toLedger.id(), amount);
        accountBookEventStorage.save(currency, accountBook.pendingEvents());
    }

    @Override
    public void delete(Ledger.Transaction.Id transactionId) {
        ledgerStorage.delete(transactionId);
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
