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

        increaseAllowance(sponsorAccount.id(), amountToMint, currencyId);
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
    public void increaseAllowance(SponsorAccount.Id sponsorAccountId, Amount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = getAccountBook(currency);

        if (amount.isPositive())
            accountBook.mint(AccountId.of(sponsorAccountId), PositiveAmount.of(amount));
        else
            accountBook.burn(AccountId.of(sponsorAccountId), PositiveAmount.of(amount.negate()));

        accountBookEventStorage.save(currency, accountBook.pendingEvents());
    }

    @Override
    public SponsorAccountStatement fund(@NonNull SponsorAccount.Id sponsorAccountId, @NonNull SponsorAccount.Transaction transaction) {
        final var sponsorAccount = getSponsorAccount(sponsorAccountId);
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

    private SponsorAccount getSponsorAccount(SponsorAccount.Id sponsorAccountId) {
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
    public Optional<SponsorAccountStatement> getSponsorAccountStatement(SponsorAccount.Id sponsorAccountId, Currency.Id currencyId) {
        return sponsorAccountStorage.get(sponsorAccountId)
                .map(sponsorAccount -> new SponsorAccountStatement(sponsorAccount, getAccountBook(getCurrency(currencyId)).state()));
    }

    public void deleteTransaction(String reference) {
        sponsorAccountStorage.deleteTransaction(reference);
    }

    private Currency getCurrency(Currency.Id id) {
        return currencyStorage.get(id)
                .orElseThrow(() -> notFound("Currency %s not found".formatted(id)));
    }
}
