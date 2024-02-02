package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.AccountId;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookEventStorage;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.LedgerStorage;

import java.time.ZonedDateTime;
import java.util.Collection;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class AccountingService implements AccountingFacadePort {
    private final AccountBookEventStorage accountBookEventStorage;
    private final LedgerStorage ledgerStorage;
    private final CurrencyStorage currencyStorage;

    @Override
    public Ledger createLedger(@NonNull SponsorId sponsorId, Currency.@NonNull Id currencyId, @NonNull PositiveAmount amountToMint, ZonedDateTime lockedUntil) {
        final var currency = getCurrency(currencyId);

        final var ledger = createLedger(sponsorId, currency, lockedUntil);

        final var accountBook = getAccountBook(currency);
        accountBook.mint(AccountId.of(ledger.id()), amountToMint);
        accountBookEventStorage.save(currency, accountBook.pendingEvents());

        return ledger;
    }

    @Override
    public Ledger createLedger(@NonNull SponsorId sponsorId, Currency.@NonNull Id currencyId, @NonNull PositiveAmount amountToMint, ZonedDateTime lockedUntil,
                               @NonNull Ledger.Transaction transaction) {
        final var ledger = createLedger(sponsorId, currencyId, amountToMint, lockedUntil);
        // TODO fund
        ledger.add(transaction);
        ledgerStorage.save(ledger);
        return ledger;
    }

    @Override
    public void fund(@NonNull Ledger.Id sponsorLedgerId, @NonNull Ledger.Transaction transaction) {
        final var ledger = getLedger(sponsorLedgerId);
        ledger.add(transaction);
        ledgerStorage.save(ledger);
    }

    private Ledger getLedger(Ledger.Id sponsorLedgerId) {
        return ledgerStorage.get(sponsorLedgerId)
                .orElseThrow(() -> notFound("Ledger %s not found".formatted(sponsorLedgerId)));
    }

    @Override
    public void pay(RewardId rewardId, Currency.Id currencyId, Ledger.Transaction transaction) {
        final var currency = getCurrency(currencyId);
        final var accountBook = getAccountBook(currency);

        accountBook.state().transferredAmountPerOrigin(AccountId.of(rewardId)).forEach((sponsorLedgerId, remainingAmountForSponsor) -> {
            final var sponsorLedger = ledgerStorage.get(sponsorLedgerId.sponsorAccountId()).orElseThrow();
            final var payableAmount = sponsorLedger.payableAmount(remainingAmountForSponsor, transaction.network());
            accountBook.burn(AccountId.of(rewardId), payableAmount);
            sponsorLedger.add(transaction.withAmount(payableAmount.negate()));
            ledgerStorage.save(sponsorLedger);
        });

        accountBookEventStorage.save(currency, accountBook.pendingEvents());
    }

    @Override
    public boolean isPayable(RewardId rewardId, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = getAccountBook(currency);

        return accountBook.state().transferredAmountPerOrigin(AccountId.of(rewardId)).entrySet().stream()
                .noneMatch(entry -> {
                    final var sponsorLedger = ledgerStorage.get(entry.getKey().sponsorAccountId()).orElseThrow();
                    return sponsorLedger.unlockedBalance().isStrictlyLowerThan(entry.getValue());
                });
    }

    @Override
    public void mint(Ledger.Id sponsorAccountId, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = getAccountBook(currency);

        accountBook.mint(AccountId.of(sponsorAccountId), amount);
        accountBookEventStorage.save(currency, accountBook.pendingEvents());
    }

    private AccountBookAggregate getAccountBook(Currency currency) {
        return AccountBookAggregate.fromEvents(accountBookEventStorage.get(currency));
    }

    @Override
    public Collection<AccountBook.Transaction> burn(Ledger.Id sponsorAccountId, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = getAccountBook(currency);

        final var transactions = accountBook.burn(AccountId.of(sponsorAccountId), amount);

        accountBookEventStorage.save(currency, accountBook.pendingEvents());
        return transactions;
    }

    @Override
    public <From, To> void transfer(From from, To to, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = getAccountBook(currency);

        accountBook.transfer(AccountId.of(from), AccountId.of(to), amount);
        accountBookEventStorage.save(currency, accountBook.pendingEvents());
    }

    @Override
    public <From, To> void refund(From from, To to, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = getAccountBook(currency);

        accountBook.refund(AccountId.of(from), AccountId.of(to), amount);
        accountBookEventStorage.save(currency, accountBook.pendingEvents());
    }

    @Override
    public void delete(Ledger.Transaction.Id transactionId) {
        ledgerStorage.delete(transactionId);
    }

    private Currency getCurrency(Currency.Id id) {
        return currencyStorage.get(id)
                .orElseThrow(() -> notFound("Currency %s not found".formatted(id)));
    }

    private <OwnerId> Ledger createLedger(OwnerId ownerId, Currency currency, ZonedDateTime lockedUntil) {
        final var ledger = new Ledger(ownerId, currency, lockedUntil);
        ledgerStorage.save(ledger);
        return ledger;
    }
}
