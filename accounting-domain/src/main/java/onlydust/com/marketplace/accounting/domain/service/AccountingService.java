package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.AccountId;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookEventStorage;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorAccountStorage;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class AccountingService implements AccountingFacadePort {
    private final AccountBookEventStorage accountBookEventStorage;
    private final SponsorAccountStorage sponsorAccountStorage;
    private final CurrencyStorage currencyStorage;

    @Override
    public SponsorAccountStatement createSponsorAccount(@NonNull SponsorId sponsorId, Currency.@NonNull Id currencyId, @NonNull PositiveAmount amountToMint,
                                                        ZonedDateTime lockedUntil) {
        final var currency = getCurrency(currencyId);
        final var sponsorAccount = new SponsorAccount(sponsorId, currency, lockedUntil);
        sponsorAccountStorage.save(sponsorAccount);

        increaseAllowance(sponsorAccount.id(), amountToMint);
        return new SponsorAccountStatement(sponsorAccount, getAccountBook(currency).state());
    }

    @Override
    public SponsorAccountStatement createSponsorAccount(@NonNull SponsorId sponsorId, Currency.@NonNull Id currencyId, @NonNull PositiveAmount amountToMint,
                                                        ZonedDateTime lockedUntil,
                                                        @NonNull SponsorAccount.Transaction transaction) {
        final var sponsorAccount = createSponsorAccount(sponsorId, currencyId, amountToMint, lockedUntil);
        return fund(sponsorAccount.account().id(), transaction);
    }

    @Override
    public SponsorAccountStatement increaseAllowance(SponsorAccount.Id sponsorAccountId, Amount amount) {
        final var sponsorAccount = mustGetSponsorAccount(sponsorAccountId);
        final var accountBook = getAccountBook(sponsorAccount.currency());

        if (amount.isPositive())
            accountBook.mint(AccountId.of(sponsorAccountId), PositiveAmount.of(amount));
        else
            accountBook.burn(AccountId.of(sponsorAccountId), PositiveAmount.of(amount.negate()));

        accountBookEventStorage.save(sponsorAccount.currency(), accountBook.pendingEvents());
        return getSponsorAccountStatement(sponsorAccountId).orElseThrow();
    }

    @Override
    public SponsorAccountStatement fund(@NonNull SponsorAccount.Id sponsorAccountId, @NonNull SponsorAccount.Transaction transaction) {
        final var sponsorAccount = mustGetSponsorAccount(sponsorAccountId);
        sponsorAccount.add(transaction);
        sponsorAccountStorage.save(sponsorAccount);
        return new SponsorAccountStatement(sponsorAccount, getAccountBook(sponsorAccount.currency()).state());
    }

    @Override
    public void pay(RewardId rewardId, Currency.Id currencyId, SponsorAccount.Transaction transaction) {
        final var currency = getCurrency(currencyId);
        final var accountBook = getAccountBook(currency);

        accountBook.state().transferredAmountPerOrigin(AccountId.of(rewardId)).forEach((sponsorAccountId, amount) -> {
            final var sponsorAccount = sponsorAccountStorage.get(sponsorAccountId.sponsorAccountId()).orElseThrow();
            final var sponsorAccountNetwork = sponsorAccount.network().orElseThrow(
                    () -> internalServerError("Sponsor account %s is not funded".formatted(sponsorAccountId.sponsorAccountId()))
            );

            if (transaction.network().equals(sponsorAccountNetwork)) {
                accountBook.burn(AccountId.of(rewardId), amount);
                sponsorAccount.add(transaction.withAmount(amount.negate()));
                sponsorAccountStorage.save(sponsorAccount);
            }
        });

        accountBookEventStorage.save(currency, accountBook.pendingEvents());
    }

    @Override
    public boolean isPayable(RewardId rewardId, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = getAccountBook(currency);

        return accountBook.state().transferredAmountPerOrigin(AccountId.of(rewardId)).entrySet().stream()
                .noneMatch(entry -> {
                    final var sponsorAccount = sponsorAccountStorage.get(entry.getKey().sponsorAccountId()).orElseThrow();
                    return sponsorAccount.unlockedBalance().isStrictlyLowerThan(entry.getValue());
                });
    }

    private SponsorAccount mustGetSponsorAccount(SponsorAccount.Id sponsorAccountId) {
        return sponsorAccountStorage.get(sponsorAccountId)
                .orElseThrow(() -> notFound("Sponsor account %s not found".formatted(sponsorAccountId)));
    }

    private AccountBookAggregate getAccountBook(Currency currency) {
        return AccountBookAggregate.fromEvents(accountBookEventStorage.get(currency));
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
    public Optional<SponsorAccountStatement> getSponsorAccountStatement(SponsorAccount.Id sponsorAccountId) {
        return sponsorAccountStorage.get(sponsorAccountId)
                .map(sponsorAccount -> new SponsorAccountStatement(sponsorAccount, getAccountBook(sponsorAccount.currency()).state()));
    }

    @Override
    public Optional<SponsorAccount> getSponsorAccount(SponsorAccount.Id sponsorAccountId) {
        return sponsorAccountStorage.get(sponsorAccountId);
    }

    @Override
    public List<SponsorAccountStatement> getSponsorAccounts(SponsorId sponsorId) {
        return sponsorAccountStorage.getSponsorAccounts(sponsorId).stream()
                .map(sponsorAccount -> new SponsorAccountStatement(sponsorAccount, getAccountBook(sponsorAccount.currency()).state()))
                .toList();
    }

    @Override
    public SponsorAccountStatement updateSponsorAccount(SponsorAccount.@NonNull Id sponsorAccountId, ZonedDateTime lockedUntil) {
        final var sponsorAccount = mustGetSponsorAccount(sponsorAccountId);
        sponsorAccount.lockUntil(lockedUntil);
        sponsorAccountStorage.save(sponsorAccount);
        return new SponsorAccountStatement(sponsorAccount, getAccountBook(sponsorAccount.currency()).state());
    }

    public SponsorAccountStatement deleteTransaction(SponsorAccount.Id sponsorAccountId, String reference) {
        sponsorAccountStorage.deleteTransaction(sponsorAccountId, reference);
        return getSponsorAccountStatement(sponsorAccountId).orElseThrow();
    }

    private Currency getCurrency(Currency.Id id) {
        return currencyStorage.get(id)
                .orElseThrow(() -> notFound("Currency %s not found".formatted(id)));
    }
}
